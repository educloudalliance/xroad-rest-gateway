package com.pkrete.xrd4j.tools.rest_gateway;

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
import com.pkrete.xrd4j.tools.rest_gateway.endpoint.ProviderEndpoint;
import com.pkrete.xrd4j.tools.rest_gateway.util.Constants;
import com.pkrete.xrd4j.tools.rest_gateway.util.ProviderGatewayUtil;
import com.pkrete.xrd4j.tools.rest_gateway.util.RESTGatewayUtil;
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
    private final static Logger logger = LoggerFactory.getLogger(ProviderGateway.class);

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
        logger.debug("Setting Provider and ProviderGateway properties");
        this.endpoints = ProviderGatewayUtil.extractProviders(endpointProps, this.props);
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
        ServiceResponseSerializer serializer;
        ServiceResponse response = null;
        String serviceId = request.getProducer().toString();

        // Check if an endpoint that matches the given service ID exists
        if (this.endpoints.containsKey(serviceId)) {
            // Response content type
            String contentType = null;
            // Get the endpoint
            ProviderEndpoint endpoint = this.endpoints.get(serviceId);
            logger.info("Process \"{}\" service.", serviceId);

            // Deserialize the request
            CustomRequestDeserializer customDeserializer = new ReqToMapRequestDeserializerImpl();
            customDeserializer.deserialize(request, endpoint.getNamespaceDeserialize());

            // Create response object according to the settings
            if (endpoint.isAttachment()) {
                response = new ServiceResponse<Map, String>(request.getConsumer(), request.getProducer(), request.getId());
            } else {
                response = new ServiceResponse<Map, SOAPElement>(request.getConsumer(), request.getProducer(), request.getId());
            }

            // Set producer namespace URI and prefix before processing
            response.getProducer().setNamespaceUrl(endpoint.getNamespaceSerialize());
            response.getProducer().setNamespacePrefix(endpoint.getPrefix());
            logger.debug("Do message prosessing...");

            // Process the request if request data is present
            if (request.getRequestData() != null) {
                // Get HTTP headers for the request
                Map<String, String> headers = ProviderGatewayUtil.generateHttpHeaders(request, endpoint);
                logger.debug("Fetch data from service...");
                // Create a REST client, endpoint's HTTP verb defines the type
                // of the client that's returned
                RESTClient restClient = RESTClientFactory.createRESTClient(endpoint.getHttpVerb());
                // Get request body
                String requestBody = ProviderGatewayUtil.getRequestBody(((Map<String, String>) request.getRequestData()));
                // Send request to the service endpoint
                ClientResponse restResponse = restClient.send(endpoint.getUrl(), requestBody, ((Map<String, String>) request.getRequestData()), headers);
                logger.debug("...done!");

                String data = restResponse.getData();
                contentType = restResponse.getContentType();

                // Content-type must be "text/xml", "application/xml" or 
                // "application/json"
                if (RESTGatewayUtil.isValidContentType(contentType)) {
                    // If response is passed as an attachement, there's no need
                    // for for conversion
                    if (endpoint.isAttachment()) {
                        // Data will be put as attachment - no modifications
                        // needed
                        response.setResponseData(data);
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
                } else {
                    logger.warn("Response's content type is not \"{}\", \"{}\" or \"{}\".", Constants.TEXT_XML, Constants.APPLICATION_XML, Constants.APPLICATION_JSON);
                    if (restResponse.getStatusCode() == 200) {
                        logger.warn("Response's status code is 200. Return generic 404 error.");
                        response.setErrorMessage(new ErrorMessage("404", Constants.ERROR_404));
                    } else {
                        logger.warn("Response's status code is {}. Reason phrase is : \"{}\".", restResponse.getStatusCode(), restResponse.getReasonPhrase());
                        response.setErrorMessage(new ErrorMessage(Integer.toString(restResponse.getStatusCode()), restResponse.getReasonPhrase()));
                    }
                }
            } else {
                logger.warn("No request data was found. Return a non-techinal error message.");
                ErrorMessage error = new ErrorMessage("422", Constants.ERROR_422);
                response.setErrorMessage(error);
            }
            logger.debug("Message prosessing done!");
            // Create serializer object according to the settings
            if (endpoint.isAttachment()) {
                serializer = new AttachmentServiceResponseSerializer(contentType);
            } else {
                serializer = new XMLServiceResponseSerializer();
            }
            // Serialize the response
            serializer.serialize(response, request);
            return response;
        }
        return response;
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
            // Convert all the elements under request to key-value pairs
            Map map = SOAPHelper.nodesToMap(requestNode.getChildNodes());
            // If message has attachments, use the first attachment as
            // request body
            if (message.countAttachments() > 0 && map.containsKey(Constants.PARAM_REQUEST_BODY)) {
                logger.debug("SOAP attachment detected. Use attachment as request body.", Constants.PARAM_REQUEST_BODY);
                map.put(Constants.PARAM_REQUEST_BODY, SOAPHelper.toString((AttachmentPart) message.getAttachments().next()));
            } else {
                map.remove(Constants.PARAM_REQUEST_BODY);
            }
            return map;
        }
    }

    /**
     * This private class serializes ServiceResponses as XML inside the
     * SOAPBody's response element.
     */
    private class XMLServiceResponseSerializer extends AbstractServiceResponseSerializer {

        @Override
        public void serializeResponse(ServiceResponse response, SOAPElement soapResponse, SOAPEnvelope envelope) throws SOAPException {
            SOAPElement responseElem = (SOAPElement) response.getResponseData();
            if (responseElem.getLocalName().equals("response")) {
                logger.debug("Additional \"response\" wrapper detected. Remove the wrapper.");
                for (int i = 0; i < responseElem.getChildNodes().getLength(); i++) {
                    Node importNode = (Node) envelope.getBody().getOwnerDocument().importNode(responseElem.getChildNodes().item(i), true);
                    soapResponse.appendChild(importNode);
                }
            } else {
                SOAPElement data = soapResponse.addChildElement((SOAPElement) response.getResponseData());
            }
        }
    }

    /**
     * This private class serializes ServiceResponses as SOAP attachments.
     */
    private class AttachmentServiceResponseSerializer extends AbstractServiceResponseSerializer {

        private final String contentType;

        public AttachmentServiceResponseSerializer(String contentType) {
            this.contentType = contentType;
        }

        @Override
        public void serializeResponse(ServiceResponse response, SOAPElement soapResponse, SOAPEnvelope envelope) throws SOAPException {
            SOAPElement data = soapResponse.addChildElement(envelope.createName("data"));
            data.addAttribute(envelope.createName("href"), "response_data");
            AttachmentPart attachPart = response.getSoapMessage().createAttachmentPart(response.getResponseData(), contentType);
            attachPart.setContentId("response_data");
            response.getSoapMessage().addAttachmentPart(attachPart);
        }
    }
}
