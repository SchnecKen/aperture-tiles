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
package com.oculusinfo.tile.rest.tile;

import com.oculusinfo.binning.TileIndex;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.util.UUID;

public interface TileService {
	/**
	 * TMS tile request.
	 * 
	 * @param id - 'default' is ok - means use server defaults. Use getLayer (/layer) to obtain an id.
	 * @param layer - The layer for which to get an image
	 * @param index The index of the desired tile
	 * @param tileSet A set of other tiles which will be wanted along with this
	 *            one
	 * @return rendered image.
	 */
	public BufferedImage getTileImage (UUID id, String layer, TileIndex index, Iterable<TileIndex> tileSet, JSONObject query);

	/**
	 * TMS raw tile data request.
	 * 
	 * @param id - 'default' is ok - means use server defaults. Use getLayer
	 *            (/layer) to obtain an id.
	 * @param layer - The layer for which to get tile data
	 * @param index The index of the desired tile
	 * @param tileSet A set of other tiles which will be wanted along with this
	 *            one
	 * @return The raw data for the indicated tile
	 */
	public JSONObject getTileObject (UUID fromString, String layer, TileIndex index, Iterable<TileIndex> tileSet, JSONObject query);
}
