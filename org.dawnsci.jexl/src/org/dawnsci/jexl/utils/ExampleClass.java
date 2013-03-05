package org.dawnsci.jexl.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

public class ExampleClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//Get the Dawn engine which can do dataset operations
		JexlEngine jexl = JexlUtils.getDawnJexlEngine();
		
		//Create some data (Datasets and lists)
		DoubleDataset x = DoubleDataset.ones(10);
		DoubleDataset y = DoubleDataset.ones(10);
		List<DoubleDataset> ls = new ArrayList<DoubleDataset>();
		ls.add(x);
		ls.add(y);
		
		//create a context with containing all your variables
		JexlContext context = new MapContext();
		context.set("x", x);
		context.set("y", y);
		context.set("ls", ls);
		context.set("PI", Math.PI);
		context.set("E", Math.E);
		
		//Create an expression to evaluate
		Expression ex = jexl.createExpression("x+y");
		System.out.println(ex.evaluate(context));
		
		//Use operators on datasets
		ex = jexl.createExpression("x/5");
		System.out.println(ex.evaluate(context));
		
		//Use operators on datasets again
		ex = jexl.createExpression("5/x");
		System.out.println(ex.evaluate(context));
		
		//create
		ex = jexl.createExpression("var cake");
		System.out.println(ex.evaluate(context));
		
		//Use
		ex = jexl.createExpression("cake = 50");
		ex.evaluate(context);
		
		//Use
		ex = jexl.createExpression("cake + x");
		System.out.println(ex.evaluate(context));
		
		//with functions
		ex = jexl.createExpression("dnp:sin(x)+dd:arange(10)");
		System.out.println(ex.evaluate(context));
		
		//create arrays
		ex = jexl.createExpression("var z = [1,3,4]");
		System.out.println(ex.evaluate(context));
		
		//use arrays
		ex = jexl.createExpression("ls[0] + PI");
		System.out.println(ex.evaluate(context));
		
		//use arrays to call functions
		ex = jexl.createExpression("var image");
		ex.evaluate(context);
		ex = jexl.createExpression("image = rd:rand([100,100])");
		System.out.println(ex.evaluate(context));
		
		ex = jexl.createExpression("im:sobelFilter(image)");
		System.out.println(ex.evaluate(context));
		
		//Then go crazy looping over lists
		ex = jexl.createExpression("for (l : ls) {x=x+l;}");
		System.out.println(ex.evaluate(context));
	}
		
}
