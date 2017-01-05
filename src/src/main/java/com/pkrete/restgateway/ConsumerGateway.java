package com.pkrete.restgateway;

import com.pkrete.xrd4j.client.SOAPClient;
import com.pkrete.xrd4j.client.SOAPClientImpl;
import com.pkrete.xrd4j.client.deserializer.AbstractResponseDeserializer;
import com.pkrete.xrd4j.client.deserializer.ServiceResponseDeserializer;
import com.pkrete.xrd4j.client.serializer.AbstractServiceRequestSerializer;
import com.pkrete.xrd4j.client.serializer.ServiceRequestSerializer;
import com.pkrete.xrd4j.common.message.ErrorMessage;
import com.pkrete.xrd4j.common.message.ServiceRequest;
import com.pkrete.xrd4j.common.message.ServiceResponse;
import com.pkrete.xrd4j.common.util.MessageHelper;
import com.pkrete.xrd4j.common.util.PropertiesUtil;
import com.pkrete.xrd4j.common.util.SOAPHelper;
import com.pkrete.xrd4j.rest.converter.XMLToJSONConverter;
import com.pkrete.restgateway.endpoint.ConsumerEndpoint;
import com.pkrete.restgateway.util.Constants;
import com.pkrete.restgateway.util.ConsumerGatewayUtil;
import com.pkrete.restgateway.util.RESTGatewayUtil;
import com.pkrete.xrd4j.common.exception.XRd4JException;
import com.pkrete.xrd4j.common.security.Decrypter;
import com.pkrete.xrd4j.common.security.Encrypter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a Servlet which functionality can be configured through
 * external properties files. This class implements a REST consumer gateway by
 * forwarding incoming requests to configured X-Road security server, and
 * returning the responses to the requesters. Requests and responses can be
 * converted from JSON to XML.
 *
 * @author Petteri Kivimäki
 */
public class ConsumerGateway extends HttpServlet {

    private Properties props;
    private Map<String, ConsumerEndpoint> endpoints;
    private static final Logger logger = LoggerFactory.getLogger(ConsumerGateway.class);
    private boolean serviceCallsByXRdServiceId;
    private Decrypter asymmetricDecrypter;
    private final Map<String, Encrypter> asymmetricEncrypterCache = new HashMap<>();
    private String publicKeyFile;
    private String publicKeyFilePassword;
    private int keyLength;

