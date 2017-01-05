package org.dawnsci.processing.docgenerator;

import java.io.PrintWriter;
import java.util.Collection;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.OperationCategory;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.ModelField;
import org.eclipse.dawnsci.analysis.api.processing.model.ModelUtils;

public class ProcessingDocGenerator {

	private static IOperationService service;
	
	public static void setOperationService(IOperationService s) {
		service = s;
	}
	
	public ProcessingDocGenerator() {
		
	}
	
	public static void writeProcessingDoc(String path) {
		
		PrintWriter writer = null;
		try {
			
			writer = new PrintWriter(path, "UTF-8");
			
			writer.println("<html>");
			writer.println("<body>");
			
			IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.dawnsci.analysis.api.operation");
			for (IConfigurationElement e : eles) {
				final String     op = e.getAttribute("id");
				final String     vis = e.getAttribute("visible");
				if (op == null) continue;
				if (e.getName().equals("category")) continue;
				boolean isVisible = vis==null ? true : Boolean.parseBoolean(vis);
				if (!isVisible) continue;
//				System.out.println(op);
				IOperation<?, ?> ob = service.create(op);
				
				writer.print("<h2>");
				
				writer.print(service.getName(op) + " ");
				OperationCategory category = service.getCategory(op);
				if (category != null) {
					writer.print("[" + category.getName()+ "]");
				}
				
				writer.print("</h2>");
				
				writer.println();
				writer.println("<h3>" + getXYorImage(ob.getInputRank()) + " : " + getXYorImage(ob.getOutputRank()) + "</h3>");
				
				IOperationModel model = service.getModelClass(op).newInstance();
				
				Collection<ModelField>  col = ModelUtils.getModelFields(model);
				writer.println();
				writer.println("<p>" + service.getDescription(op) + "</p>");
				writer.println();
				writer.println("<h4>" + "Options"+ "</h4>");
				for (ModelField f : col) {
					writer.println("<p>" + f.getDisplayName() + " : " + ((f.getAnnotation() != null && f.getAnnotation().hint() != null) ? f.getAnnotation().hint() : "" + "</p>"));
				}

				writer.println();
				writer.println();
				
			}
		
			writer.println("</body>");
		writer.println("</html>");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (writer != null) writer.close();
		}
	}
	
	public static String getXYorImage(OperationRank rank) {
		switch (rank) {
		case ONE:
			return "XY";
		case TWO:
			return "Image";
		case ANY:
			return "Any";
		case SAME:
			return "Same";
		default:
			break;
		}
		
		return "";
	}
}
