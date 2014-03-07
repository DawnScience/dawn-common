package org.dawnsci.persistence.test;

import static org.junit.Assert.*;

import org.dawnsci.persistence.json.FunctionBean;
import org.dawnsci.persistence.json.JacksonMarshaller;
import org.dawnsci.persistence.json.roi.RectangularROIBean;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Fermi;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;

/**
 * Test the marshallizer with Jackson
 * @author wqk87977
 *
 */
public class JacksonMarshallerTest {

	private double[] startPoint = new double[2];
	private double[] lengths = new double[2];
	private RectangularROIBean roibean;
	private FunctionBean functionbean;
	private String jsonroi;
	private String jsonfunction;
	private JacksonMarshaller jackMarshall;

	@Before
	public void setup(){
		jackMarshall = new JacksonMarshaller();

		startPoint [0] = 100;
		startPoint[1] = 100;
		lengths [0] = 400;
		lengths[1] = 200;

		roibean = new RectangularROIBean();
		roibean.setName("rectangle");
		roibean.setStartPoint(startPoint);
		roibean.setType("RectangularROI");
		roibean.setLengths(lengths);
		roibean.setAngle(0);
		jsonroi = "{\"type\":\"RectangularROI\"," +
				"\"name\":\"rectangle\"," +
				"\"startPoint\":[100.0,100.0]," +
				"\"lengths\":[400.0,200.0]," +
				"\"angle\":0.0," +
				"\"endPoint\":null}";

		functionbean = new FunctionBean();
//		functionbean.setType(FunctionBean.TYPE_FERMI);
		functionbean.setType(Fermi.class.getName());
		// create 4 parameters
		Parameter[] params = new Parameter[4];
		String[] names = new String[] {"parameter1", "parameter2", "parameter3", "parameter4"};
		for (int i = 0; i < params.length; i++) {
			params[i] = new Parameter();
			params[i].setName(names[i]);
			params[i].setValue(5 + i);
			params[i].setLowerLimit(0 + i);
			params[i].setUpperLimit(10 + i);
			params[i].setFixed(false);
		}
		functionbean.setParameters(params);
		jsonfunction = "{\"name\":null,\"type\":\"uk.ac.diamond.scisoft.analysis.fitting.functions.Fermi\",\"parameters\":[{\"name\":\"parameter1\",\"value\":5.0,\"upperLimit\":10.0,\"lowerLimit\":0.0,\"fixed\":false}," +
				"{\"name\":\"parameter2\",\"value\":6.0,\"upperLimit\":11.0,\"lowerLimit\":1.0,\"fixed\":false}," +
				"{\"name\":\"parameter3\",\"value\":7.0,\"upperLimit\":12.0,\"lowerLimit\":2.0,\"fixed\":false}," +
				"{\"name\":\"parameter4\",\"value\":8.0,\"upperLimit\":13.0,\"lowerLimit\":3.0,\"fixed\":false}]}";
	}

	@Test
	public void testMarshallFromROIBeanToJsonString() throws JsonProcessingException{
		String resultJSON = jackMarshall.marshal(roibean);
		assertEquals(jsonroi, resultJSON);
	}

	@Test
	public void testUnMarshallFromJSonStringToROIBean(){
		RectangularROIBean resultbean;
		try {
			resultbean = (RectangularROIBean) jackMarshall.unmarshal(jsonroi);

			assertEquals(roibean.getName(), resultbean.getName());
			assertEquals(roibean.getType(), resultbean.getType());
			assertArrayEquals(roibean.getLengths(), resultbean.getLengths(), 0);
			assertEquals(roibean.getAngle(), resultbean.getAngle(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testMarshallFromFunctionBeanToJSonString() throws JsonProcessingException{
		String resultJSON = jackMarshall.marshal(functionbean);
		assertEquals(jsonfunction, resultJSON);
	}

	@Test
	public void testUnMarshallFromJSonStringToFunctionBean(){
		FunctionBean resultbean;
		try {
			resultbean = (FunctionBean) jackMarshall.unmarshal(jsonfunction);
			assertEquals(functionbean.getType(), resultbean.getType());
			for (int i = 0; i < functionbean.getParameters().length; i++) {
				assertEquals(functionbean.getParameters()[i].getLowerLimit(), resultbean.getParameters()[i].getLowerLimit(), 0);
				assertEquals(functionbean.getParameters()[i].getUpperLimit(), resultbean.getParameters()[i].getUpperLimit(), 0);
				assertEquals(functionbean.getParameters()[i].getValue(), resultbean.getParameters()[i].getValue(), 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
