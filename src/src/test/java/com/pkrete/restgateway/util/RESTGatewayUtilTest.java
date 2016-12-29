package com.pkrete.restgateway.util;

import junit.framework.TestCase;

/**
 * Test cases for RESTGatewayUtil class.
 *
 * @author Petteri Kivimäki
 */
public class RESTGatewayUtilTest extends TestCase {

    /**
     * Test for null value.
     */
    public void testIsXml1() {
        assertEquals(false, RESTGatewayUtil.isXml(null));
    }

    /**
     * Test for an empty string.
     */
    public void testIsXml2() {
        assertEquals(false, RESTGatewayUtil.isXml(""));
    }

    /**
     * Test for "string/xml".
     */
    public void testIsXml3() {
        assertEquals(true, RESTGatewayUtil.isXml("text/xml"));
    }

    /**
     * Test for "application/json".
     */
    public void testIsXml4() {
        assertEquals(false, RESTGatewayUtil.isXml("application/json"));
    }

    /**
     * Test for "application/XML". Wrong case.
     */
    public void testIsXml5() {
        assertEquals(false, RESTGatewayUtil.isXml("application/XML"));
    }

    /**
     * Test for "text/html".
     */
    public void testIsXml6() {
        assertEquals(false, RESTGatewayUtil.isXml("text/html"));
    }
}
