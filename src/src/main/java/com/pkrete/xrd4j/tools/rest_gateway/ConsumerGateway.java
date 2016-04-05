package com.pkrete.xrd4j.tools.rest_gateway;

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
import com.pkrete.xrd4j.tools.rest_gateway.endpoint.ConsumerEndpoint;
import com.pkrete.xrd4j.tools.rest_gateway.util.Constants;
import com.pkrete.xrd4j.tools.rest_gateway.util.ConsumerGatewayUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
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
    private final static Logger logger = LoggerFactory.getLogger(ConsumerGateway.class);
    private boolean serviceCallsByXRdServiceId;

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
        this.serviceCallsByXRdServiceId = serviceCallsByXRdServiceIdStr == null ? false : serviceCallsByXRdServiceIdStr.equalsIgnoreCase("true");
        logger.debug("Security server URL : \"{}\".", this.props.getProperty(Constants.CONSUMER_PROPS_SECURITY_SERVER_URL));
        logger.debug("Default client id : \"{}\".", this.props.getProperty(Constants.CONSUMER_PROPS_ID_CLIENT));
        logger.debug("Default namespace for incoming ServiceResponses : \"{}\".", this.props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE));
        logger.debug("Default namespace for outgoing ServiceRequests : \"{}\".", this.props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE));
        logger.debug("Default namespace prefix for outgoing ServiceRequests : \"{}\".", this.props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE));
        logger.debug("Service calls by X-Road service id are enabled : {}.", this.serviceCallsByXRdServiceId);
        logger.debug("Extracting individual consumers from properties");
        this.endpoints = ConsumerGatewayUtil.extractConsumers(endpointProps, this.props);
        logger.debug("Consumer REST Gateway initialized.");
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
        boolean omitNamespace = false;
        // Get resourcePath attribute
        String resourcePath = (String) request.getAttribute("resourcePath");
        // Get HTTP headers
        String userId = this.getXRdHeader(request, Constants.XRD_HEADER_USER_ID);
        String messageId = this.getXRdHeader(request, Constants.XRD_HEADER_MESSAGE_ID);
        String namespace = this.getXRdHeader(request, Constants.XRD_HEADER_NAMESPACE_SERIALIZE);
        String prefix = this.getXRdHeader(request, Constants.XRD_HEADER_NAMESPACE_PREFIX_SERIALIZE);
        String contentType = request.getHeader(Constants.HTTP_HEADER_CONTENT_TYPE);
        String accept = this.getXRdHeader(request, Constants.HTTP_HEADER_ACCEPT) == null ? Constants.TEXT_XML : this.getXRdHeader(request, Constants.HTTP_HEADER_ACCEPT);
        logger.info("Request received. Method : \"{}\". Resource path : \"{}\".", request.getMethod(), resourcePath);

        // Accept header must be "text/xml" or "application/json"
        logger.debug("Incoming accept header value : \"{}\"", accept);
        if (!accept.startsWith(Constants.TEXT_XML) && !accept.startsWith(Constants.APPLICATION_JSON)) {
            accept = Constants.TEXT_XML + "; " + Constants.CHARSET_UTF8;
            logger.trace("Accept header value set to \"{}\".", Constants.TEXT_XML);
        }
        // Character set must be added to the accept header, if it's missing
        if (!accept.endsWith("8")) {
            accept += "; " + Constants.CHARSET_UTF8;
        }
        // Set reponse content type according the accept header
        response.setContentType(accept);

        // Set userId if null
        if (userId == null) {
            logger.debug("\"{}\" header is null. Use \"anonymous\" as userId.", Constants.XRD_HEADER_USER_ID);
            userId = "anonymous";
        }
        // Set messageId if null
        if (messageId == null) {
            messageId = MessageHelper.generateId();
            logger.debug("\"{}\" header is null. Use auto-generated id \"{}\" instead.", Constants.XRD_HEADER_MESSAGE_ID, messageId);
        }

        // Omit response namespace, if response is wanted in JSON
        if (accept.startsWith(Constants.APPLICATION_JSON)) {
            omitNamespace = true;
        }

        // Set userId and messageId to response
        response.addHeader(Constants.XRD_HEADER_USER_ID, userId);
        response.addHeader(Constants.XRD_HEADER_MESSAGE_ID, messageId);

        if (resourcePath != null) {
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

            // If endpoint was found, process it; otherwise return an error
            if (endpoint != null) {
                // Set namespace received from header, if not null or empty
                if (namespace != null && !namespace.isEmpty()) {
                    endpoint.getProducer().setNamespaceUrl(namespace);
                    logger.debug("\"{}\" HTTP header found. Value : \"{}\".", Constants.XRD_HEADER_NAMESPACE_SERIALIZE, namespace);
                }
                // Set prefix received from header, if not null or empty
                if (prefix != null && !prefix.isEmpty()) {
                    endpoint.getProducer().setNamespacePrefix(prefix);
                    logger.debug("\"{}\" HTTP header found. Value : \"{}\".", Constants.XRD_HEADER_NAMESPACE_PREFIX_SERIALIZE, prefix);
                }
                logger.info("Starting to process \"{}\" service. X-Road id : \"{}\". Message id : \"{}\".", serviceId, endpoint.getServiceId(), messageId);
                try {
                    // Create ServiceRequest object
                    ServiceRequest<Map<String, String[]>> serviceRequest = new ServiceRequest<Map<String, String[]>>(endpoint.getConsumer(), endpoint.getProducer(), messageId);
                    // Set userId
                    serviceRequest.setUserId(userId);
                    // Set HTTP request parameters as request data
                    serviceRequest.setRequestData(this.filterRequestParameters(request.getParameterMap()));
                    // String get request body
                    String requestBody = this.readRequestBody(request);
                    // Serializer that converts the request to SOAP
                    ServiceRequestSerializer serializer = new RequestSerializer(endpoint.getResourceId(), requestBody, contentType);
                    // Deserializer that converts the response from SOAP to XML string
                    ServiceResponseDeserializer deserializer = new ResponseDeserializer(omitNamespace);
                    // SOAP client that makes the service call
                    SOAPClient client = new SOAPClientImpl();
                    logger.info("Send request ({}) to the security server. URL : \"{}\".", messageId, props.getProperty(Constants.CONSUMER_PROPS_SECURITY_SERVER_URL));
                    // Make the service call that returns the service response
                    ServiceResponse serviceResponse = client.send(serviceRequest, props.getProperty(Constants.CONSUMER_PROPS_SECURITY_SERVER_URL), serializer, deserializer);
                    logger.info("Received response ({}) from the security server.", messageId);
                    // Check that response doesn't contain SOAP fault
                    if (!serviceResponse.hasError()) {
                        // Get the response that's now XML string
                        responseStr = (String) serviceResponse.getResponseData();
                    } else {
                        // Error message detected
                        logger.debug("Received response contains SOAP fault.");
                        responseStr = this.generateFault(serviceResponse.getErrorMessage());
                    }
                    // SOAP message doesn't have attachments
                    if (!SOAPHelper.hasAttachments(serviceResponse.getSoapMessage())) {
                        // If content type is JSON and the SOAP message doesn't have
                        // attachments, the response must be converted
                        if (response.getContentType().startsWith(Constants.APPLICATION_JSON)) {
                            logger.debug("Convert response from XML to JSON.");
                            // Remove <response> tags. Namespaces are omitted
                            // when content type of response is JSON
                            responseStr = responseStr.replaceAll("<(/)*response>", "");
                            responseStr = new XMLToJSONConverter().convert(responseStr);
                        } else if (response.getContentType().startsWith(Constants.TEXT_XML)) {
                            // Remove response tag and its namespace prefixes
                            String responseStrTemp = ConsumerGatewayUtil.removeResponseTag(responseStr);
                            // Try to convert modified response to SOAP element
                            if (SOAPHelper.xmlStrToSOAPElement(responseStrTemp) != null) {
                                // If conversion succeeded response tag was only
                                // a wrapper that can be removed
                                responseStr = responseStrTemp;
                                logger.debug("Response tag was removed from the response string.");
                            } else {
                                logger.debug("Response tag is the root element and cannot be removed.");
                            }
                        }
                    } else {
                        // SOAP message has attachments. Use attachment's
                        // content type
                        String attContentType = SOAPHelper.getAttachmentContentType(serviceResponse.getSoapMessage());
                        response.setContentType(attContentType);
                        logger.debug("Use SOAP attachment as response message.");
                    }
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
            } else {
                // No endpoint was found -> return 404
                responseStr = this.generateError(Constants.ERROR_404, accept);
                response.setStatus(404);
            }
        } else {
            // No resource path was defined -> return 404
            responseStr = this.generateError(Constants.ERROR_404, accept);
            response.setStatus(404);
        }

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
            logger.error(e.getMessage());
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
        return request.getScheme() + "://" + // "http" + "://
                request.getServerName() + // "myhost"
                ":" + // ":"
                request.getServerPort() + // "8080"
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
    private Map filterRequestParameters(Map parameters) {
        parameters.remove(Constants.XRD_HEADER_USER_ID);
        parameters.remove(Constants.XRD_HEADER_MESSAGE_ID);
        parameters.remove(Constants.XRD_HEADER_NAMESPACE_SERIALIZE);
        parameters.remove(Constants.XRD_HEADER_NAMESPACE_PREFIX_SERIALIZE);
        parameters.remove(Constants.HTTP_HEADER_ACCEPT);
        return parameters;
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
            logger.error("Failed to read the request body from the request.");
        }
        return null;
    }

    /**
     * Serializes GET, POST, PUT and DELETE requests to SOAP.
     */
    private class RequestSerializer extends AbstractServiceRequestSerializer {

        private String resourceId;
        private String requestBody;
        private String contentType;

        public RequestSerializer(String resourceId, String requestBody, String contentType) {
            this.resourceId = resourceId;
            this.requestBody = requestBody;
            this.contentType = contentType;
        }

        @Override
        protected void serializeRequest(ServiceRequest request, SOAPElement soapRequest, SOAPEnvelope envelope) throws SOAPException {
            if (this.resourceId != null && !this.resourceId.isEmpty()) {
                logger.debug("Add resourceId : \"{}\".", this.resourceId);
                soapRequest.addChildElement("resourceId").addTextNode(this.resourceId);
            }
            Map<String, String[]> params = (Map<String, String[]>) request.getRequestData();
            for (String key : params.keySet()) {
                String[] arr = params.get(key);
                for (String value : arr) {
                    logger.debug("Add parameter : \"{}\" -> \"{}\".", key, value);
                    soapRequest.addChildElement(key).addTextNode(value);
                }
            }
            if (this.requestBody != null && !this.requestBody.isEmpty()) {
                logger.debug("Request body was found from the request. Add request body as SOAP attachment. Content type is \"{}\".", this.contentType);
                SOAPElement data = soapRequest.addChildElement(envelope.createName(Constants.PARAM_REQUEST_BODY));
                data.addAttribute(envelope.createName("href"), Constants.PARAM_REQUEST_BODY);
                AttachmentPart attachPart = request.getSoapMessage().createAttachmentPart(this.requestBody, this.contentType);
                attachPart.setContentId(Constants.PARAM_REQUEST_BODY);
                request.getSoapMessage().addAttachmentPart(attachPart);
            }
        }
    }

    /**
     * Deserializes SOAP responses to String.
     */
    private class ResponseDeserializer extends AbstractResponseDeserializer<Map, String> {

        private boolean omitNamespace;

        public ResponseDeserializer(boolean omitNamespace) {
            this.omitNamespace = omitNamespace;
        }

        @Override
        protected Map deserializeRequestData(Node requestNode) throws SOAPException {
            return null;
        }

        @Override
        protected String deserializeResponseData(Node responseNode, SOAPMessage message) throws SOAPException {
            if (this.omitNamespace) {
                logger.debug("Remove namespaces from response.");
                SOAPHelper.removeNamespace(responseNode);
            }
            // If message has attachments, return the first attachment
            if (message.countAttachments() > 0) {
                logger.debug("SOAP attachment detected. Use attachment as response data.");
                return SOAPHelper.toString((AttachmentPart) message.getAttachments().next());
            }
            // Convert response to string
            return SOAPHelper.toString(responseNode);
        }
    }
}
