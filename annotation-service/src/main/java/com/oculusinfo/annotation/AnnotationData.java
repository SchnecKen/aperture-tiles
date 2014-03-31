/*
 * Copyright (c) 2014 Oculus Info Inc. http://www.oculusinfo.com/
 * 
 * Released under the MIT License.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oculusinfo.annotation;

import java.io.Serializable;

import org.json.JSONObject;

public abstract class AnnotationData implements Serializable {
    
	private static final long serialVersionUID = 1L;
	
	public abstract Double getX();
	public abstract Double getY();
	public abstract String getPriority();
	public abstract <T> T getData();
	public abstract Long getIndex();

	public JSONObject toJSON() {
		try {
			JSONObject json = new JSONObject();
			if ( getX() != null) json.put( "x", getX() );
			if ( getY() != null) json.put( "y", getY() );
			json.put("priority", getPriority() );
			json.put("data", getData() );
			return json;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;	
	}
	
	@Override
    public int hashCode () {
    	return getIndex().intValue();
    }

    @Override
    public boolean equals (Object that) {   	    	
    	if (that != null)
    	{
    		if (that instanceof AnnotationData) {
    			AnnotationData o = (AnnotationData)that;
    			return getIndex().equals( o.getIndex() ) &&
    				   getX() == o.getX() &&
    				   getY() == o.getY() &&
    				   getData().equals( o.getData() );
    		}    		
    	}	
    	return false;
    }
}
