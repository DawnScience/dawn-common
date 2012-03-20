package org.dawb.common.ui.plot.annotation;


/**
 * No methods in this interface are thread safe.
 * 
 * @author fcp94556
 *
 */
public interface IAnnotationSystem {

	
	/**
	 * Creates an annotation. This does not create any user interface
	 * for the annotation. You can then call methods on the annoation.
	 * Use addAnnotation(...) and removeAnnotation(...) to control
	 * if the selection is active on the graph.
	 * 
	 * @param name
	 * @param regionType
	 * @return
	 * @throws Exception if name exists already.
	 */
	public IAnnotation createAnnotation(final String name) throws Exception;
	
	/**
	 * Add an annotation to the graph.
	 * @param region
	 */
	public void addAnnotation(final IAnnotation region);
	
	
	/**
	 * Remove an annotation to the graph.
	 * @param region
	 */
	public void removeAnnotation(final IAnnotation region);
	
	/**
	 * Get an annotation by name.
	 * @param name
	 * @return
	 */
	public IAnnotation getAnnotation(final String name);

	/**
	 * Remove all annotations
	 */
	public void clearAnnotations();

}
