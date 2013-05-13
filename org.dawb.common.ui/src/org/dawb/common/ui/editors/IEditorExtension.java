package org.dawb.common.ui.editors;

import org.dawnsci.plotting.api.IPlottingContainer;
import org.eclipse.ui.IEditorPart;

/**
 * This class should be used to define additional editors which may be added to
 * multi-editor parts for the Image and HDF5 editors.
 * 
 * The part will be added at the start of the editor stack. To support
 * other locations, a method getPreferredEditorIndex() could be implemented here.
 * 
 * @author fcp94556
 *
 */
public interface IEditorExtension extends IEditorPart, IPlottingContainer {

	/**
	 * Ensure that this method returns false as quickly as possible without parsing the data.
	 * 
	 * If returning true (for instance checking a nexus file) 
	 * 
	 * @param filePath
	 * @param extension - from the filePath, can be used to check quickly if you support
	 *                    the extension. For instance: 
	 *                    <code>
	 *                    if (!".nxs".equalsIgnoreCase(extension)) return false;
	 *                    </code>
	 *                    This hard coded approach is simple and understandable from
	 *                    the code, therefore preferred.
	 *  @param perspectiveId - the current perspective id, or the last one that this part was
	 *                         used with if the editor is opening during startup. This string MAY
	 *                         be null.
	 * NOTE when using IEditorExtension your editor part must have a title. For instance:
	 * 
	
	<code>
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName("Fred");		
	}
	</code>
	
	 *                    
	 * @return true if part should be shown.
	 */
	public boolean isApplicable(final String filePath, final String extension, final String perspectiveId);
	
	/**
	 * The preferred editor index in the multi-editor stack.
	 * @return int preferred index.
	 */
	// TODO
	//public int getPreferredEditorIndex();
}
