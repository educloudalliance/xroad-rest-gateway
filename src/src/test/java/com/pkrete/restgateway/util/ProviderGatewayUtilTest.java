package com.pkrete.restgateway.util;

import com.pkrete.xrd4j.common.exception.XRd4JException;
import com.pkrete.xrd4j.common.member.ConsumerMember;
import com.pkrete.xrd4j.common.member.ProducerMember;
import com.pkrete.xrd4j.common.message.ServiceRequest;
import com.pkrete.restgateway.endpoint.ProviderEndpoint;
import java.util.Map;
import java.util.Properties;
import junit.framework.TestCase;

/**
 * Test cases for ProviderGatewayUtil class.
 *
 * @author Petteri Kivimäki
 */
public class ProviderGatewayUtilTest extends TestCase {

    private Map<String, ProviderEndpoint> map;

    /**
     * Initializes instance variables for test cases.
     *
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Properties props = new Properties();
        Properties endpoints = new Properties();
        // Set up default properties
        props.put("wsdl.path", "provider-gateway.wsdl");
        props.put(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE, "http://serialize.com");
        props.put(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE, "ts1");
        props.put(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE, "http://deserialize.com");
        // Set up endpoints
        endpoints.put("0." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1");
        endpoints.put("0." + Constants.PROVIDER_PROPS_URL, "http://www.hel.fi/palvelukarttaws/rest/v2/organization/");

        endpoints.put("1." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.getOrganization.v1");
        endpoints.put("1." + Constants.PROVIDER_PROPS_URL, "http://www.hel.fi/palvelukarttaws/rest/v2/organization/");
        endpoints.put("1." + Constants.ENDPOINT_PROPS_VERB, "post");
        endpoints.put("1." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE, "http://serialize.com/custom");
        endpoints.put("1." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE, "test");
        endpoints.put("1." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE, "http://deserialize.com/custom");

        endpoints.put("2." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.Demo2Service.getWeather.v1");
        endpoints.put("2." + Constants.PROVIDER_PROPS_URL, "http://weather.com/");
        endpoints.put("2." + Constants.PROVIDER_PROPS_ACCEPT, Constants.APPLICATION_JSON);
        endpoints.put("2." + Constants.PROVIDER_PROPS_ATTACHMENT, "true");
        endpoints.put("2." + Constants.PROVIDER_PROPS_CONTENT_TYPE, Constants.APPLICATION_JSON);
        endpoints.put("2." + Constants.PROVIDER_PROPS_SEND_XRD_HEADERS, "false");
        endpoints.put("2." + Constants.ENDPOINT_PROPS_REQUEST_ENCRYPTED, "true");
        endpoints.put("2." + Constants.ENDPOINT_PROPS_RESPONSE_ENCRYPTED, "true");

        endpoints.put("3." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.getWeather");
        endpoints.put("3." + Constants.PROVIDER_PROPS_URL, "");

        endpoints.put("4." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.Demo2Service.testService.v1");

        // Extract endpoints
        this.map = ProviderGatewayUtil.extractProviders(endpoints, props);
    }

    /**
     * The first endpoint on the list. No overridden properties.
     *
     * @throws XRd4JException if there's a XRd4J error
     */
    public void testExtractConsumer0() throws XRd4JException {
        ProviderEndpoint temp = this.map.get("FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1");
        assertEquals(false, temp == null);
        assertEquals("FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1", temp.getServiceId());
        assertEquals("http://www.hel.fi/palvelukarttaws/rest/v2/organization/", temp.getUrl());
        assertEquals("get", temp.getHttpVerb());
        assertEquals("http://serialize.com", temp.getNamespaceSerialize());
        assertEquals("ts1", temp.getPrefix());
        assertEquals("http://deserialize.com", temp.getNamespaceDeserialize());
        assertEquals(null, temp.getAccept());
        assertEquals(null, temp.getContentType());
        assertEquals(false, temp.isAttachment());
        assertEquals(true, temp.isSendXrdHeaders());
        assertEquals(false, temp.isRequestEncrypted());
        assertEquals(false, temp.isResponseEncrypted());
    }

    /**
     * The second endpoint on the list. Namespace properties overridden.
     *
     * @throws XRd4JException if there's a XRd4J error
     */
    public void testExtractConsumer1() throws XRd4JException {
        ProviderEndpoint temp = this.map.get("FI_PILOT.GOV.1019125-0.getOrganization.v1");
        assertEquals(false, temp == null);
        assertEquals("FI_PILOT.GOV.1019125-0.getOrganization.v1", temp.getServiceId());
        assertEquals("http://www.hel.fi/palvelukarttaws/rest/v2/organization/", temp.getUrl());
        assertEquals("post", temp.getHttpVerb());
        assertEquals("http://serialize.com/custom", temp.getNamespaceSerialize());
        assertEquals("test", temp.getPrefix());
        assertEquals("http://deserialize.com/custom", temp.getNamespaceDeserialize());
        assertEquals(null, temp.getAccept());
        assertEquals(null, temp.getContentType());
        assertEquals(false, temp.isAttachment());
        assertEquals(true, temp.isSendXrdHeaders());
        assertEquals(false, temp.isRequestEncrypted());
        assertEquals(false, temp.isResponseEncrypted());
    }

