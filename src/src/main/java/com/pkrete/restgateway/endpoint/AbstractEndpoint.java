package com.pkrete.restgateway.endpoint;

/**
 * This abstract class is a base class for ProviderEndpoint and ConsumerEndpoint
 * classes.
 *
 * @author Petteri Kivimäki
 */
public abstract class AbstractEndpoint {

    private String serviceId;
    private String httpVerb;
    private String namespaceSerialize;
    private String namespaceDeserialize;
    private String prefix;
    private Boolean processingWrappers;
    private boolean requestEncrypted;
    private boolean responseEncrypted;

    /**
     * Constructs and initializes a new AbstractEndpoint object.
     *
     * @param serviceId unique ID of the service
     */
    public AbstractEndpoint(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Returns the unique ID of the service.
     *
     * @return unique ID of the service
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Sets the unique ID of the service.
     *
     * @param serviceId new value
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * Returns the HTTP verb that is used in the request.
     *
     * @return HTTP verb that is used in the request
     */
    public String getHttpVerb() {
        return httpVerb;
    }

    /**
     * Sets the HTTP verb that is used in the request.
     *
     * @param httpVerb new value
     */
    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    /**
     * Returns the request namespace of this Endpoint.
     *
     * @return request namespace of this Endpoint
     */
    public String getNamespaceDeserialize() {
        return namespaceDeserialize;
    }

    /**
     * Sets the request namespace of this Endpoint.
     *
     * @param namespaceDeserialize new value
     */
    public void setNamespaceDeserialize(String namespaceDeserialize) {
        this.namespaceDeserialize = namespaceDeserialize;
    }

    /**
     * Returns the response namespace of this Endpoint.
     *
     * @return response namespace of this Endpoint
     */
    public String getNamespaceSerialize() {
        return namespaceSerialize;
    }

    /**
     * Sets the response namespace of this Endpoint.
     *
     * @param namespaceSerialize new value
     */
    public void setNamespaceSerialize(String namespaceSerialize) {
        this.namespaceSerialize = namespaceSerialize;
    }

    /**
     * Returns the response namespace prefix of this Endpoint.
     *
     * @return response namespace prefix of this Endpoint
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the response namespace prefix of this Endpoint.
     *
     * @param prefix new value
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return True/False indicating whether <response> and <request> tags are
     * expected in incoming and outgoing SOAP message bodies, or a null value
     * indicating no setting.
     */
    public Boolean isProcessingWrappers() {
        return this.processingWrappers;
    }

    /**
     * set whether <response> and <request> tags are expected in incoming and
     * outgoing SOAP message bodies.
     *
     * @param processingWrappers
     */
    public void setProcessingWrappers(Boolean processingWrappers) {
        this.processingWrappers = processingWrappers;
    }

    /**
     * Returns true if and only if the request data is encrypted.
     *
     * @return true if and only if the request data is encrypted
     */
    public boolean isRequestEncrypted() {
        return requestEncrypted;
    }

    /**
     * Sets the value that tells is the request data is encrypted.
     *
     * @param requestEncrypted new value
     */
    public void setRequestEncrypted(boolean requestEncrypted) {
        this.requestEncrypted = requestEncrypted;
    }

    /**
     * Returns true if and only if the response data is encrypted.
     *
     * @return true if and only if the response data is encrypted
     */
    public boolean isResponseEncrypted() {
        return responseEncrypted;
    }

    /**
     * Sets the value that tells is the response data is encrypted.
     *
     * @param responseEncrypted new value
     */
    public void setResponseEncrypted(boolean responseEncrypted) {
        this.responseEncrypted = responseEncrypted;
    }

}
