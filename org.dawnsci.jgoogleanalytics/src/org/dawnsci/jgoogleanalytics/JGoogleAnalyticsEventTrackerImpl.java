package org.dawnsci.jgoogleanalytics;

import org.dawb.common.util.eclipse.BundleUtils;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.EventTracker;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
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

	private String version;

	static {
		System.out.println("Starting JGoogleAnalytics Event tracker service.");
	}

	private JGoogleAnalyticsTracker tracker;
	private FocusPoint focusPoint;

	public JGoogleAnalyticsEventTrackerImpl() {
		// Important do nothing here, OSGI may start the service more than once.
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
		ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.common.ui");
		String sIsEnabled = store.getString(BasePlottingConstants.IS_TRACKER_ENABLED);
		boolean isTrackingEnabled = Boolean.valueOf(sIsEnabled);
		if (isTrackingEnabled) {
			// replace all spaces by underscores
			name = name.trim().replaceAll("\\s+", "_");
			// get beamline name if any otherwise replace by ext/
			String beamline = System.getenv("BEAMLINE");
			if (beamline == null || beamline.equals(""))
				beamline = "ext";
			name = beamline + '/' + name;
			if (focusPoint != null) {
				String currentName = focusPoint.getName();
				if (!currentName.equals(name)) {
					focusPoint = new FocusPoint(name);
				}
			} else {
				focusPoint = new FocusPoint(name);
			}
			if (tracker == null) {
				String code = store.getString(BasePlottingConstants.ANALYTICS_TRACK_CODE);
				tracker = new JGoogleAnalyticsTracker(APP_NAME, getVersion(), code);
			}
			if (tracker.getLoggingAdapter() == null)
				tracker.setLoggingAdapter(LoggerLoader.LOGGING_INSTANCE);

			if (focusPoint == null)
				throw new Exception("A unique name must be set");
			tracker.trackAsynchronously(focusPoint);
		}
	}

	@Override
	public void track(String id, String label) throws Exception {
		track(label + "[" + id + "]");
	}

	@Override
	public void trackToolEvent(String name) throws Exception {
		if (version == null)
			version = BundleUtils.getDawnVersion();
		track("Tool/" + name);
	}

	@Override
	public void trackPerspectiveEvent(String name) throws Exception {
		if (version == null)
			version = BundleUtils.getDawnVersion();
		track("Perspective/" + name);
	}

	@Override
	public void trackActionEvent(String name) throws Exception {
		track("Action/" + name);
	}

	private String getVersion() {
		if (version == null)
			version = BundleUtils.getDawnVersion();
		if (version != null) {
			// get only the first 5 characters of the version ie 1.9.0
			version = version.substring(0, 5);
		}
		return version;
	}
}
