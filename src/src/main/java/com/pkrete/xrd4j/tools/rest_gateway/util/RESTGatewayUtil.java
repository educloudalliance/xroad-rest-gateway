package com.pkrete.xrd4j.tools.rest_gateway.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides utility methods for REST Gateway implementation.
 *
 * @author Petteri Kivimäki
 */
public class RESTGatewayUtil {

    private final static Logger logger = LoggerFactory.getLogger(RESTGatewayUtil.class);

    /**
     * Checks the given content type and returns true if and only if it begins
     * with "text/xml" or "application/xml". Otherwise returns false.
     * @param contentType content type to be checked
     * @return returns true if and only if the given content type begins with
     * "text/xml" or "application/xml"; otherwise returns false
     */
    public static boolean isXml(String contentType) {
        if (contentType != null && (contentType.startsWith("text/xml") || contentType.startsWith("application/xml"))) {
            return true;
        }
        return false;
    }


}
