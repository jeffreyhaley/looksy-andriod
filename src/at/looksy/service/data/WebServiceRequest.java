package at.looksy.service.data;

import org.json.JSONException;
import org.json.JSONObject;

public class WebServiceRequest extends JSONObject {
	
	private String requestAction;
	private JSONObject requestData;
	
	public WebServiceRequest() {
		super();
	}
	
	public WebServiceRequest(String str) throws JSONException {
		super(str);
	}

	public String getRequestAction() {
		return requestAction;
	}

	public void setRequestAction(String requestAction) {
		this.requestAction = requestAction;
	}

	public JSONObject getRequestData() {
		return requestData;
	}

	public void setRequestData(JSONObject requestData) {
		this.requestData = requestData;
	}

	
}
