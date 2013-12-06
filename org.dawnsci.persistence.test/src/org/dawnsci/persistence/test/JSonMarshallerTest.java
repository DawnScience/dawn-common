package org.dawnsci.persistence.test;

import static org.junit.Assert.*;

import org.dawnsci.persistence.json.JacksonMarshaller;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;
import uk.ac.diamond.scisoft.analysis.persistence.bean.function.FunctionBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.RectangularROIBean;

/**
 * Test the marshallizer with Jackson
 * @author wqk87977
 *
 */
public class JSonMarshallerTest {

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
		functionbean.setType(FunctionBean.TYPE_FERMI);
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
		jsonfunction = "{\"name\":null,\"type\":2,\"parameters\":[{\"name\":\"parameter1\",\"value\":5.0,\"upperLimit\":10.0,\"lowerLimit\":0.0,\"fixed\":false}," +
				"{\"name\":\"parameter2\",\"value\":6.0,\"upperLimit\":11.0,\"lowerLimit\":1.0,\"fixed\":false}," +
				"{\"name\":\"parameter3\",\"value\":7.0,\"upperLimit\":12.0,\"lowerLimit\":2.0,\"fixed\":false}," +
				"{\"name\":\"parameter4\",\"value\":8.0,\"upperLimit\":13.0,\"lowerLimit\":3.0,\"fixed\":false}]}";
	}

	@Test
	public void testMarshallFromROIBeanToJsonString(){
		String resultJSON = jackMarshall.marshallFromROIBean(roibean);
		assertEquals(jsonroi, resultJSON);
	}

	@Test
	public void testUnMarshallFromJSonStringToROIBean(){
		RectangularROIBean resultbean = (RectangularROIBean) jackMarshall.unmarshallToROIBean(jsonroi);
		assertEquals(roibean.getName(), resultbean.getName());
		assertEquals(roibean.getType(), resultbean.getType());
		assertArrayEquals(roibean.getLengths(), resultbean.getLengths(), 0);
		assertEquals(roibean.getAngle(), resultbean.getAngle(), 0);
	}

	@Test
	public void testMarshallFromFunctionBeanToJSonString(){
		String resultJSON = jackMarshall.marshallFromFunctionBean(functionbean);
		assertEquals(jsonfunction, resultJSON);
	}

	@Test
	public void testUnMarshallFromJSonStringToFunctionBean(){
		FunctionBean resultbean = jackMarshall.unmarshallToFunctionBean(jsonfunction);
		assertEquals(functionbean.getType(), resultbean.getType());
		for (int i = 0; i < functionbean.getParameters().length; i++) {
			assertEquals(functionbean.getParameters()[i].getLowerLimit(), resultbean.getParameters()[i].getLowerLimit(), 0);
			assertEquals(functionbean.getParameters()[i].getUpperLimit(), resultbean.getParameters()[i].getUpperLimit(), 0);
			assertEquals(functionbean.getParameters()[i].getValue(), resultbean.getParameters()[i].getValue(), 0);
		}

	}
}
