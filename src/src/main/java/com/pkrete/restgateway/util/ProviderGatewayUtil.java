package com.pkrete.restgateway.util;

import com.pkrete.xrd4j.common.message.ServiceRequest;
import com.pkrete.xrd4j.common.util.MessageHelper;
import com.pkrete.xrd4j.rest.converter.JSONToXMLConverter;
import com.pkrete.restgateway.endpoint.ProviderEndpoint;
import com.pkrete.xrd4j.common.security.Decrypter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides utility methods for Provider Gateway implementation.
 *
 * @author Petteri Kivimäki
 */
public class ProviderGatewayUtil {

    private static final Logger logger = LoggerFactory.getLogger(ProviderGatewayUtil.class);

    /**
     * This a utility class providing only static methods which is why it should
     * not be initiated.
     */
    private ProviderGatewayUtil() {

    }

    /**
     * Goes through the given properties and extracts all the defined provider
     * endpoints. Returns a map containing service id - provider endpoint
     * key-value pairs.
     *
     * @param endpoints endpoint properties
     * @param gatewayProperties REST Provider Gateway general properties
     * @return map containing service id - provider endpoint key-value pairs
     */
    public static Map<String, ProviderEndpoint> extractProviders(Properties endpoints, Properties gatewayProperties) {
        Map<String, ProviderEndpoint> results = new HashMap<>();
        logger.info("Start extracting provider endpoints from properties.");
        if (endpoints == null || endpoints.isEmpty()) {
            logger.warn("No endpoints were founds. The list was null or empty.");
            return results;
        }

        int i = 0;
        String key = Integer.toString(i);

        // Loop through all the endpoints
        while (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_ID)) {

            String id = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_ID);
            String url = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_URL);

            if (RESTGatewayUtil.isNullOrEmpty(id) || RESTGatewayUtil.isNullOrEmpty(url)) {
                logger.warn("ID or URL is null or empty. Provider endpoint skipped.");
                key = Integer.toString(++i);
                continue;
            }

            ProviderEndpoint endpoint = new ProviderEndpoint(id, url);
            setDefaultValues(endpoint, gatewayProperties);

            logger.info("New provider endpoint found. ID : \"{}\", URL : \"{}\".", id, url);

            // HTTP verb, content-Type HTTP header, accept HTTP header,
            // attachment, X-Road headers, request parameter name filter 
            // condition, request parameter name filter operation,
            // request parameter value filter condition, request parameter 
            // value filter operation
            extractEndpoints(key, endpoints, endpoint);

            // Wrapper processing, ServiceRequest namespace,
            // ServiceResponse namespace, ServiceResponse namespace prefix
            RESTGatewayUtil.extractEndpoints(key, endpoints, endpoint);

            results.put(id, endpoint);

            // Increase counter by one and update key
            key = Integer.toString(++i);
        }
        logger.info("{} provider endpoints extracted from properties.", results.size());
        return results;
    }

    /**
     * Extracts properties common for consumer endpoints from the given
     * properties.
     *
     * @param key property key
     * @param endpoints list of configured endpoints read from properties
     * @param endpoint the endpoint object that's being initialized
     */
    public static void extractEndpoints(String key, Properties endpoints, ProviderEndpoint endpoint) {
        // HTTP verb
        if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_VERB)) {
            String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_VERB);
            endpoint.setHttpVerb(value);
            logger.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENDPOINT_PROPS_VERB, value);
        }
        // Content-Type HTTP header
        if (endpoints.containsKey(key + "." + Constants.PROVIDER_PROPS_CONTENT_TYPE)) {
            String value = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_CONTENT_TYPE);
            endpoint.setContentType(value);
            logger.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.PROVIDER_PROPS_CONTENT_TYPE, value);
        }
        // Accept HTTP header
        if (endpoints.containsKey(key + "." + Constants.PROVIDER_PROPS_ACCEPT)) {
            String value = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_ACCEPT);
            endpoint.setAccept(value);
            logger.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.PROVIDER_PROPS_ACCEPT, value);
        }
        // Attachment
        if (endpoints.containsKey(key + "." + Constants.PROVIDER_PROPS_ATTACHMENT)) {
            String value = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_ATTACHMENT);
            endpoint.setAttachment(MessageHelper.strToBool(value));
            logger.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.PROVIDER_PROPS_ATTACHMENT, value);
        }
        // X-Road headers
        if (endpoints.containsKey(key + "." + Constants.PROVIDER_PROPS_SEND_XRD_HEADERS)) {
            String value = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_SEND_XRD_HEADERS);
            endpoint.setSendXrdHeaders(MessageHelper.strToBool(value));
            logger.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.PROVIDER_PROPS_SEND_XRD_HEADERS, value);
        }
        // Request parameter name filter condition
        if (endpoints.containsKey(key + "." + Constants.PROVIDER_PROPS_REQUEST_PARAM_NAME_FILTER_CONDITION)) {
            String value = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_REQUEST_PARAM_NAME_FILTER_CONDITION);
            endpoint.setReqParamNameFilterCondition(value);
            logger.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.PROVIDER_PROPS_REQUEST_PARAM_NAME_FILTER_CONDITION, value);
        }
        // Request parameter name filter operation
        if (endpoints.containsKey(key + "." + Constants.PROVIDER_PROPS_REQUEST_PARAM_NAME_FILTER_OPERATION)) {
            String value = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_REQUEST_PARAM_NAME_FILTER_OPERATION);
            endpoint.setReqParamNameFilterOperation(value);
            logger.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.PROVIDER_PROPS_REQUEST_PARAM_NAME_FILTER_OPERATION, value);
        }
        // Request parameter value filter condition
        if (endpoints.containsKey(key + "." + Constants.PROVIDER_PROPS_REQUEST_PARAM_VALUE_FILTER_CONDITION)) {
            String value = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_REQUEST_PARAM_VALUE_FILTER_CONDITION);
            endpoint.setReqParamValueFilterCondition(value);
            logger.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.PROVIDER_PROPS_REQUEST_PARAM_VALUE_FILTER_CONDITION, value);
        }
        // Request parameter value filter operation
        if (endpoints.containsKey(key + "." + Constants.PROVIDER_PROPS_REQUEST_PARAM_VALUE_FILTER_OPERATION)) {
            String value = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_REQUEST_PARAM_VALUE_FILTER_OPERATION);
            endpoint.setReqParamValueFilterOperation(value);
            logger.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.PROVIDER_PROPS_REQUEST_PARAM_VALUE_FILTER_OPERATION, value);
        }
    }

    /**
     * Sets default values to the given endpoint.
     *
     * @param endpoint ProviderEndpoint to be modified
     * @param gatewayProperties REST Consumer Gateway general properties
     */
    private static void setDefaultValues(ProviderEndpoint endpoint, Properties gatewayProperties) {
        // Set default HTTP verb - GET
        endpoint.setHttpVerb("get");

        // Initialize endpoint properties to those defined in gateway properties
        endpoint.setNamespaceDeserialize(gatewayProperties.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE));
        endpoint.setNamespaceSerialize(gatewayProperties.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE));
        endpoint.setPrefix(gatewayProperties.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE));
        if (gatewayProperties.containsKey(Constants.ENDPOINT_PROPS_WRAPPERS)) {
            endpoint.setProcessingWrappers(MessageHelper.strToBool(gatewayProperties.getProperty(Constants.ENDPOINT_PROPS_WRAPPERS)));
        }
    }

    /**
     * Generates a Map containing HTTP headers that are sent to the service
     * endpoint.
     *
     * @param request ServiceRequest holding X-Road specific headers
     * @param endpoint ProviderEndpoint holding standard HTTP headers
     * @return Map containing HTTP headers
     */
    public static Map<String, String> generateHttpHeaders(ServiceRequest request, ProviderEndpoint endpoint) {
        logger.info("Generate HTTP headers.");
        Map<String, String> headers = new HashMap<>();
        if (endpoint.isSendXrdHeaders()) {
            logger.debug("Generate X-Road specific headers.");
            headers.put(Constants.XRD_HEADER_CLIENT, request.getConsumer().toString());
            headers.put(Constants.XRD_HEADER_SERVICE, request.getProducer().toString());
            headers.put(Constants.XRD_HEADER_MESSAGE_ID, request.getId());
            logger.debug(Constants.LOG_STRING_FOR_HEADERS, Constants.XRD_HEADER_CLIENT, request.getConsumer().toString());
            logger.debug(Constants.LOG_STRING_FOR_HEADERS, Constants.XRD_HEADER_SERVICE, request.getProducer().toString());
            logger.debug(Constants.LOG_STRING_FOR_HEADERS, Constants.XRD_HEADER_MESSAGE_ID, request.getId());
            if (request.getUserId() != null && !request.getUserId().isEmpty()) {
                logger.debug(Constants.LOG_STRING_FOR_HEADERS, Constants.XRD_HEADER_USER_ID, request.getUserId());
                headers.put(Constants.XRD_HEADER_USER_ID, request.getUserId());
            }
        } else {
            logger.debug("Generation of X-Road specific headers is disabled.");
        }
        if (endpoint.getContentType() != null && !endpoint.getContentType().isEmpty()) {
            logger.debug(Constants.LOG_STRING_FOR_HEADERS, Constants.HTTP_HEADER_CONTENT_TYPE, endpoint.getContentType());
            headers.put(Constants.HTTP_HEADER_CONTENT_TYPE, endpoint.getContentType());
        }
        if (endpoint.getAccept() != null && !endpoint.getAccept().isEmpty()) {
            logger.debug(Constants.LOG_STRING_FOR_HEADERS, Constants.HTTP_HEADER_ACCEPT, endpoint.getAccept());
            headers.put(Constants.HTTP_HEADER_ACCEPT, endpoint.getAccept());
        }
        logger.info("HTTP headers were succesfully generated.");
        return headers;
    }

    /**
     * Converts JSON string to XML string. XML string is wrapped inside
     * \<response\> wrapper element. The wrapper must be added, because
     * otherwise it's not possible to convert the XML to SOAP element. JSON
     * string does not likely have a root element that SOAP requires.
     * \<response\> is used as a temporary root element and will be omitted by
     * ProviderGateway when SOAP response is serialized as XML.
     *
     * @param data JSON string to be converted
     * @return XML string
     */
    public static String fromJSONToXML(String data) {
        // Set wrapper tag's name
        String wrapper = "response";
        // Convert service endpoint's response to XML
        String dataXml = new JSONToXMLConverter().convert(data);
        // Return data inside wrapper element
        return "<" + wrapper + ">" + dataXml + "</" + wrapper + ">";
    }

    /**
     * Extracts the request body from the parameters map and returns the value
     * matching the "requestBody" key. If the "requestBody" key is found, it's
     * removed from the params map. If no "requestBody" key is found, null is
     * returned.
     *
     * @param params request parameters as key value pairs
     * @return value matching the "requestBody" key or null
     */
    public static String getRequestBody(Map<String, List<String>> params) {
        if (params.containsKey(Constants.PARAM_REQUEST_BODY)) {
            logger.trace("\"{}\" key found.", Constants.PARAM_REQUEST_BODY);
            // Get value matching the key
            String requestBody = params.get(Constants.PARAM_REQUEST_BODY).get(0);
            // Remove the key-value pair from the map
            params.remove(Constants.PARAM_REQUEST_BODY);
            // Return the value
            return requestBody;
        }
        logger.trace("\"{}\" key not found.", Constants.PARAM_REQUEST_BODY);
        // No key-value pair found, return null
        return null;

    }

    /**
     * Filters request parameter names and values according to the rules defined
     * by the ProviderEndpoint. Filter can be applied to only parameter name or
     * value or both of them. Filter condition and operation are defined
     * individually for parameter name and value.
     *
     * @param request request which parameters are filtered
     * @param endpoint endpoint that contains the rules for filtering
     */
    public static void filterRequestParameters(ServiceRequest request, ProviderEndpoint endpoint) {
        logger.debug("Start filtering request parameters.");
        // Get a list of keys in the parameters map
        List<String> keys = new ArrayList<>(((Map<String, String>) request.getRequestData()).keySet());
        // Loop through request parameters
        for (String orgKey : keys) {
            // Skip request body and resourceId parameters - all the other 
            // parameters are filtered
            if (!orgKey.equals(Constants.PARAM_REQUEST_BODY) && !orgKey.equals(Constants.PARAM_RESOURCE_ID)) {
                processReqParamFilters(request, endpoint, orgKey);
            } else {
                logger.trace("Skip \"{}\" and \"{}\" parameters.", Constants.PARAM_REQUEST_BODY, Constants.PARAM_RESOURCE_ID);
            }
        }
        logger.debug("Filtering request parameters done.");
    }

    /**
     * Processes request parameter name and value filters for the parameter
     * specified by orgKey. After processing processing the modified values are
     * updated to the original request.
     *
     * @param request request which parameters are filtered
     * @param endpoint endpoint that contains the rules for filtering
     * @param orgKey the name of parameter to process
     */
    private static void processReqParamFilters(ServiceRequest request, ProviderEndpoint endpoint, String orgKey) {
        // Variable that tells if request data must be updated
        boolean update = false;
        // Variable for new key value
        String key = orgKey;
        // Get values list
        List<String> values = ((Map<String, List<String>>) request.getRequestData()).get(orgKey);

        // Check if request parameter name filter has been defined
        if (endpoint.getReqParamNameFilterCondition() != null && endpoint.getReqParamNameFilterOperation() != null) {
            logger.trace("Request parameter name: \"{}\". Filter condition: \"{}\"", orgKey, endpoint.getReqParamNameFilterCondition());
            Pattern regex = Pattern.compile(endpoint.getReqParamNameFilterCondition());
            Matcher m = regex.matcher(orgKey);
            if (m.find()) {
                key = m.replaceAll(endpoint.getReqParamNameFilterOperation());
                logger.trace("Filter condition: true. Filter operation: \"{}\". Parameter name: \"{}\" => \"{}\"", endpoint.getReqParamNameFilterOperation(), orgKey, key);
                update = true;
            }
        }
        // Check if request parameter value filter has been defined
        if (endpoint.getReqParamValueFilterCondition() != null && endpoint.getReqParamValueFilterOperation() != null) {
            // Loop through the values
            for (int i = 0; i < values.size(); i++) {
                String orgValue = values.get(i);
                logger.trace("Request parameter value: \"{}\". Filter condition: \"{}\"", orgValue, endpoint.getReqParamValueFilterCondition());
                Pattern regex = Pattern.compile(endpoint.getReqParamValueFilterCondition());
                Matcher m = regex.matcher(orgValue);
                if (m.find()) {
                    String value = m.replaceAll(endpoint.getReqParamValueFilterOperation());
                    logger.trace("Filter condition: true. Filter operation: \"{}\". Parameter name: \"{}\" => \"{}\"", endpoint.getReqParamValueFilterOperation(), orgValue, value);
                    values.set(i, value);
                    update = true;
                }

            }
        }

        // If key or value have changed, request data must be updated
        if (update) {
            // Remove old key - value list pair
            ((Map<String, List<String>>) request.getRequestData()).remove(orgKey);
            // Add modified key - value list pair
            ((Map<String, List<String>>) request.getRequestData()).put(key, values);
        }
    }

    /**
     * Checks and validates the properties related to the private key. If
     * everything is OK, true is returned. If there's a problem with the private
     * key, false is returned.
     *
     * @param props general properties
     * @param endpoints list of configured endpoints
     * @return true if everything is OK. False if there's a problem with the
     * private key or the private key is not needed
     */
    public static boolean checkPrivateKeyProperties(Properties props, Map<String, ProviderEndpoint> endpoints) {
        logger.info("Check private key encryption properties.");
        // Loop through all the endpoints
        for (Map.Entry<String, ProviderEndpoint> entry : endpoints.entrySet()) {
            ProviderEndpoint endpoint = entry.getValue();
            // If request is encrypted, decryption is done using the private
            // key, so it must be possible to access it. It's enough to 
            // check the private key once.
            if (endpoint.isRequestEncrypted()) {
                Decrypter decrypter = RESTGatewayUtil.checkPrivateKey(props);
                if (decrypter != null) {
                    // Private key props are OK
                    return true;
                } else {
                    logger.error("None of the services support deccryption of request messages.");
                    return false;
                }
            }
        }
        // If response is encrypted, encryption is done using the public
        // key of the receiver, so it must be possible to access it. However,
        // it's not possible to check the public keys, because we don't know
        // the ID's of the service consumers

        logger.info("Private key encryption properties checked.");
        // Private key is not needed so return false
        return false;
    }
}
