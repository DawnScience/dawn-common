package org.dawnsci.jgoogleanalytics;

import org.dawb.common.util.eclipse.BundleUtils;
import org.eclipse.dawnsci.analysis.api.EventTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to wrap JGoogleAnalyticsTracker, the creation of FocusPoint and
 * Logging Adapter instances.
 * 
 * @author Baha El Kassaby
 * 
 */
public class JGoogleAnalyticsEventTrackerImpl implements EventTracker {

	public static final Logger logger = LoggerFactory.getLogger(JGoogleAnalyticsEventTrackerImpl.class);

	private static final String APP_NAME = "DAWN";
	public static final String GOOGLE_TRACK_CODE = "UA-48311061-3";

	static {
		System.out.println("Starting JGoogleAnalytics Event tracker service.");
	}

	private JGoogleAnalyticsTracker tracker;
	private FocusPoint focusPoint;

	public JGoogleAnalyticsEventTrackerImpl() {
		// Important do nothing here, OSGI may start the service more than once.
	}

	//thread lazy initialisation of the instance without explicit synchronisation
	private static class TrackerLoader {
		static JGoogleAnalyticsTracker ANALYTICS_INSTANCE = new JGoogleAnalyticsTracker(APP_NAME, BundleUtils.getDawnVersion(), GOOGLE_TRACK_CODE);
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
	public void track(String name) throws Exception {
		// replace all spaces by underscores
		name = name.trim().replaceAll("\\s+", "_");
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
		tracker.trackAsynchronously(focusPoint);
	}

	@Override
	public void track(String id, String label) throws Exception {
		track(label + "[" + id + "]");
	}

	@Override
	public void trackToolEvent(String name) throws Exception {
		track(APP_NAME + "/" + BundleUtils.getDawnVersion() + "/Tool/" + name);
	}

	@Override
	public void trackPerspectiveEvent(String name) throws Exception {
		track(APP_NAME + "/" + BundleUtils.getDawnVersion() + "/Perspective/" + name);
	}

	@Override
	public void trackActionEvent(String name) throws Exception {
		track(APP_NAME + "/" + BundleUtils.getDawnVersion() + "/Action/" + name);
	}
}
