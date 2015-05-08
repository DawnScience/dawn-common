package org.dawnsci.jgoogleanalytics;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Simple class performing HTTP Get method on the requested url
 * 
 * @author : Siddique Hameed
 * @version : 0.1
 */

public class HTTPGetMethod {
	private static final String GET_METHOD_NAME = "GET";

	private static final String SUCCESS_MESSAGE = "JGoogleAnalytics: Tracking Successful!";

	private LoggingAdapter loggingAdapter = null;

	public void setLoggingAdapter(LoggingAdapter loggingAdapter) {
		this.loggingAdapter = loggingAdapter;
	}

	private static String uaName = null; // User Agent name

	private static String osString = "Unknown";

	HTTPGetMethod() {
		// Initialise the static parameters if we need to.
		if (uaName == null) {
			uaName = "Java/" + System.getProperty("java.version"); // java
																	// version
																	// info
																	// appended
			// os string is architecture+osname+version concatenated with _
			osString = System.getProperty("os.arch");
			if (osString == null || osString.length() < 1) {
				osString = "";
			} else {
				osString += "; ";
				osString += System.getProperty("os.name") + " "
						+ System.getProperty("os.version");
			}
		}
	}

	public void request(String urlString) {
		try {
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = openURLConnection(url);
			urlConnection.setInstanceFollowRedirects(true);
			urlConnection.setRequestMethod(GET_METHOD_NAME);
			urlConnection.setRequestProperty("User-agent", uaName + " ("
					+ osString + ")");

			urlConnection.connect();
			int responseCode = getResponseCode(urlConnection);
			if (responseCode != HttpURLConnection.HTTP_OK) {
				logError("JGoogleAnalytics: Error tracking, url=" + urlString);
			} else {
				logMessage(SUCCESS_MESSAGE);
			}
		} catch (Exception e) {
			logError(e.getMessage());
		}
	}

	protected int getResponseCode(HttpURLConnection urlConnection)
			throws IOException {
		return urlConnection.getResponseCode();
	}

	private HttpURLConnection openURLConnection(URL url) throws IOException {
		return (HttpURLConnection) url.openConnection();
	}

	private void logMessage(String message) {
		if (loggingAdapter != null) {
			loggingAdapter.logMessage(message);
		}
	}

	private void logError(String errorMesssage) {
		if (loggingAdapter != null) {
			loggingAdapter.logError(errorMesssage);
		}
	}
}
