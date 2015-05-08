package org.dawnsci.jgoogleanalytics;

public class SystemOutLogger implements LoggingAdapter {

	public void logError(String errorMessage) {
		System.out.println("errorMessage = " + errorMessage);
	}

	public void logMessage(String message) {
		System.out.println("message = " + message);
	}
}