    /**
     * The third endpoint on the list. All the properties set.
     *
     * @throws XRd4JException if there's a XRd4J error
     */
    public void testExtractConsumer3() throws XRd4JException {
        ProviderEndpoint temp = this.map.get("FI_PILOT.GOV.1019125-0.Demo2Service.getWeather.v1");
        assertEquals(false, temp == null);
        assertEquals("FI_PILOT.GOV.1019125-0.Demo2Service.getWeather.v1", temp.getServiceId());
        assertEquals("http://weather.com/", temp.getUrl());
        assertEquals("get", temp.getHttpVerb());
        assertEquals("http://serialize.com", temp.getNamespaceSerialize());
        assertEquals("ts1", temp.getPrefix());
        assertEquals("http://deserialize.com", temp.getNamespaceDeserialize());
        assertEquals(Constants.APPLICATION_JSON, temp.getAccept());
        assertEquals(Constants.APPLICATION_JSON, temp.getContentType());
        assertEquals(true, temp.isAttachment());
        assertEquals(false, temp.isSendXrdHeaders());
        assertEquals(true, temp.isRequestEncrypted());
        assertEquals(true, temp.isResponseEncrypted());
    }

    /**
     * The fourth endpoint on the list. URL is empty -> endpoint not loaded.
     *
     * @throws XRd4JException if there's a XRd4J error
     */
    public void testExtractConsumer4() throws XRd4JException {
        ProviderEndpoint temp = this.map.get("FI_PILOT.GOV.1019125-0.getWeather");
        assertEquals(true, temp == null);
    }

    /**
     * The fourth endpoint on the list. URL is null -> endpoint not loaded.
     *
     * @throws XRd4JException if there's a XRd4J error
     */
    public void testExtractConsumer5() throws XRd4JException {
        ProviderEndpoint temp = this.map.get("FI_PILOT.GOV.1019125-0.Demo2Service.testService.v1");
        assertEquals(true, temp == null);
    }

    /**
     * Test generation of HTTP headers. Only X-Road headers.
     *
     * @throws com.pkrete.xrd4j.common.exception.XRd4JException
     */
    public void testGenerateHtmlHeaders1() throws XRd4JException {
        ConsumerMember consumer = new ConsumerMember("FI_PILOT", "GOV", "0245437-2", "ConsumerService");
        ProducerMember producer = new ProducerMember("FI_PILOT", "GOV", "1019125-0", "Demo2Service", "getOrganizationList", "v1");
        ServiceRequest request = new ServiceRequest(consumer, producer, "12345");
        request.setUserId("test-user");
        Map<String, String> headers = ProviderGatewayUtil.generateHttpHeaders(request, this.map.get("FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1"));
        assertEquals("FI_PILOT.GOV.0245437-2.ConsumerService", headers.get(Constants.XRD_HEADER_CLIENT));
        assertEquals("FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1", headers.get(Constants.XRD_HEADER_SERVICE));
        assertEquals("12345", headers.get(Constants.XRD_HEADER_MESSAGE_ID));
        assertEquals("test-user", headers.get(Constants.XRD_HEADER_USER_ID));
        assertEquals(null, headers.get(Constants.HTTP_HEADER_CONTENT_TYPE));
        assertEquals(null, headers.get(Constants.HTTP_HEADER_ACCEPT));
    }

    /**
     * Test generation of HTTP headers. No X-Road headers.
     *
     * @throws com.pkrete.xrd4j.common.exception.XRd4JException
     */
    public void testGenerateHtmlHeaders2() throws XRd4JException {
        ConsumerMember consumer = new ConsumerMember("FI_PILOT", "GOV", "0245437-2", "ConsumerService");
        ProducerMember producer = new ProducerMember("FI_PILOT", "GOV", "1019125-0", "Demo2Service", "getWeather", "v1");
        ServiceRequest request = new ServiceRequest(consumer, producer, "12345");
        request.setUserId("test-user");
        Map<String, String> headers = ProviderGatewayUtil.generateHttpHeaders(request, this.map.get("FI_PILOT.GOV.1019125-0.Demo2Service.getWeather.v1"));
        assertEquals(null, headers.get(Constants.XRD_HEADER_CLIENT));
        assertEquals(null, headers.get(Constants.XRD_HEADER_SERVICE));
        assertEquals(null, headers.get(Constants.XRD_HEADER_MESSAGE_ID));
        assertEquals(null, headers.get(Constants.XRD_HEADER_USER_ID));
        assertEquals(Constants.APPLICATION_JSON, headers.get(Constants.HTTP_HEADER_CONTENT_TYPE));
        assertEquals(Constants.APPLICATION_JSON, headers.get(Constants.HTTP_HEADER_ACCEPT));
    }
}
