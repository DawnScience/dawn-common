package org.dawnsci.persistence.roi;

import java.lang.reflect.Type;


import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ROISerializer implements JsonSerializer<ROIBean> {

	@Override
	public JsonElement serialize(ROIBean src, Type typeOfSrc,
			JsonSerializationContext context) {
//		if(src instanceof RectangularROIBean){
//			return new JsonPrimitive(((RectangularROIBean)src).toString());
//		}
		return new JsonPrimitive(src.toString());
	}

	
}
