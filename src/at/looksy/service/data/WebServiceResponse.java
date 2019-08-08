package at.looksy.service.data;

import org.json.JSONException;
import org.json.JSONObject;

public class WebServiceResponse extends JSONObject {
	
	public static enum StatusCode {
		OK,
		ERROR,
		WARN
	}
	
	private StatusCode status;
	
	public WebServiceResponse() {}
	
	public WebServiceResponse(String str) throws JSONException {
		super(str);
	}

	public StatusCode getStatus() {
		return status;
	}

	public void setStatus(StatusCode statusCode) {
		this.status = statusCode;
	}
	
}
