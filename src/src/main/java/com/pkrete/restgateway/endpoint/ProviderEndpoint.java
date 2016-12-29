package com.pkrete.restgateway.endpoint;

/**
 * This class represents provider endpoint and holds all the information that is
 * needed for connecting to it.
 *
 * @author Petteri Kivimäki
 */
public class ProviderEndpoint extends AbstractEndpoint {

    private String url;
    private String accept;
    private String contentType;
    private boolean attachment;
    private boolean sendXrdHeaders;
    private String reqParamNameFilterCondition;
    private String reqParamNameFilterOperation;
    private String reqParamValueFilterCondition;
    private String reqParamValueFilterOperation;

    /**
     * Constructs and initializes a new ProviderEndpoint object.
     *
     * @param serviceId unique ID of the service
     * @param url URL of the Adapter Service
     */
    public ProviderEndpoint(String serviceId, String url) {
        super(serviceId);
        this.url = url;
        this.sendXrdHeaders = true;
        this.attachment = false;
    }

    /**
     * Returns the URL of the Adapter Service.
     *
     * @return URL of the Adapter Service
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the Adapter Service.
     *
     * @param url new value
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the value of the accept header that's used in the request.
     *
     * @return value of the accept header that's used in the request
     */
    public String getAccept() {
        return accept;
    }

    /**
     * Sets the value of the accept header that's used in the request.
     *
     * @param accept new value
     */
    public void setAccept(String accept) {
        this.accept = accept;
    }

    /**
     * Returns the value of the content type header that's used in the request.
     *
     * @return value of the content type header that's used in the request
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the value of the content type header that's used in the request.
     *
     * @param contentType new value
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Returns a boolean value that tells if X-Road specific HTTP headers should
     * be sent with the request.
     *
     * @return true if and only if headers must be sent; otherwise false
     */
    public boolean isSendXrdHeaders() {
        return sendXrdHeaders;
    }

    /**
     * Sets the boolean value that tells if X-Road specific HTTP headers should
     * be sent with the request.
     *
     * @param sendXrdHeaders new value
     */
    public void setSendXrdHeaders(boolean sendXrdHeaders) {
        this.sendXrdHeaders = sendXrdHeaders;
    }

    /**
     * Returns true if and only if the response data is passed as SOAP
     * attachment. Otherwise false.
     *
     * @return true if and only if the response data is passed as SOAP
     * attachment; otherwise false
     */
    public boolean isAttachment() {
        return attachment;
    }

    /**
     * Sets the boolean value that defines if the response data is passed as
     * SOAP attachment
     *
     * @param attachment new value
     */
    public void setAttachment(boolean attachment) {
        this.attachment = attachment;
    }

    /**
     * Returns the value of the request parameter name filter condition that's
     * used for modifying request parameter names. Filtering is done using
     * regex.
     *
     * @return value of the request parameter name filter condition
     */
    public String getReqParamNameFilterCondition() {
        return this.reqParamNameFilterCondition;
    }

    /**
     * Sets the value of the request parameter name filter condition that's used
     * for modifying request parameter names. Filtering is done using regex.
     *
     * @param reqParamNameFilterCondition new value
     */
    public void setReqParamNameFilterCondition(String reqParamNameFilterCondition) {
        this.reqParamNameFilterCondition = reqParamNameFilterCondition;
    }

    /**
     * Returns the value of the request parameter name filter operation that's
     * used for modifying request parameter names. Filtering is done using
     * regex. Operation is executed if and only if request parameter name
     * matches the condition.
     *
     * @return value of the request parameter name filter operation
     */
    public String getReqParamNameFilterOperation() {
        return this.reqParamNameFilterOperation;
    }

    /**
     * Sets the value of the request parameter name filter operation that's used
     * for modifying request parameter names. Filtering is done using regex.
     * Operation is executed if and only if request parameter name matches the
     * condition.
     *
     * @param reqParamNameFilterOperation new value
     */
    public void setReqParamNameFilterOperation(String reqParamNameFilterOperation) {
        this.reqParamNameFilterOperation = reqParamNameFilterOperation;
    }

    /**
     * Returns the value of the request parameter value filter condition that's
     * used for modifying request parameter values. Filtering is done using
     * regex.
     *
     * @return value of the request parameter value filter condition
     */
    public String getReqParamValueFilterCondition() {
        return reqParamValueFilterCondition;
    }

    /**
     * Sets the value of the request parameter value filter condition that's
     * used for modifying request parameter values. Filtering is done using
     * regex.
     *
     * @param reqParamValueFilterCondition new value
     */
    public void setReqParamValueFilterCondition(String reqParamValueFilterCondition) {
        this.reqParamValueFilterCondition = reqParamValueFilterCondition;
    }

    /**
     * Returns the value of the request parameter value filter operation that's
     * used for modifying request parameter values. Filtering is done using
     * regex. Operation is executed if and only if request parameter value
     * matches the condition.
     *
     * @return value of the request parameter value filter operation
     */
    public String getReqParamValueFilterOperation() {
        return this.reqParamValueFilterOperation;
    }

    /**
     * Sets the value of the request parameter value filter operation that's
     * used for modifying request parameter values. Filtering is done using
     * regex. Operation is executed if and only if request parameter value
     * matches the condition.
     *
     * @param reqParamValueFilterOperation new value
     */
    public void setReqParamValueFilterOperation(String reqParamValueFilterOperation) {
        this.reqParamValueFilterOperation = reqParamValueFilterOperation;
    }
}
