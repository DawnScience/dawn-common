package org.dawnsci.jgoogleanalytics;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.HttpURLConnection;

public class HTTPGetMethodRequestTest extends TestCase {

	public void testRequest_Failure() throws Exception {
		MockHTTPGetMethod httpGetMethod = new MockHTTPGetMethod();
		httpGetMethod.setLoggingAdapter(new SystemOutLogger());
		httpGetMethod.request("http://www.daw3nsci.org");
		assertTrue(httpGetMethod.responseCode != HttpURLConnection.HTTP_OK);
	}

	private class MockHTTPGetMethod extends HTTPGetMethod {
		int responseCode = 0;

		protected int getResponseCode(HttpURLConnection urlConnection)
				throws IOException {
			responseCode = super.getResponseCode(urlConnection);
			return responseCode;
		}
	}
}
