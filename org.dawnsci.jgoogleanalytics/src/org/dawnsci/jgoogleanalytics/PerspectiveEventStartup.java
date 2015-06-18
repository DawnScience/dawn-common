package org.dawnsci.jgoogleanalytics;

import org.eclipse.dawnsci.analysis.api.EventTracker;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;

/**
 * Class used to monitor perspectives usage
 *
 */
public class PerspectiveEventStartup extends PerspectiveAdapter implements IStartup {

	private EventTracker tracker;

	@Override
	public void earlyStartup() {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length;) {
			windows[i].addPerspectiveListener(this);
			break;
		}
	}

	@Override
	public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		String perspectiveName = perspective.getLabel();
		if (tracker == null)
			tracker = new JGoogleAnalyticsEventTrackerImpl();
		try {
			if (!perspectiveName.equals(""))
				tracker.trackPerspectiveEvent(perspectiveName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
