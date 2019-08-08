package at.looksy.service.task;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import at.looksy.BuildConfig;

import android.os.AsyncTask;
import android.util.Log;
import at.looksy.core.Constants;
import at.looksy.service.consumer.IWebServiceAsyncConsumer;
import at.looksy.service.data.WebServiceRequest;
import at.looksy.service.data.WebServiceResponse;
import at.looksy.service.data.WebServiceResponse.StatusCode;


public class WebServiceConnectionTask extends AsyncTask<WebServiceRequest, Integer, WebServiceResponse> {
	
	public WebServiceConnectionTask() {}
	public WebServiceConnectionTask(IWebServiceAsyncConsumer consumer) {
		this.consumer = consumer;
	}

	private IWebServiceAsyncConsumer consumer = null;
	private WebServiceRequest wsRequest = null;
	private int MAX_RETRIES = 3;

	@Override
	protected void onPreExecute() {			
		super.onPreExecute();
	}

	@Override
	protected WebServiceResponse doInBackground(WebServiceRequest... datas) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = null;
		WebServiceResponse wsResponse = null;
		boolean connected = false;
		
		wsRequest = datas[0];
		
		String url = Constants.WEB_SERVICE_BASE_URI + "index.php";
		HttpPost request = new HttpPost(url);
		request.addHeader(Constants.WEB_SERVICE_HEADER_CONTENT_TYPE, 
				Constants.WEB_SERVICE_HEADER_FORM);

		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>(3);
			params.add(new BasicNameValuePair(
					Constants.WEB_SERVICE_MODULE, 
					Constants.WEB_SERVICE_MODULE_API));
			params.add(new BasicNameValuePair(
					Constants.WEB_SERVICE_ACTION, 
					wsRequest.getRequestAction()));
			params.add(new BasicNameValuePair(
					Constants.WEB_SERVICE_DATA, 
					wsRequest.getRequestData().toString()));
			
			request.setEntity(new UrlEncodedFormEntity(params));
			
			if (BuildConfig.DEBUG)
				Log.d(Constants.DEBUG_TAG, 
						"Request: " + url + "?" + 
								new Scanner(request.getEntity().getContent()).
								useDelimiter("\\A").next());
			
		} catch (Exception e) { e.printStackTrace(); }
		
		for (int i = 0; i < MAX_RETRIES; i++) {

			try {		
				// connect and retrieve response
				response = httpClient.execute(request);

				// act on response
				if (response != null) {
					connected = true;

					// set up the input stream and readers
					InputStream is = response.getEntity().getContent();
					BufferedReader rs = new BufferedReader(new InputStreamReader(is));
					StringBuffer responseStringBuffer = new StringBuffer();

					// fetch JSON data
					String line = rs.readLine();
					if (BuildConfig.DEBUG)
						Log.d(Constants.DEBUG_TAG, "HTTP response line: '" + line + "'");
					responseStringBuffer.append(line);
					rs.close();
					wsResponse = new WebServiceResponse(responseStringBuffer.toString());
					if (wsResponse.getString(Constants.JSON_STATUS).
							equals(Constants.JSON_STATUS_OK))
						wsResponse.setStatus(StatusCode.OK);
					else
						wsResponse.setStatus(StatusCode.ERROR);

				} else {
					if (BuildConfig.DEBUG)
						Log.d(Constants.DEBUG_TAG, "No response from remote host");
				}

			} catch (Exception ex) {
				if (BuildConfig.DEBUG) {
					Log.w(Constants.DEBUG_TAG, "Exception generated while establishing connection: " + 
							ex.toString() + "\n" + ex.getStackTrace().toString());
				}

			} finally {
				if (connected) break;
			}
		}
		
		return wsResponse;
	}

	@Override
	protected void onPostExecute(WebServiceResponse wsResponse) {
		super.onPostExecute(wsResponse);
		if (consumer != null)
			consumer.consumeWSExchange(wsRequest, wsResponse);
	}
}
