package org.dawnsci.jgoogleanalytics;

import org.dawb.common.util.eclipse.BundleUtils;
import org.eclipse.dawnsci.analysis.api.IAnalyticsTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to wrap JGoogleAnalyticsTracker, the creation of FocusPoint and
 * Logging Adapter instances.
 * 
 * @author Baha El Kassaby
 * 
 */
public class AnalyticsTracker implements IAnalyticsTracker {

	public static final Logger logger = LoggerFactory.getLogger(AnalyticsTracker.class);

	static {
		System.out.println("Starting Analytics service.");
	}

	private JGoogleAnalyticsTracker tracker;
	private FocusPoint focusPoint;

	public AnalyticsTracker() {
		// Important do nothing here, OSGI may start the service more than once.
	}

	//thread lazy initialisation of the instance without explicit synchronisation
	private static class TrackerLoader {
		static JGoogleAnalyticsTracker ANALYTICS_INSTANCE = new JGoogleAnalyticsTracker("DAWN", BundleUtils.getDawnVersion(), "UA-48311061-3");
	}

	private static class LoggerLoader {
		static LoggingAdapter LOGGING_INSTANCE = new LoggingAdapter() {
			@Override
			public void logError(String s) {
				logger.error(s);
			}

			@Override
			public void logMessage(String s) {
				logger.info(s);
			}
		};
	}

	@Override
	public void track(String name, boolean isAsynchronous) throws Exception {
		if (focusPoint != null) {
			String currentName = focusPoint.getName();
			if (!currentName.equals(name)) {
				focusPoint = new FocusPoint(name);
			}
		} else {
			focusPoint = new FocusPoint(name);
		}
		if (tracker == null)
			tracker = TrackerLoader.ANALYTICS_INSTANCE;
		if (tracker.getLoggingAdapter() == null)
			tracker.setLoggingAdapter(LoggerLoader.LOGGING_INSTANCE);

		if (focusPoint == null)
			throw new Exception("A unique name must be set");
		if(isAsynchronous)
			tracker.trackAsynchronously(focusPoint);
		else
			tracker.trackSynchronously(focusPoint);
	}
}
