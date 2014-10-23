/*
 * Copyright (c) 2014 Oculus Info Inc. 
 * http://www.oculusinfo.com/
 * 
 * Released under the MIT License.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oculusinfo.binning.io.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;

import com.oculusinfo.binning.TileData;
import com.oculusinfo.binning.TileIndex;
import com.oculusinfo.binning.util.TypeDescriptor;

abstract public class GenericAvroSerializer<T> implements TileSerializer<T> {
	private static final long serialVersionUID = 5775555328063499845L;


	private Schema _tileSchema = null;
	private Schema _recordSchema = null;

	private CodecFactory _compressionCodec;
	private TypeDescriptor _typeDescription;
	protected GenericAvroSerializer (CodecFactory compressionCodec, TypeDescriptor typeDescription) {
		_compressionCodec = compressionCodec;
		_typeDescription = typeDescription;
	}

	abstract protected String getRecordSchemaFile ();
	abstract protected T getValue (GenericRecord bin);
	abstract protected void setValue (GenericRecord bin, T value) throws IOException ;
    
	public String getFileExtension(){
		return "avro";
	}
    
	protected Schema getRecordSchema () throws IOException {
		if (_recordSchema == null) {
			_recordSchema = createRecordSchema();
		}
		return _recordSchema;
	}
	
	protected Schema createRecordSchema() throws IOException {
		return new AvroSchemaComposer().addResource(getRecordSchemaFile()).resolved();
	}
	
	protected Schema getTileSchema () throws IOException {
		if (_tileSchema == null) {
			_tileSchema = createTileSchema();
		}
		return _tileSchema;
	}
	
	protected Schema createTileSchema() throws IOException {
		return new AvroSchemaComposer().add(getRecordSchema()).addResource("tile.avsc").resolved();
	}
	
	@Override
	public TypeDescriptor getBinTypeDescription () {
		return _typeDescription;
	}

	protected Map<String, String> getTileMetaData (TileData<T> tile) {
		Collection<String> keys = tile.getMetaDataProperties();
		if (null == keys || keys.isEmpty()) return null;
		Map<String, String> metaData = new HashMap<String, String>();
		for (String key: keys) {
			String value = tile.getMetaData(key);
			if (null != value)
				metaData.put(key, value);
		}
		return metaData;
	}

	@Override
	public TileData<T> deserialize (TileIndex index, InputStream stream) throws IOException {

		DatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>();
		DataFileStream<GenericRecord> dataFileReader = new DataFileStream<GenericRecord>(stream, reader);
    	
		try {
        	
			GenericRecord r = dataFileReader.next();

			int level = (Integer) r.get("level");
			int xIndex = (Integer) r.get("xIndex");
			int yIndex = (Integer) r.get("yIndex");
			int xBins = (Integer) r.get("xBinCount");
			int yBins = (Integer) r.get("yBinCount");
			Map<?, ?> meta = (Map<?, ?>) r.get("meta");
			
                    
			@SuppressWarnings("unchecked")
			GenericData.Array<GenericRecord> bins = (GenericData.Array<GenericRecord>) r.get("values");

			// Warning suppressed because Array.newInstance definitionally returns 
			// something of the correct type, or throws an exception 
			List<T> data = new ArrayList<T>(xBins*yBins);
			int i = 0;
			for (GenericRecord bin: bins) {
				data.add(getValue(bin));
				if (i >= xBins*yBins) break;
			}
			TileIndex newTileIndex = new TileIndex(level, xIndex, yIndex, xBins, yBins);
			TileData<T> newTile = new TileData<T>(newTileIndex, data);
			if (null != meta) {
    			for (Object key: meta.keySet()) {
    				if (null != key) {
    					Object value = meta.get(key);
    					if (null != value) {
    						newTile.setMetaData(key.toString(), value.toString());
    					}
    				}
    			}
			}
			return newTile;
		} finally {
			dataFileReader.close();
			stream.close();
		}
	}

	@Override
	public void serialize (TileData<T> tile, OutputStream stream) throws IOException {
		Schema recordSchema = getRecordSchema();
		Schema tileSchema = getTileSchema();

		List<GenericRecord> bins = new ArrayList<GenericRecord>();

		for (T value: tile.getData()){
			if (value == null)continue;
			GenericRecord bin = new GenericData.Record(recordSchema);
			setValue(bin, value);
			bins.add(bin);
		}

		GenericRecord tileRecord = new GenericData.Record(tileSchema);
		TileIndex idx = tile.getDefinition();
		tileRecord.put("level", idx.getLevel());
		tileRecord.put("xIndex", idx.getX());
		tileRecord.put("yIndex", idx.getY());
		tileRecord.put("xBinCount", idx.getXBins());
		tileRecord.put("yBinCount", idx.getYBins());
		tileRecord.put("values", bins);
		tileRecord.put("default", null);
		tileRecord.put("meta", getTileMetaData(tile));

		DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(tileSchema);
		DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<GenericRecord>(datumWriter);
		try {
			dataFileWriter.setCodec(_compressionCodec);
			dataFileWriter.create(tileSchema, stream);
			dataFileWriter.append(tileRecord);
			dataFileWriter.close();
			stream.close();
		} catch (IOException e) {throw new RuntimeException("Error serializing",e);}	
	}
}
