package com.pkrete.restgateway;

import com.pkrete.xrd4j.common.exception.XRd4JException;
import com.pkrete.xrd4j.common.message.ErrorMessage;
import com.pkrete.xrd4j.common.message.ServiceRequest;
import com.pkrete.xrd4j.common.message.ServiceResponse;
import com.pkrete.xrd4j.common.util.PropertiesUtil;
import com.pkrete.xrd4j.common.util.SOAPHelper;
import com.pkrete.xrd4j.rest.ClientResponse;
import com.pkrete.xrd4j.rest.client.RESTClient;
import com.pkrete.xrd4j.rest.client.RESTClientFactory;
import com.pkrete.xrd4j.server.AbstractAdapterServlet;
import com.pkrete.xrd4j.server.deserializer.AbstractCustomRequestDeserializer;
import com.pkrete.xrd4j.server.deserializer.CustomRequestDeserializer;
import com.pkrete.xrd4j.server.serializer.AbstractServiceResponseSerializer;
import com.pkrete.xrd4j.server.serializer.ServiceResponseSerializer;
import com.pkrete.restgateway.endpoint.ProviderEndpoint;
import com.pkrete.restgateway.util.Constants;
import com.pkrete.restgateway.util.ProviderGatewayUtil;
import com.pkrete.restgateway.util.RESTGatewayUtil;
import com.pkrete.xrd4j.common.security.Decrypter;
import com.pkrete.xrd4j.common.security.Encrypter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
 * external properties files. This class implements a REST provider gateway by
 * forwarding incoming requests to configured service endpoints, and returning
 * the responses to the requesters. Responses can be converted from JSON to XML,
 * or they can be transmitted as SOAP attachments.
 *
 * @author Petteri Kivimäki
 */
public class ProviderGateway extends AbstractAdapterServlet {

    private Properties props;
    private Map<String, ProviderEndpoint> endpoints;
    private static final Logger logger = LoggerFactory.getLogger(ProviderGateway.class);
    private Decrypter asymmetricDecrypter;
    private final Map<String, Encrypter> asymmetricEncrypterCache = new HashMap<>();
    private int keyLength;
    private String publicKeyFile;
    private String publicKeyFilePassword;

