package org.dawb.common.ui.plot.tool;

import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.util.text.StringUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.Page;

/**
 * Page to extend for adding a tool to the plotting.
 * @author fcp94556
 *
 */
public abstract class AbstractToolPage extends Page implements IToolPage, IAdaptable {

	private IToolPageSystem toolSystem;
	private IPlottingSystem plotSystem;
	private IWorkbenchPart  part;
	private String          title;
	private String unique_id;
	private ImageDescriptor imageDescriptor;

	public AbstractToolPage() {
		this.unique_id = StringUtils.getUniqueId(AbstractToolPage.class);
	}

	@Override
	public void setPlottingSystem(IPlottingSystem system) {
		this.plotSystem = system;
	}

	@Override
	public IPlottingSystem getPlottingSystem() {
		return plotSystem;
	}

	@Override
	public IToolPageSystem getToolSystem() {
		return toolSystem;
	}
	@Override
	public void setToolSystem(IToolPageSystem system) {
		this.toolSystem = system;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	private boolean isActive = false;
	public boolean isActive() {
		return isActive;
	}
	
	/**
	 * Does nothing by default - optionally override.
	 */
	public void activate() {
		isActive = true;
	}

	/**
	 * Does nothing by default - optionally override.
	 */
	public void deactivate() {
		isActive = false;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((unique_id == null) ? 0 : unique_id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractToolPage other = (AbstractToolPage) obj;
		if (unique_id == null) {
			if (other.unique_id != null)
				return false;
		} else if (!unique_id.equals(other.unique_id))
			return false;
		return true;
	}

	public String toString(){
		if (getTitle()!=null) return getTitle();
		return super.toString();
	}

	@Override
	public IWorkbenchPart getPart() {
		return part;
	}

	public void setPart(IWorkbenchPart part) {
		this.part = part;
	}

	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

	public void setImageDescriptor(ImageDescriptor imageDescriptor) {
		this.imageDescriptor = imageDescriptor;
	}
	
	/**
	 * Default does nothing
	 */
	public Object getAdapter(Class key) {
		return null;
	}
}
