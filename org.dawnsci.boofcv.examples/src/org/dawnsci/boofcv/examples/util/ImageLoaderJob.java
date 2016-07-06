package org.dawnsci.boofcv.examples.util;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.january.dataset.IDataset;

public class ImageLoaderJob extends Job {
	private List<IDataset> data;
	private List<String> filenames;

	public ImageLoaderJob(List<String> filenames) {
		super("Image loading");
		this.filenames = filenames;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (monitor != null)
			monitor.worked(1);
		// load data
		data = Utils.getImageDatasets(filenames, null);
		for (int i = 0; i < data.size(); i++) {
			String tmp = filenames.get(i);
			String name = tmp.substring(tmp.lastIndexOf("/") + 1);
			data.get(i).setName(name);
		}
		if (monitor != null)
			monitor.worked(1);
		return Status.OK_STATUS;
	}

	public List<IDataset> getData() {
		return data;
	}
}
