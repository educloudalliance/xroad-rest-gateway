package com.pkrete.restgateway.util;

import com.pkrete.restgateway.endpoint.AbstractEndpoint;
import com.pkrete.xrd4j.common.util.MessageHelper;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides utility methods for REST Gateway implementation.
 *
 * @author Petteri Kivimäki
 */
public class RESTGatewayUtil {

    private static final Logger logger = LoggerFactory.getLogger(RESTGatewayUtil.class);

    /**
     * Constructs and initializes a new RESTGatewayUtil object. Should never be
     * used.
     */
    private RESTGatewayUtil() {
    }

    /**
     * Checks the given content type and returns true if and only if it begins
     * with "text/xml" or "application/xml". Otherwise returns false.
     *
     * @param contentType content type to be checked
     * @return returns true if and only if the given content type begins with
     * "text/xml" or "application/xml"; otherwise returns false
     */
    public static boolean isXml(String contentType) {
        return contentType != null && (contentType.startsWith(Constants.TEXT_XML) || contentType.startsWith(Constants.APPLICATION_XML));
    }

    /**
     * Checks the given content type and returns true if and only if it begins
     * with "text/xml", "application/xml" or "application/json". Otherwise
     * returns false.
     *
     * @param contentType content type to be checked
     * @return returns true if and only if the given content type begins with
     * "text/xml", "application/xml" or "application/json; otherwise returns
     * false
     */
    public static boolean isValidContentType(String contentType) {
        return contentType != null && (contentType.startsWith(Constants.TEXT_XML) || contentType.startsWith(Constants.APPLICATION_XML) || contentType.startsWith(Constants.APPLICATION_JSON));
    }

    /**
     * Extracts properties common for both consumer and provider endpoints from
     * the given properties.
     *
     * @param key property key
     * @param endpoints list of configured endpoints read from properties
     * @param endpoint the endpoint object that's being initialized
     */
    public static void extractEndpoints(String key, Properties endpoints, AbstractEndpoint endpoint) {
        // Wrapper processing
        if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_WRAPPERS)) {
            String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_WRAPPERS);
            endpoint.setProcessingWrappers(MessageHelper.strToBool(value));
            logger.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENDPOINT_PROPS_WRAPPERS, value);
        }
        // ServiceRequest namespace
        if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE)) {
            String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE);
            endpoint.setNamespaceDeserialize(value);
            logger.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE, value);
        }
        // ServiceResponse namespace
        if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE)) {
            String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE);
            endpoint.setNamespaceSerialize(value);
            logger.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE, value);
        }
        // ServiceResponse namespace prefix
        if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE)) {
            String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE);
            endpoint.setPrefix(value);
            logger.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE, value);
        }
    }
}
