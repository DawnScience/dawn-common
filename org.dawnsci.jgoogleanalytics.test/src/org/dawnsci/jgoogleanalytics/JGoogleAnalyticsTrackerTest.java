package org.dawnsci.jgoogleanalytics;

import junit.framework.TestCase;

public class JGoogleAnalyticsTrackerTest extends TestCase {

	private FocusPoint parentFocusPoint = new FocusPoint("JGoogleAnalyticsTest");
	private FocusPoint syncChildFocuPoint = new FocusPoint(
			"TrackingSynchronously", parentFocusPoint);
	private FocusPoint asyncChildFocuPoint = new FocusPoint(
			"TrackingAsynchronously", parentFocusPoint);

	public void testTrackSynchronously_LibraryFinder() throws Exception {
		JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker(
				"JGoogleAnalytics", "v0.1", "UA-48311061-3");
		tracker.setLoggingAdapter(new SystemOutLogger());
		tracker.trackSynchronously(syncChildFocuPoint);
	}

	public void testTrackAsynchronously_LibraryFinder() throws Exception {
		JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker(
				"JGoogleAnalytics", "v0.1", "UA-48311061-3");
		tracker.setLoggingAdapter(new SystemOutLogger());
		tracker.trackAsynchronously(asyncChildFocuPoint);
		Thread.sleep(3000);
	}

}
