package at.looksy.service.consumer;

import at.looksy.service.data.WebServiceRequest;
import at.looksy.service.data.WebServiceResponse;

public interface IWebServiceAsyncConsumer {
	
	/**
	 * Handler method for a web service exchange
	 * @param wsRequest Request sent to the web service
	 * @param wsResponse Response received by this device
	 */
	public void consumeWSExchange(WebServiceRequest wsRequest, 
			WebServiceResponse wsResponse);
	
	/**
	 * Dictates whether the caller should receive a callback after
	 * the web service has received a response. A caller could
	 * send a request and go away, thus not wanting to consume
	 * the response. Use this method to indicate when we should
	 * receive a callback and when we should not.
	 * @return True if caller should receive callback, else false
	 */
	public boolean receiveWSCallback();

}