package com.pkrete.restgateway.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides utility methods for REST Gateway implementation.
 *
 * @author Petteri Kivimäki
 */
public class RESTGatewayUtil {

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

}