    @Override
    public void init() throws ServletException {
        super.init();
        logger.debug("Starting to initialize Consumer REST Gateway.");
        logger.debug("Reading Consumer and ConsumerGateway properties");
        String propertiesDirectoryParameter = System.getProperty(Constants.PROPERTIES_DIR_PARAM_NAME);
        Properties endpointProps;
        if (propertiesDirectoryParameter != null) {
            endpointProps = PropertiesUtil.getInstance().load(propertiesDirectoryParameter + Constants.PROPERTIES_FILE_CONSUMERS, false);
            this.props = PropertiesUtil.getInstance().load(propertiesDirectoryParameter + Constants.PROPERTIES_FILE_CONSUMER_GATEWAY, false);
        } else {
            endpointProps = PropertiesUtil.getInstance().load(Constants.PROPERTIES_FILE_CONSUMERS);
            this.props = PropertiesUtil.getInstance().load(Constants.PROPERTIES_FILE_CONSUMER_GATEWAY);
        }
        logger.debug("Setting Consumer and ConsumerGateway properties");
        String serviceCallsByXRdServiceIdStr = this.props.getProperty(Constants.CONSUMER_PROPS_SVC_CALLS_BY_XRD_SVC_ID_ENABLED);
        this.serviceCallsByXRdServiceId = serviceCallsByXRdServiceIdStr == null ? false : "true".equalsIgnoreCase(serviceCallsByXRdServiceIdStr);
        logger.debug("Security server URL : \"{}\".", this.props.getProperty(Constants.CONSUMER_PROPS_SECURITY_SERVER_URL));
        logger.debug("Default client id : \"{}\".", this.props.getProperty(Constants.CONSUMER_PROPS_ID_CLIENT));
        logger.debug("Default namespace for incoming ServiceResponses : \"{}\".", this.props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE));
        logger.debug("Default namespace for outgoing ServiceRequests : \"{}\".", this.props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE));
        logger.debug("Default namespace prefix for outgoing ServiceRequests : \"{}\".", this.props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE));
        logger.debug("Service calls by X-Road service id are enabled : {}.", this.serviceCallsByXRdServiceId);
        this.publicKeyFile = props.getProperty(Constants.ENCRYPTION_PROPS_PUBLIC_KEY_FILE);
        this.publicKeyFilePassword = props.getProperty(Constants.ENCRYPTION_PROPS_PUBLIC_KEY_FILE_PASSWORD);
        this.keyLength = RESTGatewayUtil.getKeyLength(props);
        logger.debug("Symmetric key length : \"{}\".", this.keyLength);
        logger.debug("Extracting individual consumers from properties");
        this.endpoints = ConsumerGatewayUtil.extractConsumers(endpointProps, this.props);
        // Check encryption properties. The method also sets the values of
        // asymmetricEncrypterCache.
        if (ConsumerGatewayUtil.checkEncryptionProperties(props, endpoints, this.asymmetricEncrypterCache)) {
            this.asymmetricDecrypter = RESTGatewayUtil.checkPrivateKey(props);
        }
    }

    /**
     * Processes requests for HTTP <code>GET</code>, <code>POST</code>,
     * <code>PUT</code> and <code>DELETE</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String responseStr;
        // Get resourcePath attribute
        String resourcePath = (String) request.getAttribute("resourcePath");
        // Get HTTP headers
        String userId = processUserId(this.getXRdHeader(request, Constants.XRD_HEADER_USER_ID));
        String messageId = processMessageId(this.getXRdHeader(request, Constants.XRD_HEADER_MESSAGE_ID));
        String namespace = this.getXRdHeader(request, Constants.XRD_HEADER_NAMESPACE_SERIALIZE);
        String prefix = this.getXRdHeader(request, Constants.XRD_HEADER_NAMESPACE_PREFIX_SERIALIZE);
        String contentType = request.getHeader(Constants.HTTP_HEADER_CONTENT_TYPE);
        String acceptHeader = this.getXRdHeader(request, Constants.HTTP_HEADER_ACCEPT) == null ? Constants.TEXT_XML : this.getXRdHeader(request, Constants.HTTP_HEADER_ACCEPT);
        logger.info("Request received. Method : \"{}\". Resource path : \"{}\".", request.getMethod(), resourcePath);

        // Check accept header
        String accept = processAcceptHeader(acceptHeader);
        // Set reponse content type according the accept header
        response.setContentType(accept);

        // Omit response namespace, if response is wanted in JSON
        boolean omitNamespace = accept.startsWith(Constants.APPLICATION_JSON);

        // Set userId and messageId to response
        response.addHeader(Constants.XRD_HEADER_USER_ID, userId);
        response.addHeader(Constants.XRD_HEADER_MESSAGE_ID, messageId);

        if (resourcePath == null) {
            // No resource path was defined -> return 404
            responseStr = this.generateError(Constants.ERROR_404, accept);
            response.setStatus(404);
            // Send response
            this.writeResponse(response, responseStr);
            // Quit processing
            return;
        }

        // Build the service id for the incoming request
        String serviceId = request.getMethod() + " " + resourcePath;
        logger.debug("Incoming service id to be looked for : \"{}\"", serviceId);
        // Try to find a configured endpoint matching the request's
        // service id
        ConsumerEndpoint endpoint = ConsumerGatewayUtil.findMatch(serviceId, endpoints);

        // If endpoint is null, try to use resourcePath as service id
        if (endpoint == null) {
            if (this.serviceCallsByXRdServiceId) {
                logger.info("Endpoint is null, use resource path as service id. Resource path : \"{}\"", resourcePath);
                endpoint = ConsumerGatewayUtil.createUnconfiguredEndpoint(this.props, resourcePath);
            } else {
                logger.info("Endpoint is null and service calls by X-Road service id are disabled. Nothing to do here.");
            }
        }
        // If endpoint is still null, return error message
        if (endpoint == null) {
            // No endpoint was found -> return 404
            responseStr = this.generateError(Constants.ERROR_404, accept);
            response.setStatus(404);
            // Send response
            this.writeResponse(response, responseStr);
            // Quit processing
            return;
        }

        // Set namespace and prefix received from header, if not null or empty
        processNamespaceAndPrefix(endpoint, namespace, prefix);

        logger.info("Starting to process \"{}\" service. X-Road id : \"{}\". Message id : \"{}\".", serviceId, endpoint.getServiceId(), messageId);
        try {
            // Create ServiceRequest object
            ServiceRequest<Map<String, String[]>> serviceRequest = new ServiceRequest<>(endpoint.getConsumer(), endpoint.getProducer(), messageId);
            // Set userId
            serviceRequest.setUserId(userId);
            // Set HTTP request parameters as request data
            serviceRequest.setRequestData(this.filterRequestParameters(request.getParameterMap()));
            // Set request wrapper processing
            if (endpoint.isProcessingWrappers() != null) {
                serviceRequest.setProcessingWrappers(endpoint.isProcessingWrappers());
            }
            // Serializer that converts the request to SOAP
            ServiceRequestSerializer serializer = getRequestSerializer(endpoint, this.readRequestBody(request), contentType);
            // Deserializer that converts the response from SOAP to XML/JSON
            ServiceResponseDeserializer deserializer = getResponseDeserializer(endpoint, omitNamespace);
            // SOAP client that makes the service call
            SOAPClient client = new SOAPClientImpl();
            logger.info("Send request ({}) to the security server. URL : \"{}\".", messageId, props.getProperty(Constants.CONSUMER_PROPS_SECURITY_SERVER_URL));
            // Make the service call that returns the service response
            ServiceResponse serviceResponse = client.send(serviceRequest, props.getProperty(Constants.CONSUMER_PROPS_SECURITY_SERVER_URL), serializer, deserializer);
            logger.info("Received response ({}) from the security server.", messageId);
            // Set response wrapper processing
            if (endpoint.isProcessingWrappers() != null) {
                serviceResponse.setProcessingWrappers(endpoint.isProcessingWrappers());
            }
            // Generate response message
            responseStr = handleResponse(response, serviceResponse);

            // Check if the URLs in the response should be rewritten
            // to point this servlet
            if (endpoint.isModifyUrl()) {
                // Get ConsumerGateway URL
                String servletUrl = this.getServletUrl(request);
                // Modify the response
                responseStr = ConsumerGatewayUtil.rewriteUrl(servletUrl, resourcePath, responseStr);
            }
            logger.info("Processing \"{}\" service successfully completed. X-Road id : \"{}\". Message id : \"{}\".", serviceId, endpoint.getServiceId(), messageId);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            logger.error("Processing \"{}\" service failed. X-Road id : \"{}\". Message id : \"{}\".", serviceId, endpoint.getServiceId(), messageId);
            // Internal server error -> return 500
            responseStr = this.generateError(Constants.ERROR_500, accept);
            response.setStatus(500);
        }

        // Send response
        this.writeResponse(response, responseStr);
    }

    /**
     * Returns a new ServiceRequestSerializer that converts the request to SOAP.
     * The implementation of the ServiceRequestSerializer is decided based on
     * the given parameters.
     *
     * @param endpoint ConsumerEndpoint that's processed using the serializer
     * @param requestBody request body that's being processed
     * @param contentType content type of the request
     * @return new ServiceRequestSerializer object
     * @throws XRd4JException
     */
    private ServiceRequestSerializer getRequestSerializer(ConsumerEndpoint endpoint, String requestBody, String contentType) throws XRd4JException {
        // Type of the serializer depends on the encryption
        if (endpoint.isRequestEncrypted()) {
            logger.debug("Endpoint requires that request is encrypted.");
            String providerId = endpoint.getProducer().toString();
            Encrypter asymmetricEncrypter;
            // Check if encrypter already exists in cache - it should as all
            // the encrypters are loaded during start up
            if (this.asymmetricEncrypterCache.containsKey(providerId)) {
                asymmetricEncrypter = RESTGatewayUtil.getEncrypter(this.publicKeyFile, this.publicKeyFilePassword, providerId);
                logger.trace("Asymmetric encrypter for provider \"{}\" loaded from cache.", providerId);
            } else {
                // Create new encrypter if it does not exist already for some reason
                asymmetricEncrypter = RESTGatewayUtil.getEncrypter(this.publicKeyFile, this.publicKeyFilePassword, providerId);
                // Add new encrypter to the cache
                this.asymmetricEncrypterCache.put(providerId, asymmetricEncrypter);
                logger.trace("Asymmetric encrypter for provider \"{}\" not found from cache. New ecrypter created.", providerId);
            }
            if (asymmetricEncrypter == null) {
                throw new XRd4JException("No public key found when encryption is required.");
            }
            return new EncryptingRequestSerializer(endpoint.getResourceId(), requestBody, contentType, asymmetricEncrypter, this.keyLength);
        } else {
            return new RequestSerializer(endpoint.getResourceId(), requestBody, contentType);
        }
    }

    /**
     * Returns a new ServiceResponseDeserializer that converts the response from
     * SOAP to XML/JSON. The implementation of the ServiceResponseDeserializer
     * is decided based on the given parameters.
     *
     * @param endpoint ConsumerEndpoint that's processed using the deserializer
     * @param omitNamespace boolean value that tells if the response namespace
     * should be omitted by the deserializer
     * @return new ServiceResponseDeserializer object
     * @throws XRd4JException
     */
    private ServiceResponseDeserializer getResponseDeserializer(ConsumerEndpoint endpoint, boolean omitNamespace) throws XRd4JException {
        // Type of the serializer depends on the encryption
        if (endpoint.isResponseEncrypted()) {
            // If asymmetric decrypter is null, there's nothing to do
            if (this.asymmetricDecrypter == null) {
                throw new XRd4JException("No private key available when decryption is required.");
            }
            return new EncryptingResponseDeserializer(omitNamespace, this.asymmetricDecrypter);
        } else {
            // Deserializer that converts the response from SOAP to XML/JSON string
            return new ResponseDeserializer(omitNamespace);
        }
    }

    /**
     * Returns "anonymous" if the given user id is null. Otherwise returns the
     * given user id.
     *
     * @param userId user id to be checked
     * @return "anonymous" if the given user id is null; otherwise userId
     */
    private String processUserId(String userId) {
        // Set userId if null
        if (userId == null) {
            logger.debug("\"{}\" header is null. Use \"anonymous\" as userId.", Constants.XRD_HEADER_USER_ID);
            return "anonymous";
        }
        return userId;
    }

    /**
     * Generates a unique identifier if the given message id is null. Otherwise
     * returns the given message id.
     *
     * @param messageId message id to be checked
     * @return unique identifier if the given message id is null; otherwise
     * messageId
     */
    private String processMessageId(String messageId) {
        // Set messageId if null
        if (messageId == null) {
            String id = MessageHelper.generateId();
            logger.debug("\"{}\" header is null. Use auto-generated id \"{}\" instead.", Constants.XRD_HEADER_MESSAGE_ID, id);
            return id;
        }
        return messageId;

    }

    /**
     * Checks namespace and prefix for null and empty, and sets them to endpoint
     * if a value is found.
     *
     * @param endpoint ConsumerEndpoint object
     * @param namespace namespace HTTP header String
     * @param prefix prefix HTTP header String
     */
    private void processNamespaceAndPrefix(ConsumerEndpoint endpoint, String namespace, String prefix) {
        // Set namespace received from header, if not null or empty
        if (!RESTGatewayUtil.isNullOrEmpty(namespace)) {
            endpoint.getProducer().setNamespaceUrl(namespace);
            logger.debug("\"{}\" HTTP header found. Value : \"{}\".", Constants.XRD_HEADER_NAMESPACE_SERIALIZE, namespace);
        }
        // Set prefix received from header, if not null or empty
        if (!RESTGatewayUtil.isNullOrEmpty(prefix)) {
            endpoint.getProducer().setNamespacePrefix(prefix);
            logger.debug("\"{}\" HTTP header found. Value : \"{}\".", Constants.XRD_HEADER_NAMESPACE_PREFIX_SERIALIZE, prefix);
        }
    }

    /**
     * Checks the value of the accept header and sets it to "text/xml" if no
     * valid value is found. UTF8 character set definition is added if missing.
     *
     * @param accept HTTP Accept header value from the request
     * @return sanitized Accept header value
     */
    private String processAcceptHeader(String accept) {
        // Accept header must be "text/xml" or "application/json"
        logger.debug("Incoming accept header value : \"{}\"", accept);
        if (!accept.startsWith(Constants.TEXT_XML) && !accept.startsWith(Constants.APPLICATION_JSON)) {
            logger.trace("Accept header value set to \"{}\".", Constants.TEXT_XML);
            return Constants.TEXT_XML + "; " + Constants.CHARSET_UTF8;
        }
        // Character set must be added to the accept header, if it's missing
        if (!accept.endsWith("8")) {
            return accept + "; " + Constants.CHARSET_UTF8;
        }
        return accept;
    }

    /**
     * Process the response and check it for error messages and attachments
     * etc., and generate the response message string.
     *
     * @param response HttpServletResponse object
     * @param serviceResponse ServiceResponse object
     * @return response message as a String
     */
    private String handleResponse(HttpServletResponse response, ServiceResponse serviceResponse) {
        String responseStr;
        // Check that response doesn't contain SOAP fault
        if (!serviceResponse.hasError()) {
            // Get the response that's now XML string. If the response has
            // attachments, the first attachment is returned. In case
            // of attachment, the response might not be XML. This is
            // handled later.
            responseStr = (String) serviceResponse.getResponseData();
        } else {
            // Error message detected
            logger.debug("Received response contains SOAP fault.");
            responseStr = this.generateFault(serviceResponse.getErrorMessage());
        }
        // SOAP message doesn't have attachments
        if (!SOAPHelper.hasAttachments(serviceResponse.getSoapMessage())) {
            // Convert the response according to content type and remove
            // response tag if possible
            responseStr = handleResponseBody(response, responseStr);
        } else {
            // SOAP message has attachments. Use attachment's
            // content type.
            String attContentType = SOAPHelper.getAttachmentContentType(serviceResponse.getSoapMessage());
            response.setContentType(attContentType);
            logger.debug("Use SOAP attachment as response message.");
        }
        return responseStr;
    }

    /**
     * Checks the content type and tries to convert the response accordingly. In
     * addition, the method tries to remove the response tag and its namespace
     * prefixes from the response string.
     *
     * @param response HttpServletResponse object
     * @param responseStr response message as a String
     * @return modified response message as a String
     */
    private String handleResponseBody(HttpServletResponse response, String responseStr) {
        // If content type is JSON and the SOAP message doesn't have
        // attachments, the response must be converted
        if (response.getContentType().startsWith(Constants.APPLICATION_JSON)) {
            logger.debug("Convert response from XML to JSON.");
            // Remove response tag and its namespace prefixes 
            String tmp = ConsumerGatewayUtil.removeResponseTag(responseStr);
            return new XMLToJSONConverter().convert(tmp);
        } else if (response.getContentType().startsWith(Constants.TEXT_XML)) {
            // Remove response tag and its namespace prefixes
            String responseStrTemp = ConsumerGatewayUtil.removeResponseTag(responseStr);
            // Try to convert modified response to SOAP element
            if (SOAPHelper.xmlStrToSOAPElement(responseStrTemp) != null) {
                logger.debug("Response tag was removed from the response string.");
                // If conversion succeeded response tag was only
                // a wrapper that can be removed
                return responseStrTemp;
            } else {
                logger.debug("Response tag is the root element and cannot be removed.");
            }
        }
        return responseStr;
    }

    /**
     * Sends the response to the requester.
     *
     * @param response HttpServletResponse object
     * @param responseStr response payload as a String
     */
    private void writeResponse(HttpServletResponse response, String responseStr) {
        PrintWriter out = null;
        try {
            logger.debug("Send response.");

            logger.debug("Response content type : \"{}\".", response.getContentType());
            // Get writer
            out = response.getWriter();
            // Send response
            out.println(responseStr);
            logger.trace("Consumer Gateway response : \"{}\"", responseStr);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (out != null) {
                out.close();
            }
            logger.debug("Request was successfully processed.");
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>PUT</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>DELETE</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private String generateError(String errorMsg, String contentType) {
        StringBuilder builder = new StringBuilder();
        if (contentType.startsWith(Constants.APPLICATION_JSON)) {
            builder.append("{\"error\":\"").append(errorMsg).append("\"}");
        } else {
            builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            builder.append("<error>").append(errorMsg).append("</error>");
        }
        return builder.toString();
    }

    private String generateFault(ErrorMessage err) {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<error>");
        builder.append("<code>").append(err.getFaultCode()).append("</code>");
        builder.append("<string>").append(err.getFaultString()).append("</string>");
        if (err.getFaultActor() != null) {
            builder.append("<actor>").append(err.getFaultActor()).append("</actor>");
        } else {
            builder.append("<actor/>");
        }
        if (err.getDetail() != null) {
            builder.append("<detail>").append(err.getDetail()).append("</detail>");
        } else {
            builder.append("<detail/>");
        }
        builder.append("</error>");
        return builder.toString();
    }

    /**
     * Return the URL of this servlet.
     *
     * @param request HTTP servlet request
     * @return URL of this servlet
     */
    private String getServletUrl(HttpServletRequest request) {
        return request.getScheme() + "://"
                + // "http" + "://
                request.getServerName()
                + // "myhost"
                ":"
                + // ":"
                request.getServerPort()
                + // "8080"
                request.getContextPath()
                + "/Consumer/";
    }

    /**
     * Checks if URL parameter (1) or HTTP header (2) with the given name exists
     * and returns its value. If no URL parameter or HTTP header is found, null
     * is returned. URL parameters are the primary source, and HTTP headers
     * secondary source.
     *
     * @param request HTTP request
     * @param header name of the header
     * @return value of the header
     */
    private String getXRdHeader(HttpServletRequest request, String header) {
        String headerValue = request.getParameter(header);
        if (headerValue != null && !headerValue.isEmpty()) {
            return headerValue;
        }
        return request.getHeader(header);
    }

    /**
     * Removes all the X-Road specific HTTP and SOAP headers from the request
     * parameters map. This method must be called before writing the parameters
     * to the SOAP request object.
     *
     * @param parameters HTTP request parameters map
     * @return filtered parameters map
     */
    private Map filterRequestParameters(Map<String, String[]> parameters) {
        // Request parameters map is unmodifiable so we need to copy it
        Map<String, String[]> params = new HashMap<>(parameters);
        // Remove X-Road headers
        params.remove(Constants.XRD_HEADER_USER_ID);
        params.remove(Constants.XRD_HEADER_MESSAGE_ID);
        params.remove(Constants.XRD_HEADER_NAMESPACE_SERIALIZE);
        params.remove(Constants.XRD_HEADER_NAMESPACE_PREFIX_SERIALIZE);
        params.remove(Constants.HTTP_HEADER_ACCEPT);
        // Return copied parameters Map
        return params;
    }

    /**
     * Reads the request body from the request and returns it as a String.
     *
     * @param request HttpServletRequest that contains the request body
     * @return request body as a String or null
     */
    private String readRequestBody(HttpServletRequest request) {
        try {
            // Read from request
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (Exception e) {
            logger.error("Failed to read the request body from the request.", e);
        }
        return null;
    }

    /**
     * Serializes GET, POST, PUT and DELETE requests to SOAP.
     */
    private class RequestSerializer extends AbstractServiceRequestSerializer {

        protected final String resourceId;
        protected final String requestBody;
        protected final String contentType;

        public RequestSerializer(String resourceId, String requestBody, String contentType) {
            this.resourceId = resourceId;
            this.requestBody = requestBody;
            this.contentType = contentType;
            logger.debug("New RequestSerializer created.");
        }

        @Override
        protected void serializeRequest(ServiceRequest request, SOAPElement soapRequest, SOAPEnvelope envelope) throws SOAPException {
            handleBody(request, soapRequest);
            if (this.requestBody != null && !this.requestBody.isEmpty()) {
                handleAttachment(request, soapRequest, envelope, this.requestBody);
            }
        }

        protected void handleBody(ServiceRequest request, SOAPElement soapRequest) throws SOAPException {
            if (this.resourceId != null && !this.resourceId.isEmpty()) {
                logger.debug("Add resourceId : \"{}\".", this.resourceId);
                soapRequest.addChildElement("resourceId").addTextNode(this.resourceId);
            }
            Map<String, String[]> params = (Map<String, String[]>) request.getRequestData();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                String key = entry.getKey();
                String[] arr = entry.getValue();
                for (String value : arr) {
                    logger.debug("Add parameter : \"{}\" -> \"{}\".", key, value);
                    soapRequest.addChildElement(key).addTextNode(value);
                }
            }
        }

        protected void handleAttachment(ServiceRequest request, SOAPElement soapRequest, SOAPEnvelope envelope, String attachmentData) throws SOAPException {
            logger.debug("Request body was found from the request. Add request body as SOAP attachment. Content type is \"{}\".", this.contentType);
            SOAPElement data = soapRequest.addChildElement(envelope.createName(Constants.PARAM_REQUEST_BODY));
            data.addAttribute(envelope.createName("href"), Constants.PARAM_REQUEST_BODY);
            AttachmentPart attachPart = request.getSoapMessage().createAttachmentPart(attachmentData, this.contentType);
            attachPart.setContentId(Constants.PARAM_REQUEST_BODY);
            request.getSoapMessage().addAttachmentPart(attachPart);

        }
    }

    private class EncryptingRequestSerializer extends RequestSerializer {

        private final Encrypter asymmetricEncrypter;
        private final int keyLength;

        public EncryptingRequestSerializer(String resourceId, String requestBody, String contentType, Encrypter asymmetricEncrypter, int keyLength) {
            super(resourceId, requestBody, contentType);
            this.asymmetricEncrypter = asymmetricEncrypter;
            this.keyLength = keyLength;
            logger.debug("New EncryptingRequestSerializer created.");
        }

        @Override
        protected void serializeRequest(ServiceRequest request, SOAPElement soapRequest, SOAPEnvelope envelope) throws SOAPException {
            SOAPElement payload = SOAPHelper.xmlStrToSOAPElement("<" + Constants.PARAM_ENCRYPTION_WRAPPER + "/>");
            try {
                // Create new symmetric encrypter using of defined key length
                Encrypter symmetricEncrypter = RESTGatewayUtil.createSymmetricEncrypter(this.keyLength);
                // Process request parameters
                handleBody(request, payload);
                // Process request body 
                if (this.requestBody != null && !this.requestBody.isEmpty()) {
                    handleAttachment(request, payload, envelope, symmetricEncrypter.encrypt(this.requestBody));
                }
                // Encrypt message with symmetric AES encryption
                String encryptedData = symmetricEncrypter.encrypt(SOAPHelper.toString(payload));
                // Build message body that includes enrypted data,
                // encrypted session key and IV
                RESTGatewayUtil.buildEncryptedBody(symmetricEncrypter, asymmetricEncrypter, soapRequest, encryptedData);
            } catch (NoSuchAlgorithmException ex) {
                logger.error(ex.getMessage(), ex);
                throw new SOAPException("Encrypting SOAP request failed.", ex);
            }
        }
    }

    /**
     * Deserializes SOAP responses to String.
     */
    private class ResponseDeserializer extends AbstractResponseDeserializer<Map, String> {

        protected boolean omitNamespace;

        public ResponseDeserializer(boolean omitNamespace) {
            this.omitNamespace = omitNamespace;
        }

        @Override
        protected Map deserializeRequestData(Node requestNode) throws SOAPException {
            return null;
        }

        @Override
        protected String deserializeResponseData(Node responseNode, SOAPMessage message) throws SOAPException {
            // Remove namespace if it's required
            handleNamespace(responseNode);

            // If message has attachments, return the first attachment
            if (message.countAttachments() > 0) {
                logger.debug("SOAP attachment detected. Use attachment as response data.");
                return SOAPHelper.toString((AttachmentPart) message.getAttachments().next());
            }
            // Convert response to string
            return SOAPHelper.toString(responseNode);
        }

        protected void handleNamespace(Node responseNode) {
            if (this.omitNamespace) {
                logger.debug("Remove namespaces from response.");
                SOAPHelper.removeNamespace(responseNode);
            }
        }
    }

    /**
     * Deserializes SOAP responses to String.
     */
    private class EncryptingResponseDeserializer extends ResponseDeserializer {

        private final Decrypter asymmetricDecrypter;

        public EncryptingResponseDeserializer(boolean omitNamespace, Decrypter asymmetricDecrypter) {
            super(omitNamespace);
            this.asymmetricDecrypter = asymmetricDecrypter;
            logger.debug("New EncryptingResponseDeserializer created.");
        }

        @Override
        protected Map deserializeRequestData(Node requestNode) throws SOAPException {
            return null;
        }

        @Override
        protected String deserializeResponseData(Node responseNode, SOAPMessage message) throws SOAPException {
            // Put all the response nodes to map so that we can easily access them
            Map<String, String> nodes = SOAPHelper.nodesToMap(responseNode.getChildNodes());

            /**
             * N.B.! If signature is present (B: encrypt then sign) and we want
             * to verify it before going ahead with processing, this is the
             * place to do it. The signed part of the message is accessed:
             * nodes.get(Constants.PARAM_ENCRYPTED)
             */
            // Decrypt session key using the private key
            Decrypter symmetricDecrypter = RESTGatewayUtil.getSymmetricDecrypter(this.asymmetricDecrypter, nodes.get(Constants.PARAM_KEY), nodes.get(Constants.PARAM_IV));

            // If message has attachments, return the first attachment
            if (message.countAttachments() > 0) {
                logger.debug("SOAP attachment detected. Use attachment as response data.");
                // Return decrypted data
                return symmetricDecrypter.decrypt(SOAPHelper.toString((AttachmentPart) message.getAttachments().next()));
            }
            /**
             * N.B.! If signature is present (A: sign then encrypt) and we want
             * to verify it before going ahead with processing, this is the
             * place to do it. The signed part of the message is accessed:
             * symmetricDecrypter.decrypt(nodes.get(Constants.PARAM_ENCRYPTED))
             * UTF-8 String wrapper must left out from signature verification.
             */

            // Decrypt the response. UTF-8 characters are not showing correctly
            // if we don't wrap the decrypted data into a new string and
            // explicitly tell that it's UTF-8 even if encrypter and
            // decrypter handle strings as UTF-8.
            String decryptedResponse = new String(symmetricDecrypter.decrypt(nodes.get(Constants.PARAM_ENCRYPTED)).getBytes(StandardCharsets.UTF_8));
            // Convert encrypted response to SOAP
            SOAPElement encryptionWrapper = SOAPHelper.xmlStrToSOAPElement(decryptedResponse);
            // Remove all the children under response node
            SOAPHelper.removeAllChildren(responseNode);
            // Remove the extra <encryptionWrapper> element between response node
            // and the actual response. After the modification all the response
            // elements are directly under response.
            SOAPHelper.moveChildren(encryptionWrapper, (SOAPElement) responseNode, !this.omitNamespace);
            // Clone response node because removing namespace from the original 
            // node causes null pointer exception in AbstractResponseDeserializer 
            // when wrappers are not used. Cloning the original node, removing
            // namespace from the clone and returning the clone prevents the
            // problem to occur.
            Node modifiedResponseNode = (Node) responseNode.cloneNode(true);
            // Remove namespace if it's required
            handleNamespace(modifiedResponseNode);
            // Return the response
            return SOAPHelper.toString(modifiedResponseNode);
        }
    }
}