    @Override
    public void init() {
        super.init();
        logger.debug("Starting to initialize Provider REST Gateway.");
        logger.debug("Reading Provider and ProviderGateway properties");
        String propertiesDirectoryParameter = System.getProperty(Constants.PROPERTIES_DIR_PARAM_NAME);
        Properties endpointProps;
        if (propertiesDirectoryParameter != null) {
            this.props = PropertiesUtil.getInstance().load(propertiesDirectoryParameter + Constants.PROPERTIES_FILE_PROVIDER_GATEWAY, false);
            endpointProps = PropertiesUtil.getInstance().load(propertiesDirectoryParameter + Constants.PROPERTIES_FILE_PROVIDERS, false);
        } else {
            this.props = PropertiesUtil.getInstance().load(Constants.PROPERTIES_FILE_PROVIDER_GATEWAY);
            endpointProps = PropertiesUtil.getInstance().load(Constants.PROPERTIES_FILE_PROVIDERS);
        }
        logger.debug("Default namespace for incoming ServiceRequests : \"{}\".", this.props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE));
        logger.debug("Default namespace for outgoing ServiceResponses : \"{}\".", this.props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE));
        logger.debug("Default namespace prefix for outgoing ServiceResponses : \"{}\".", this.props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE));
        this.publicKeyFile = props.getProperty(Constants.ENCRYPTION_PROPS_PUBLIC_KEY_FILE);
        this.publicKeyFilePassword = props.getProperty(Constants.ENCRYPTION_PROPS_PUBLIC_KEY_FILE_PASSWORD);
        this.keyLength = RESTGatewayUtil.getKeyLength(props);
        logger.debug("Symmetric key length : \"{}\".", this.keyLength);
        logger.debug("Setting Provider and ProviderGateway properties");
        this.endpoints = ProviderGatewayUtil.extractProviders(endpointProps, this.props);
        // Check encryption properties
        if (ProviderGatewayUtil.checkPrivateKeyProperties(props, endpoints)) {
            this.asymmetricDecrypter = RESTGatewayUtil.checkPrivateKey(props);
        }
        logger.debug("Provider REST Gateway initialized.");
    }

    /**
     * Must return the absolute path of the WSDL file.
     *
     * @return absolute path of the WSDL file
     */
    @Override
    protected String getWSDLPath() {
        String path = this.props.getProperty("wsdl.path");
        logger.debug("WSDL path : \"" + path + "\".");
        return path;
    }

    /**
     * Takes care of processing of all the incoming messages.
     *
     * @param request ServiceRequest object that holds the request data
     * @return ServiceResponse object that holds the response data
     * @throws SOAPException if there's a SOAP error
     * @throws XRd4JException if there's a XRd4J error
     */
    @Override
    protected ServiceResponse handleRequest(ServiceRequest request) throws SOAPException, XRd4JException {
        ServiceResponse response = new ServiceResponse<>(request.getConsumer(), request.getProducer(), request.getId());
        String serviceId = request.getProducer().toString();

        // Check if an endpoint that matches the given service ID exists
        if (!this.endpoints.containsKey(serviceId)) {
            logger.warn("No endpoint matching the given service id found: \"{}\".", serviceId);
            return response;
        }

        // Get the endpoint
        ProviderEndpoint endpoint = this.endpoints.get(serviceId);
        logger.info("Process \"{}\" service.", serviceId);

        // Set request and response wrapper processing
        if (endpoint.isProcessingWrappers() != null) {
            request.setProcessingWrappers(endpoint.isProcessingWrappers());
            response.setProcessingWrappers(endpoint.isProcessingWrappers());
        }
        // Deserialize the request
        CustomRequestDeserializer customDeserializer = getRequestDeserializer(endpoint);
        // Deserialize the request
        customDeserializer.deserialize(request, endpoint.getNamespaceDeserialize());

        // Set producer namespace URI and prefix before processing
        response.getProducer().setNamespaceUrl(endpoint.getNamespaceSerialize());
        response.getProducer().setNamespacePrefix(endpoint.getPrefix());
        logger.debug("Do message processing...");

        // Create response serializer
        ServiceResponseSerializer serializer = getResponseSerializer(endpoint, request.getConsumer().toString());

        if (request.getRequestData() == null) {
            logger.warn("No request data was found. Return a non-techinal error message.");
            ErrorMessage error = new ErrorMessage("422", Constants.ERROR_422);
            response.setErrorMessage(error);
            serializer.serialize(response, request);
            return response;
        }

        // Filter request parameters
        ProviderGatewayUtil.filterRequestParameters(request, endpoint);

        // Get HTTP headers for the request
        Map<String, String> headers = ProviderGatewayUtil.generateHttpHeaders(request, endpoint);
        logger.debug("Fetch data from service...");
        // Create a REST client, endpoint's HTTP verb defines the type
        // of the client that's returned
        RESTClient restClient = RESTClientFactory.createRESTClient(endpoint.getHttpVerb());
        // Get request body
        String requestBody = ProviderGatewayUtil.getRequestBody((Map<String, List<String>>) request.getRequestData());
        // Send request to the service endpoint
        ClientResponse restResponse = restClient.send(endpoint.getUrl(), requestBody, (Map<String, List<String>>) request.getRequestData(), headers);
        logger.debug("...done!");

        String data = restResponse.getData();
        String contentType = restResponse.getContentType();

        // Content-type must be "text/xml", "application/xml" or
        // "application/json"
        if (!RESTGatewayUtil.isValidContentType(contentType)) {
            logger.warn("Response's content type is not \"{}\", \"{}\" or \"{}\".", Constants.TEXT_XML, Constants.APPLICATION_XML, Constants.APPLICATION_JSON);
            if (restResponse.getStatusCode() == 200) {
                logger.warn("Response's status code is 200. Return generic 404 error.");
                response.setErrorMessage(new ErrorMessage("404", Constants.ERROR_404));
            } else {
                logger.warn("Response's status code is {}. Reason phrase is : \"{}\".", restResponse.getStatusCode(), restResponse.getReasonPhrase());
                response.setErrorMessage(new ErrorMessage(Integer.toString(restResponse.getStatusCode()), restResponse.getReasonPhrase()));
            }
            serializer.serialize(response, request);
            return response;
        }

        // If response is passed as an attachment, there's no need
        // for conversion
        if (endpoint.isAttachment()) {
            // Data will be put as attachment - no modifications
            // needed
            response.setResponseData(data);
            // Set serializer's content type because we need to handle attachments
            if (endpoint.isResponseEncrypted()) {
                ((EncryptingXMLServiceResponseSerializer) serializer).setContentType(contentType);
            } else {
                ((XMLServiceResponseSerializer) serializer).setContentType(contentType);
            }
        } else {
            // If data is not XML, it must be converted
            if (!RESTGatewayUtil.isXml(contentType)) {
                logger.debug("Convert response to XML.");
                // Convert service endpoint's response to XML
                data = ProviderGatewayUtil.fromJSONToXML(data);
            } else {
                // Do not change the namespace if response is XML
                response.setForceNamespaceToResponseChildren(false);
            }
            // Use XML as response data
            response.setResponseData(SOAPHelper.xmlStrToSOAPElement(data));
        }

        logger.debug("Message prosessing done!");

        // Serialize the response
        serializer.serialize(response, request);
        return response;

    }

    /**
     * Returns a new CustomRequestDeserializer that converts the request from
     * SOAP to ServiceRequest object. The implementation of the
     * CustomRequestDeserializer is decided based on the given parameters.
     *
     * @param endpoint ProviderEndpoint that's processed using the deserializer
     * @return new CustomRequestDeserializer object
     * @throws XRd4JException
     */
    private CustomRequestDeserializer getRequestDeserializer(ProviderEndpoint endpoint) throws XRd4JException {
        // Check is the request encrypted and create deserializer accordingly
        if (endpoint.isRequestEncrypted()) {
            // If asymmetric decrypter is null, there's nothing to do
            if (this.asymmetricDecrypter == null) {
                throw new XRd4JException("No private key available when decryption is required.");
            }
            return new DecryptingReqToMapRequestDeserializerImpl(asymmetricDecrypter);
        } else {
            return new ReqToMapRequestDeserializerImpl();
        }
    }

    /**
     * Returns a new ServiceResponseSerializer that converts the response object
     * to SOAP. The implementation of the ServiceResponseSerializer is decided
     * based on the given parameters.
     *
     * @param endpoint ProviderEndpoint that's processed using the serializer
     * @param consumerIdentifier string that identifies the consumer which
     * response is being processed
     * @return new ServiceResponseSerializer object
     * @throws XRd4JException
     */
    private ServiceResponseSerializer getResponseSerializer(ProviderEndpoint endpoint, String consumerIdentifier) throws XRd4JException {
        if (endpoint.isResponseEncrypted()) {
            logger.debug("Endpoint requires that response is encrypted.");
            Encrypter asymmetricEncrypter;
            // Check if encrypter already exists in cache
            if (this.asymmetricEncrypterCache.containsKey(consumerIdentifier)) {
                asymmetricEncrypter = this.asymmetricEncrypterCache.get(consumerIdentifier);
                logger.trace("Asymmetric encrypter for consumer \"{}\" loaded from cache.", consumerIdentifier);
            } else {
                // Create new encrypter if it does not exist yet
                asymmetricEncrypter = RESTGatewayUtil.getEncrypter(this.publicKeyFile, this.publicKeyFilePassword, consumerIdentifier);
                // Add new encrypter to the cache
                this.asymmetricEncrypterCache.put(consumerIdentifier, asymmetricEncrypter);
                logger.trace("Asymmetric encrypter for consumer \"{}\" not found from cache. New ecrypter created.", consumerIdentifier);
            }
            if (asymmetricEncrypter == null) {
                throw new XRd4JException("No public key found when encryption is required.");
            }
            return new EncryptingXMLServiceResponseSerializer(asymmetricEncrypter, this.keyLength);
        } else {
            return new XMLServiceResponseSerializer();
        }
    }

    /**
     * This private class deserializes all the request parameters in a Map as
     * key - value pairs.
     */
    private class ReqToMapRequestDeserializerImpl extends AbstractCustomRequestDeserializer<Map> {

        @Override
        protected Map deserializeRequest(Node requestNode, SOAPMessage message) throws SOAPException {
            if (requestNode == null) {
                logger.warn("\"requestNode\" is null. Null is returned.");
                return null;
            }
            // Convert all the elements under request to key - value list pairs.
            // Each key can have multiple values
            Map map = SOAPHelper.nodesToMultiMap(requestNode.getChildNodes());
            // If message has attachments, use the first attachment as
            // request body
            processAttachment(map, message);
            return map;
        }

        protected boolean processAttachment(Map map, SOAPMessage message) {
            // If message has attachments, use the first attachment as
            // request body
            if (message.countAttachments() > 0 && map.containsKey(Constants.PARAM_REQUEST_BODY)) {
                logger.debug("SOAP attachment detected. Use attachment as request body.", Constants.PARAM_REQUEST_BODY);
                List<String> values = new ArrayList<>();
                values.add(SOAPHelper.toString((AttachmentPart) message.getAttachments().next()));
                map.put(Constants.PARAM_REQUEST_BODY, values);
                return true;
            } else {
                map.remove(Constants.PARAM_REQUEST_BODY);
                return false;
            }
        }
    }

    private class DecryptingReqToMapRequestDeserializerImpl extends ReqToMapRequestDeserializerImpl {

        private final Decrypter asymmetricDecrypter;

        public DecryptingReqToMapRequestDeserializerImpl(Decrypter asymmetricDecrypter) {
            this.asymmetricDecrypter = asymmetricDecrypter;
            logger.debug("New DecryptingReqToMapRequestDeserializerImpl created.");
        }

        @Override
        protected Map deserializeRequest(Node requestNode, SOAPMessage message) throws SOAPException {
            if (requestNode == null) {
                logger.warn("\"requestNode\" is null. Null is returned.");
                return null;
            }
            Map<String, String> nodes = SOAPHelper.nodesToMap(requestNode.getChildNodes());

            // Decrypt session key using the private key
            Decrypter symmetricDecrypter = RESTGatewayUtil.getSymmetricDecrypter(this.asymmetricDecrypter, nodes.get(Constants.PARAM_KEY), nodes.get(Constants.PARAM_IV));
            // Decrypt the data
            String decrypted = symmetricDecrypter.decrypt(nodes.get(Constants.PARAM_ENCRYPTED));
            // Convert decrypted data to SOAP element
            SOAPElement soapData = SOAPHelper.xmlStrToSOAPElement(decrypted);

            // Convert all the elements under request to key - value list pairs.
            // Each key can have multiple values
            Map map = SOAPHelper.nodesToMultiMap(soapData.getChildNodes());
            // If message has attachments, use the first attachment as
            // request body
            processAttachment(map, message);
            // If the message has attachment, it has to be decrypted too
            if (map.containsKey(Constants.PARAM_REQUEST_BODY)) {
                // Get attachment value by fixed parameter name. The map
                // contains key - value list so we must get the whole list.
                List<String> values = (List<String>) map.get(Constants.PARAM_REQUEST_BODY);
                // Only the first attachment is handled
                String encryptedValue = values.get(0);
                // Create new list that's added to the results
                List<String> newValues = new ArrayList<>();
                // Add decrypted attachment to the new list
                newValues.add(symmetricDecrypter.decrypt(encryptedValue));
                // Replace the old value with the new one
                map.put(Constants.PARAM_REQUEST_BODY, newValues);
            }
            return map;
        }
    }

    /**
     * This private class serializes ServiceResponses as XML inside the
     * SOAPBody's response element.
     */
    private class XMLServiceResponseSerializer extends AbstractServiceResponseSerializer {

        protected String contentType;

        @Override
        public void serializeResponse(ServiceResponse response, SOAPElement soapResponse, SOAPEnvelope envelope) throws SOAPException {
            if (this.contentType == null) {
                handleBody(response, soapResponse, envelope);
            } else {
                handleAttachment(response, soapResponse, envelope);
            }
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        protected void handleBody(ServiceResponse response, SOAPElement soapResponse, SOAPEnvelope envelope) throws SOAPException {
            SOAPElement responseElem = (SOAPElement) response.getResponseData();
            if ("response".equals(responseElem.getLocalName())) {
                logger.debug("Additional \"response\" wrapper detected. Remove the wrapper.");
                for (int i = 0; i < responseElem.getChildNodes().getLength(); i++) {
                    Node importNode = (Node) soapResponse.getOwnerDocument().importNode(responseElem.getChildNodes().item(i), true);
                    soapResponse.appendChild(importNode);
                }
            } else {
                soapResponse.addChildElement((SOAPElement) response.getResponseData());
            }
        }

        protected void handleAttachment(ServiceResponse response, SOAPElement soapResponse, SOAPEnvelope envelope) throws SOAPException {
            SOAPElement data = soapResponse.addChildElement(envelope.createName("data"));
            data.addAttribute(envelope.createName("href"), "response_data");
            AttachmentPart attachPart = response.getSoapMessage().createAttachmentPart(response.getResponseData(), contentType);
            attachPart.setContentId("response_data");
            response.getSoapMessage().addAttachmentPart(attachPart);
        }
    }

    private class EncryptingXMLServiceResponseSerializer extends XMLServiceResponseSerializer {

        private final Encrypter asymmetricEncrypter;
        private final int keyLength;

        public EncryptingXMLServiceResponseSerializer(Encrypter asymmetricEncrypter, int keyLength) {
            this.asymmetricEncrypter = asymmetricEncrypter;
            this.keyLength = keyLength;
            logger.debug("New EncryptingXMLServiceResponseSerializer created.");
        }

        @Override
        public void serializeResponse(ServiceResponse response, SOAPElement soapResponse, SOAPEnvelope envelope) throws SOAPException {
            // Create wrapper for the response data
            SOAPElement payload = SOAPHelper.xmlStrToSOAPElement("<" + Constants.PARAM_ENCRYPTION_WRAPPER + "/>");
            try {
                // Create new symmetric encrypter of defined key length
                Encrypter symmetricEncrypter = RESTGatewayUtil.createSymmetricEncrypter(this.keyLength);
                // If content type is null, there are now attachments
                if (this.contentType == null) {
                    // Add response under the wrapper
                    handleBody(response, payload, envelope);
                } else {
                    // Get response data that will be put as attachment
                    String plainText = (String) response.getResponseData();
                    // Encrypt response
                    response.setResponseData(symmetricEncrypter.encrypt(plainText));
                    // Process attachment - this will add new element to body too
                    handleAttachment(response, payload, envelope);
                }
                /**
                 * N.B.! If signature is required (A: sign then encrypt), this
                 * is the place to do it. The string to be signed is accessed
                 * like this: SOAPHelper.toString(soapResponse)
                 */
                // Encrypt message with symmetric AES encryption
                String encryptedData = symmetricEncrypter.encrypt(SOAPHelper.toString(payload));
                /**
                 * N.B.! If signature is required (B: encrypt then sign), this
                 * is the place to do it. The encryptedData variable must be
                 * signed.
                 */
                // Build message body that includes enrypted data,
                // encrypted session key and IV
                RESTGatewayUtil.buildEncryptedBody(symmetricEncrypter, asymmetricEncrypter, soapResponse, encryptedData);
            } catch (NoSuchAlgorithmException ex) {
                logger.error(ex.getMessage(), ex);
                throw new SOAPException("Encrypting SOAP request failed.", ex);
            }
        }

    }
}
