package org.dawnsci.persistence.roi;

import java.lang.reflect.Type;

import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.ROIBean;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class ROIDeserializer implements JsonDeserializer<ROIBean> {

	@Override
	public ROIBean deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		return new ROIBean();//json.getAsJsonPrimitive().getAsString());
	}
}
