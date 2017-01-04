package com.pkrete.restgateway.util;

import com.pkrete.xrd4j.common.exception.XRd4JException;
import com.pkrete.xrd4j.common.member.ConsumerMember;
import com.pkrete.xrd4j.common.member.ProducerMember;
import com.pkrete.xrd4j.common.util.SOAPHelper;
import com.pkrete.restgateway.endpoint.ConsumerEndpoint;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;
import junit.framework.TestCase;

/**
 * Test cases for ConsumerGatewayUtil class.
 *
 * Running these tests cause the below error message to appear on the screen.
 * The error message is expected and can be ignored. The error message:
 *
 * "[Fatal Error] :1:xx: The markup in the document following the root element
 * must be well-formed."
 *
 * @author Petteri Kivimäki
 */
public class ConsumerGatewayUtilTest extends TestCase {

    private String servletUrl;
    private Map<String, ConsumerEndpoint> map;

    /**
     * Initializes instance variables for test cases.
     *
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.servletUrl = "http://localhost:8080/RESTGateway/Consumer/";
        Properties props = new Properties();
        Properties endpoints = new Properties();
        // Set up default properties
        props.put(Constants.CONSUMER_PROPS_ID_CLIENT, "FI_PILOT.GOV.0245437-2.ConsumerService");
        props.put(Constants.CONSUMER_PROPS_SECURITY_SERVER_URL, "http://localhost:8080/RESTGateway/Provider");
        props.put(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE, "http://serialize.com");
        props.put(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE, "ts1");
        props.put(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE, "http://deserialize.com");
        // Set up endpoints
        endpoints.put("0." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1");
        endpoints.put("0." + Constants.ENDPOINT_PROPS_VERB, "get");
        endpoints.put("0." + Constants.CONSUMER_PROPS_PATH, "/www.hel.fi/palvelukarttaws/rest/v2/organization/");

        endpoints.put("1." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.Demo2Service.getOrganization.v1");
        endpoints.put("1." + Constants.ENDPOINT_PROPS_VERB, "get");
        endpoints.put("1." + Constants.CONSUMER_PROPS_PATH, "/www.hel.fi/palvelukarttaws/rest/v2/organization/{resourceId}");
        endpoints.put("1." + Constants.CONSUMER_PROPS_ID_CLIENT, "FI_PILOT.GOV.0245437-2.TestService");
        endpoints.put("1." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE, "http://serialize.com/custom");
        endpoints.put("1." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE, "test");
        endpoints.put("1." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE, "http://deserialize.com/custom");

        endpoints.put("2." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.Demo2Service.getCompany.v1");
        endpoints.put("2." + Constants.ENDPOINT_PROPS_VERB, "get");
        endpoints.put("2." + Constants.CONSUMER_PROPS_PATH, "/avoindata.prh.fi/opendata/bis/v1/{resourceId}");
        endpoints.put("2." + Constants.CONSUMER_PROPS_MOD_URL, "true");
        endpoints.put("2." + Constants.ENDPOINT_PROPS_REQUEST_ENCRYPTED, "true");
        endpoints.put("2." + Constants.ENDPOINT_PROPS_RESPONSE_ENCRYPTED, "true");

        endpoints.put("3." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.testApi.v1");
        endpoints.put("3." + Constants.ENDPOINT_PROPS_VERB, "get");
        endpoints.put("3." + Constants.CONSUMER_PROPS_PATH, "/test.com/api");

        endpoints.put("4." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.Demo2Service.exampleApi.v1");
        endpoints.put("4." + Constants.ENDPOINT_PROPS_VERB, "get");
        endpoints.put("4." + Constants.CONSUMER_PROPS_PATH, "/example.com/api");
        endpoints.put("4." + Constants.CONSUMER_PROPS_ID_CLIENT, "FI_PILOT.GOV0245437-2");
        // Extract endpoints
        this.map = ConsumerGatewayUtil.extractConsumers(endpoints, props);
    }

    /**
     * Test parsing client ids from string.
     *
     * @throws XRd4JException if there's a XRd4J error
     */
    public void testParseConsumer1() throws XRd4JException {
        String clientId = "FI_PILOT.GOV.0245437-2";
        ConsumerEndpoint consumerEndpoint = new ConsumerEndpoint(null, clientId, null);
        assertEquals(true, ConsumerGatewayUtil.setConsumerMember(consumerEndpoint));
        assertEquals(clientId, consumerEndpoint.getConsumer().toString());
        assertEquals("FI_PILOT", consumerEndpoint.getConsumer().getXRoadInstance());
        assertEquals("GOV", consumerEndpoint.getConsumer().getMemberClass());
        assertEquals("0245437-2", consumerEndpoint.getConsumer().getMemberCode());
        assertEquals(null, consumerEndpoint.getConsumer().getSubsystemCode());

        clientId = "FI_PILOT.GOV.0245437-2.ConsumerService";
        consumerEndpoint = new ConsumerEndpoint(null, clientId, null);
        assertEquals(true, ConsumerGatewayUtil.setConsumerMember(consumerEndpoint));
        assertEquals(clientId, consumerEndpoint.getConsumer().toString());
        assertEquals("FI_PILOT", consumerEndpoint.getConsumer().getXRoadInstance());
        assertEquals("GOV", consumerEndpoint.getConsumer().getMemberClass());
        assertEquals("0245437-2", consumerEndpoint.getConsumer().getMemberCode());
        assertEquals("ConsumerService", consumerEndpoint.getConsumer().getSubsystemCode());

        clientId = "FI_PILOT.GOV.0245437-2.ConsumerService.getOrganizationList";
        consumerEndpoint = new ConsumerEndpoint(null, clientId, null);
        assertEquals(false, ConsumerGatewayUtil.setConsumerMember(consumerEndpoint));

        clientId = "FI_PILOT.GOV.0245437-2.ConsumerService.getOrganizationList.v1";
        consumerEndpoint = new ConsumerEndpoint(null, clientId, null);
        assertEquals(false, ConsumerGatewayUtil.setConsumerMember(consumerEndpoint));

        clientId = "FI_PILOT.GOV";
        consumerEndpoint = new ConsumerEndpoint(null, clientId, null);
        assertEquals(false, ConsumerGatewayUtil.setConsumerMember(consumerEndpoint));
    }

    /**
     * Test parsing service ids from string.
     *
     * @throws XRd4JException if there's a XRd4J error
     */
    public void testParseProducer1() throws XRd4JException {
        String serviceId = "FI_PILOT.GOV.0245437-2.ConsumerService.getOrganizationList.v1";
        ConsumerEndpoint consumerEndpoint = new ConsumerEndpoint(serviceId, null, null);
        assertEquals(true, ConsumerGatewayUtil.setProducerMember(consumerEndpoint));
        assertEquals(serviceId, consumerEndpoint.getProducer().toString());
        assertEquals("FI_PILOT", consumerEndpoint.getProducer().getXRoadInstance());
        assertEquals("GOV", consumerEndpoint.getProducer().getMemberClass());
        assertEquals("0245437-2", consumerEndpoint.getProducer().getMemberCode());
        assertEquals("ConsumerService", consumerEndpoint.getProducer().getSubsystemCode());
        assertEquals("getOrganizationList", consumerEndpoint.getProducer().getServiceCode());
        assertEquals("v1", consumerEndpoint.getProducer().getServiceVersion());

        serviceId = "FI_PILOT.GOV.0245437-2.ConsumerService2.getOrganizationList.V1";
        consumerEndpoint = new ConsumerEndpoint(serviceId, null, null);
        assertEquals(true, ConsumerGatewayUtil.setProducerMember(consumerEndpoint));
        assertEquals(serviceId, consumerEndpoint.getProducer().toString());
        assertEquals("FI_PILOT", consumerEndpoint.getProducer().getXRoadInstance());
        assertEquals("GOV", consumerEndpoint.getProducer().getMemberClass());
        assertEquals("0245437-2", consumerEndpoint.getProducer().getMemberCode());
        assertEquals("ConsumerService2", consumerEndpoint.getProducer().getSubsystemCode());
        assertEquals("getOrganizationList", consumerEndpoint.getProducer().getServiceCode());
        assertEquals("V1", consumerEndpoint.getProducer().getServiceVersion());

        serviceId = "FI_PILOT.GOV.0245437-2.ConsumerService2.getOrganizationList.V1_0";
        consumerEndpoint = new ConsumerEndpoint(serviceId, null, null);
        assertEquals(true, ConsumerGatewayUtil.setProducerMember(consumerEndpoint));
        assertEquals(serviceId, consumerEndpoint.getProducer().toString());
        assertEquals("FI_PILOT", consumerEndpoint.getProducer().getXRoadInstance());
        assertEquals("GOV", consumerEndpoint.getProducer().getMemberClass());
        assertEquals("0245437-2", consumerEndpoint.getProducer().getMemberCode());
        assertEquals("ConsumerService2", consumerEndpoint.getProducer().getSubsystemCode());
        assertEquals("getOrganizationList", consumerEndpoint.getProducer().getServiceCode());
        assertEquals("V1_0", consumerEndpoint.getProducer().getServiceVersion());

        serviceId = "FI_PILOT.GOV.0245437-2.ConsumerService.getOrganizationList1";
        consumerEndpoint = new ConsumerEndpoint(serviceId, null, null);
        assertEquals(true, ConsumerGatewayUtil.setProducerMember(consumerEndpoint));
        assertEquals(serviceId, consumerEndpoint.getProducer().toString());
        assertEquals("FI_PILOT", consumerEndpoint.getProducer().getXRoadInstance());
        assertEquals("GOV", consumerEndpoint.getProducer().getMemberClass());
        assertEquals("0245437-2", consumerEndpoint.getProducer().getMemberCode());
        assertEquals("ConsumerService", consumerEndpoint.getProducer().getSubsystemCode());
        assertEquals("getOrganizationList1", consumerEndpoint.getProducer().getServiceCode());
        assertEquals(null, consumerEndpoint.getProducer().getServiceVersion());

        serviceId = "FI_PILOT.GOV.0245437-2.getOrganizationList2";
        consumerEndpoint = new ConsumerEndpoint(serviceId, null, null);
        assertEquals(true, ConsumerGatewayUtil.setProducerMember(consumerEndpoint));
        assertEquals(serviceId, consumerEndpoint.getProducer().toString());
        assertEquals("FI_PILOT", consumerEndpoint.getProducer().getXRoadInstance());
        assertEquals("GOV", consumerEndpoint.getProducer().getMemberClass());
        assertEquals("0245437-2", consumerEndpoint.getProducer().getMemberCode());
        assertEquals(null, consumerEndpoint.getProducer().getSubsystemCode());
        assertEquals("getOrganizationList2", consumerEndpoint.getProducer().getServiceCode());
        assertEquals(null, consumerEndpoint.getProducer().getServiceVersion());

        serviceId = "FI_PILOT.GOV.0245437-2.getOrganizationList.v1";
        consumerEndpoint = new ConsumerEndpoint(serviceId, null, null);
        assertEquals(true, ConsumerGatewayUtil.setProducerMember(consumerEndpoint));
        assertEquals(serviceId, consumerEndpoint.getProducer().toString());
        assertEquals("FI_PILOT", consumerEndpoint.getProducer().getXRoadInstance());
        assertEquals("GOV", consumerEndpoint.getProducer().getMemberClass());
        assertEquals("0245437-2", consumerEndpoint.getProducer().getMemberCode());
        assertEquals(null, consumerEndpoint.getProducer().getSubsystemCode());
        assertEquals("getOrganizationList", consumerEndpoint.getProducer().getServiceCode());
        assertEquals("v1", consumerEndpoint.getProducer().getServiceVersion());

        serviceId = "FI_PILOT.GOV.0245437-2.getOrganizationList.V11";
        consumerEndpoint = new ConsumerEndpoint(serviceId, null, null);
        assertEquals(true, ConsumerGatewayUtil.setProducerMember(consumerEndpoint));
        assertEquals(serviceId, consumerEndpoint.getProducer().toString());
        assertEquals("FI_PILOT", consumerEndpoint.getProducer().getXRoadInstance());
        assertEquals("GOV", consumerEndpoint.getProducer().getMemberClass());
        assertEquals("0245437-2", consumerEndpoint.getProducer().getMemberCode());
        assertEquals(null, consumerEndpoint.getProducer().getSubsystemCode());
        assertEquals("getOrganizationList", consumerEndpoint.getProducer().getServiceCode());
        assertEquals("V11", consumerEndpoint.getProducer().getServiceVersion());

        serviceId = "FI_PILOT.GOV.0245437-2.getOrganizationList.1";
        consumerEndpoint = new ConsumerEndpoint(serviceId, null, null);
        assertEquals(true, ConsumerGatewayUtil.setProducerMember(consumerEndpoint));
        assertEquals(serviceId, consumerEndpoint.getProducer().toString());
        assertEquals("FI_PILOT", consumerEndpoint.getProducer().getXRoadInstance());
        assertEquals("GOV", consumerEndpoint.getProducer().getMemberClass());
        assertEquals("0245437-2", consumerEndpoint.getProducer().getMemberCode());
        assertEquals(null, consumerEndpoint.getProducer().getSubsystemCode());
        assertEquals("getOrganizationList", consumerEndpoint.getProducer().getServiceCode());
        assertEquals("1", consumerEndpoint.getProducer().getServiceVersion());

        serviceId = "FI_PILOT.GOV.0245437-2.getOrganizationList.1_0";
        consumerEndpoint = new ConsumerEndpoint(serviceId, null, null);
        assertEquals(true, ConsumerGatewayUtil.setProducerMember(consumerEndpoint));
        assertEquals(serviceId, consumerEndpoint.getProducer().toString());
        assertEquals("FI_PILOT", consumerEndpoint.getProducer().getXRoadInstance());
        assertEquals("GOV", consumerEndpoint.getProducer().getMemberClass());
        assertEquals("0245437-2", consumerEndpoint.getProducer().getMemberCode());
        assertEquals(null, consumerEndpoint.getProducer().getSubsystemCode());
        assertEquals("getOrganizationList", consumerEndpoint.getProducer().getServiceCode());
        assertEquals("1_0", consumerEndpoint.getProducer().getServiceVersion());

        serviceId = "FI_PILOT.GOV.0245437-2.getOrganizationList.1-0";
        consumerEndpoint = new ConsumerEndpoint(serviceId, null, null);
        assertEquals(true, ConsumerGatewayUtil.setProducerMember(consumerEndpoint));
        assertEquals(serviceId, consumerEndpoint.getProducer().toString());
        assertEquals("FI_PILOT", consumerEndpoint.getProducer().getXRoadInstance());
        assertEquals("GOV", consumerEndpoint.getProducer().getMemberClass());
        assertEquals("0245437-2", consumerEndpoint.getProducer().getMemberCode());
        assertEquals(null, consumerEndpoint.getProducer().getSubsystemCode());
        assertEquals("getOrganizationList", consumerEndpoint.getProducer().getServiceCode());
        assertEquals("1-0", consumerEndpoint.getProducer().getServiceVersion());

        serviceId = "FI_PILOT.GOV.0245437-2.getOrganizationList.ve1";
        consumerEndpoint = new ConsumerEndpoint(serviceId, null, null);
        assertEquals(true, ConsumerGatewayUtil.setProducerMember(consumerEndpoint));
        assertEquals(serviceId, consumerEndpoint.getProducer().toString());
        assertEquals("FI_PILOT", consumerEndpoint.getProducer().getXRoadInstance());
        assertEquals("GOV", consumerEndpoint.getProducer().getMemberClass());
        assertEquals("0245437-2", consumerEndpoint.getProducer().getMemberCode());
        assertEquals("getOrganizationList", consumerEndpoint.getProducer().getSubsystemCode());
        assertEquals("ve1", consumerEndpoint.getProducer().getServiceCode());
        assertEquals(null, consumerEndpoint.getProducer().getServiceVersion());

        serviceId = "FI_PILOT.GOV";
        consumerEndpoint = new ConsumerEndpoint(serviceId, null, null);
        assertEquals(false, ConsumerGatewayUtil.setProducerMember(consumerEndpoint));

        serviceId = "FI_PILOT.GOV.0245437-2";
        consumerEndpoint = new ConsumerEndpoint(serviceId, null, null);
        assertEquals(false, ConsumerGatewayUtil.setProducerMember(consumerEndpoint));

        serviceId = "FI_PILOT.GOV.0245437-2.subsystem.service.v1.0";
        consumerEndpoint = new ConsumerEndpoint(serviceId, null, null);
        assertEquals(false, ConsumerGatewayUtil.setProducerMember(consumerEndpoint));
    }

    /**
     * The first endpoint on the list. No overridden properties.
     *
     * @throws XRd4JException if there's a XRd4J error
     */
    public void testExtractConsumer0() throws XRd4JException {
        ConsumerEndpoint temp = ConsumerGatewayUtil.findMatch("GET /www.hel.fi/palvelukarttaws/rest/v2/organization/", map);
        assertEquals(false, temp == null);
        assertEquals("FI_PILOT.GOV.0245437-2.ConsumerService", temp.getClientId());
        assertEquals("FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1", temp.getServiceId());
        assertEquals("/www.hel.fi/palvelukarttaws/rest/v2/organization/", temp.getResourcePath());
        assertEquals(null, temp.getResourceId());
        assertEquals(false, temp.isModifyUrl());
        assertEquals("http://serialize.com", temp.getNamespaceSerialize());
        assertEquals("ts1", temp.getPrefix());
        assertEquals("http://deserialize.com", temp.getNamespaceDeserialize());
        assertEquals(false, temp.isRequestEncrypted());
        assertEquals(false, temp.isResponseEncrypted());
        assertEquals(new ConsumerMember("FI_PILOT", "GOV", "0245437-2", "ConsumerService").toString(), temp.getConsumer().toString());
        assertEquals(new ProducerMember("FI_PILOT", "GOV", "1019125-0", "Demo2Service", "getOrganizationList", "v1").toString(), temp.getProducer().toString());
    }

    /**
     * The second endpoint on the list. Overridden properties.
     *
     * @throws XRd4JException if there's a XRd4J error
     */
    public void testExtractConsumer1() throws XRd4JException {
        ConsumerEndpoint temp = ConsumerGatewayUtil.findMatch("GET /www.hel.fi/palvelukarttaws/rest/v2/organization/49/", map);
        assertEquals(false, temp == null);
        assertEquals("FI_PILOT.GOV.0245437-2.TestService", temp.getClientId());
        assertEquals("FI_PILOT.GOV.1019125-0.Demo2Service.getOrganization.v1", temp.getServiceId());
        assertEquals("/www.hel.fi/palvelukarttaws/rest/v2/organization/{resourceId}/", temp.getResourcePath());
        assertEquals("49", temp.getResourceId());
        assertEquals(false, temp.isModifyUrl());
        assertEquals("http://serialize.com/custom", temp.getNamespaceSerialize());
        assertEquals("test", temp.getPrefix());
        assertEquals("http://deserialize.com/custom", temp.getNamespaceDeserialize());
        assertEquals(false, temp.isRequestEncrypted());
        assertEquals(false, temp.isResponseEncrypted());
        assertEquals(new ConsumerMember("FI_PILOT", "GOV", "0245437-2", "TestService").toString(), temp.getConsumer().toString());
        assertEquals(new ProducerMember("FI_PILOT", "GOV", "1019125-0", "Demo2Service", "getOrganization", "v1").toString(), temp.getProducer().toString());
    }

    /**
     * The third endpoint on the list. No overridden properties.
     *
     * @throws XRd4JException if there's a XRd4J error
     */
    public void testExtractConsumer2() throws XRd4JException {
        ConsumerEndpoint temp = ConsumerGatewayUtil.findMatch("GET /avoindata.prh.fi/opendata/bis/v1/12345-6/", map);
        assertEquals(false, temp == null);
        assertEquals("FI_PILOT.GOV.0245437-2.ConsumerService", temp.getClientId());
        assertEquals("FI_PILOT.GOV.1019125-0.Demo2Service.getCompany.v1", temp.getServiceId());
        assertEquals("/avoindata.prh.fi/opendata/bis/v1/{resourceId}/", temp.getResourcePath());
        assertEquals("12345-6", temp.getResourceId());
        assertEquals(true, temp.isModifyUrl());
        assertEquals("http://serialize.com", temp.getNamespaceSerialize());
        assertEquals("ts1", temp.getPrefix());
        assertEquals("http://deserialize.com", temp.getNamespaceDeserialize());
        assertEquals(true, temp.isRequestEncrypted());
        assertEquals(true, temp.isResponseEncrypted());
        assertEquals(new ConsumerMember("FI_PILOT", "GOV", "0245437-2", "ConsumerService").toString(), temp.getConsumer().toString());
        assertEquals(new ProducerMember("FI_PILOT", "GOV", "1019125-0", "Demo2Service", "getCompany", "v1").toString(), temp.getProducer().toString());
    }

    /**
     * The fourth endpoint on the list. Invalid configuration - service id lacks
     * subsystem.
     *
     * @throws XRd4JException if there's a XRd4J error
     */
    public void testExtractConsumer4() throws XRd4JException {
        ConsumerEndpoint temp = ConsumerGatewayUtil.findMatch("GET /test.com/api/", map);
        assertEquals(false, temp == null);
        assertEquals("FI_PILOT.GOV.0245437-2.ConsumerService", temp.getClientId());
        assertEquals("FI_PILOT.GOV.1019125-0.testApi.v1", temp.getServiceId());
        assertEquals("/test.com/api/", temp.getResourcePath());
        assertEquals(null, temp.getResourceId());
        assertEquals(false, temp.isModifyUrl());
        assertEquals("http://serialize.com", temp.getNamespaceSerialize());
        assertEquals("ts1", temp.getPrefix());
        assertEquals("http://deserialize.com", temp.getNamespaceDeserialize());
        assertEquals(false, temp.isRequestEncrypted());
        assertEquals(false, temp.isResponseEncrypted());
        assertEquals(new ConsumerMember("FI_PILOT", "GOV", "0245437-2", "ConsumerService").toString(), temp.getConsumer().toString());
        ProducerMember producer = new ProducerMember("FI_PILOT", "GOV", "1019125-0", "null", "testApi", "v1");
        producer.setSubsystemCode(null);
        assertEquals(producer.toString(), temp.getProducer().toString());
    }

    /**
     * The fifth endpoint on the list. Invalid configuration - client id lacks
     * subsystem.
     *
     * @throws XRd4JException if there's a XRd4J error
     */
    public void testExtractConsumer5() throws XRd4JException {
        ConsumerEndpoint temp = ConsumerGatewayUtil.findMatch("GET /example.com/api/", map);
        assertEquals(true, temp == null);
    }

    /**
     * Modify JSON string containing URLs beginning with http. No resourceId.
     */
    public void testModifyUrl1() {
        String responseCorrect = "{\"urls\":[\"http://localhost:8080/RESTGateway/Consumer/example.com/second\", \"http://localhost:8080/RESTGateway/Consumer/example.com/second/\", \"http://localhost:8080/RESTGateway/Consumer/example.com/second?param1=value1\", \"http://localhost:8080/RESTGateway/Consumer/example.com/second/?param1=value1\", \"http://localhost:8080/RESTGateway/Consumer/example.com/second/12345\", \"http://localhost:8080/RESTGateway/Consumer/example.com/second/12345?param1=value1\", \"http://localhost:8080/RESTGateway/Consumer/example.com/second/12345/\", \"http://localhost:8080/RESTGateway/Consumer/example.com/second/12345/?param1=value1\"]}";
        String resourcePath = "/example.com/second/";
        String responseStr = "{\"urls\":[\"http://example.com/second\", \"http://example.com/second/\", \"http://example.com/second?param1=value1\", \"http://example.com/second/?param1=value1\", \"http://example.com/second/12345\", \"http://example.com/second/12345?param1=value1\", \"http://example.com/second/12345/\", \"http://example.com/second/12345/?param1=value1\"]}";
        responseStr = ConsumerGatewayUtil.rewriteUrl(servletUrl, resourcePath, responseStr);
        assertEquals(responseCorrect, responseStr);
    }

    /**
     * Modify JSON string containing URLs beginning with http. ResourceId is
     * defined.
     */
    public void testModifyUrl2() {
        String responseCorrect = "{\"urls\":[\"http://localhost:8080/RESTGateway/Consumer/example.com/second\", \"http://localhost:8080/RESTGateway/Consumer/example.com/second/\", \"http://localhost:8080/RESTGateway/Consumer/example.com/second?param1=value1\", \"http://localhost:8080/RESTGateway/Consumer/example.com/second/?param1=value1\", \"http://localhost:8080/RESTGateway/Consumer/example.com/second/12345\", \"http://localhost:8080/RESTGateway/Consumer/example.com/second/12345?param1=value1\", \"http://localhost:8080/RESTGateway/Consumer/example.com/second/12345/\", \"http://localhost:8080/RESTGateway/Consumer/example.com/second/12345/?param1=value1\"]}";
        String resourcePath = "/example.com/second/{resourceId}/";
        String responseStr = "{\"urls\":[\"http://example.com/second\", \"http://example.com/second/\", \"http://example.com/second?param1=value1\", \"http://example.com/second/?param1=value1\", \"http://example.com/second/12345\", \"http://example.com/second/12345?param1=value1\", \"http://example.com/second/12345/\", \"http://example.com/second/12345/?param1=value1\"]}";
        responseStr = ConsumerGatewayUtil.rewriteUrl(servletUrl, resourcePath, responseStr);
        assertEquals(responseCorrect, responseStr);
    }

    /**
     * Modify JSON string containing URLs beginning with https.
     */
    public void testModifyUrl3() {
        String responseCorrect = "{\"urls\":[\"https://localhost:8080/RESTGateway/Consumer/example.com/second\", \"https://localhost:8080/RESTGateway/Consumer/example.com/second/\", \"https://localhost:8080/RESTGateway/Consumer/example.com/second?param1=value1\", \"https://localhost:8080/RESTGateway/Consumer/example.com/second/?param1=value1\", \"https://localhost:8080/RESTGateway/Consumer/example.com/second/12345\", \"https://localhost:8080/RESTGateway/Consumer/example.com/second/12345?param1=value1\", \"https://localhost:8080/RESTGateway/Consumer/example.com/second/12345/\", \"https://localhost:8080/RESTGateway/Consumer/example.com/second/12345/?param1=value1\"]}";
        String resourcePath = "/example.com/second/";
        String responseStr = "{\"urls\":[\"https://example.com/second\", \"https://example.com/second/\", \"https://example.com/second?param1=value1\", \"https://example.com/second/?param1=value1\", \"https://example.com/second/12345\", \"https://example.com/second/12345?param1=value1\", \"https://example.com/second/12345/\", \"https://example.com/second/12345/?param1=value1\"]}";
        this.servletUrl = this.servletUrl.replace("http", "https");
        responseStr = ConsumerGatewayUtil.rewriteUrl(servletUrl, resourcePath, responseStr);
        assertEquals(responseCorrect, responseStr);
    }

    /**
     * Modify JSON string containing URLs beginning with https. ResourceId is
     * defined.
     */
    public void testModifyUrl4() {
        String responseCorrect = "{\"urls\":[\"https://localhost:8080/RESTGateway/Consumer/example.com/second\", \"https://localhost:8080/RESTGateway/Consumer/example.com/second/\", \"https://localhost:8080/RESTGateway/Consumer/example.com/second?param1=value1\", \"https://localhost:8080/RESTGateway/Consumer/example.com/second/?param1=value1\", \"https://localhost:8080/RESTGateway/Consumer/example.com/second/12345\", \"https://localhost:8080/RESTGateway/Consumer/example.com/second/12345?param1=value1\", \"https://localhost:8080/RESTGateway/Consumer/example.com/second/12345/\", \"https://localhost:8080/RESTGateway/Consumer/example.com/second/12345/?param1=value1\"]}";
        String resourcePath = "/example.com/second/{resourceId}/";
        String responseStr = "{\"urls\":[\"https://example.com/second\", \"https://example.com/second/\", \"https://example.com/second?param1=value1\", \"https://example.com/second/?param1=value1\", \"https://example.com/second/12345\", \"https://example.com/second/12345?param1=value1\", \"https://example.com/second/12345/\", \"https://example.com/second/12345/?param1=value1\"]}";
        this.servletUrl = this.servletUrl.replace("http", "https");
        responseStr = ConsumerGatewayUtil.rewriteUrl(servletUrl, resourcePath, responseStr);
        assertEquals(responseCorrect, responseStr);
    }

    /**
     * Modify XML string containing URLs beginning with http. No resourceId.
     */
    public void testModifyUrl5() {
        String responseCorrect = "<urls><url>http://localhost:8080/RESTGateway/Consumer/example.com/second</url><url>http://localhost:8080/RESTGateway/Consumer/example.com/second/</url><url>http://localhost:8080/RESTGateway/Consumer/example.com/second?param1=value1</url><url>http://localhost:8080/RESTGateway/Consumer/example.com/second/?param1=value1</url><url>http://localhost:8080/RESTGateway/Consumer/example.com/second/12345</url><url>http://localhost:8080/RESTGateway/Consumer/example.com/second/12345?param1=value1</url><url>http://localhost:8080/RESTGateway/Consumer/example.com/second/12345/</url><url>http://localhost:8080/RESTGateway/Consumer/example.com/second/12345/?param1=value1</url></urls>";
        String resourcePath = "/example.com/second/";
        String responseStr = "<urls><url>http://example.com/second</url><url>http://example.com/second/</url><url>http://example.com/second?param1=value1</url><url>http://example.com/second/?param1=value1</url><url>http://example.com/second/12345</url><url>http://example.com/second/12345?param1=value1</url><url>http://example.com/second/12345/</url><url>http://example.com/second/12345/?param1=value1</url></urls>";
        responseStr = ConsumerGatewayUtil.rewriteUrl(servletUrl, resourcePath, responseStr);
        assertEquals(responseCorrect, responseStr);
    }

    /**
     * Modify XML string containing URLs beginning with http. ResourceId is
     * defined.
     */
    public void testModifyUrl6() {
        String responseCorrect = "<urls><url>http://localhost:8080/RESTGateway/Consumer/example.com/second</url><url>http://localhost:8080/RESTGateway/Consumer/example.com/second/</url><url>http://localhost:8080/RESTGateway/Consumer/example.com/second?param1=value1</url><url>http://localhost:8080/RESTGateway/Consumer/example.com/second/?param1=value1</url><url>http://localhost:8080/RESTGateway/Consumer/example.com/second/12345</url><url>http://localhost:8080/RESTGateway/Consumer/example.com/second/12345?param1=value1</url><url>http://localhost:8080/RESTGateway/Consumer/example.com/second/12345/</url><url>http://localhost:8080/RESTGateway/Consumer/example.com/second/12345/?param1=value1</url></urls>";
        String resourcePath = "/example.com/second/{resourceId}/";
        String responseStr = "<urls><url>http://example.com/second</url><url>http://example.com/second/</url><url>http://example.com/second?param1=value1</url><url>http://example.com/second/?param1=value1</url><url>http://example.com/second/12345</url><url>http://example.com/second/12345?param1=value1</url><url>http://example.com/second/12345/</url><url>http://example.com/second/12345/?param1=value1</url></urls>";
        responseStr = ConsumerGatewayUtil.rewriteUrl(servletUrl, resourcePath, responseStr);
        assertEquals(responseCorrect, responseStr);
    }

    /**
     * Modify XML string containing URLs beginning with https.
     */
    public void testModifyUrl7() {
        String responseCorrect = "<urls><url>https://localhost:8080/RESTGateway/Consumer/example.com/second</url><url>https://localhost:8080/RESTGateway/Consumer/example.com/second/</url><url>https://localhost:8080/RESTGateway/Consumer/example.com/second?param1=value1</url><url>https://localhost:8080/RESTGateway/Consumer/example.com/second/?param1=value1</url><url>https://localhost:8080/RESTGateway/Consumer/example.com/second/12345</url><url>https://localhost:8080/RESTGateway/Consumer/example.com/second/12345?param1=value1</url><url>https://localhost:8080/RESTGateway/Consumer/example.com/second/12345/</url><url>https://localhost:8080/RESTGateway/Consumer/example.com/second/12345/?param1=value1</url></urls>";
        String resourcePath = "/example.com/second/";
        String responseStr = "<urls><url>https://example.com/second</url><url>https://example.com/second/</url><url>https://example.com/second?param1=value1</url><url>https://example.com/second/?param1=value1</url><url>https://example.com/second/12345</url><url>https://example.com/second/12345?param1=value1</url><url>https://example.com/second/12345/</url><url>https://example.com/second/12345/?param1=value1</url></urls>";
        this.servletUrl = this.servletUrl.replace("http", "https");
        responseStr = ConsumerGatewayUtil.rewriteUrl(servletUrl, resourcePath, responseStr);
        assertEquals(responseCorrect, responseStr);
    }

    /**
     * Modify XML string containing URLs beginning with https. ResourceId is
     * defined.
     */
    public void testModifyUrl8() {
        String responseCorrect = "<urls><url>https://localhost:8080/RESTGateway/Consumer/example.com/second</url><url>https://localhost:8080/RESTGateway/Consumer/example.com/second/</url><url>https://localhost:8080/RESTGateway/Consumer/example.com/second?param1=value1</url><url>https://localhost:8080/RESTGateway/Consumer/example.com/second/?param1=value1</url><url>https://localhost:8080/RESTGateway/Consumer/example.com/second/12345</url><url>https://localhost:8080/RESTGateway/Consumer/example.com/second/12345?param1=value1</url><url>https://localhost:8080/RESTGateway/Consumer/example.com/second/12345/</url><url>https://localhost:8080/RESTGateway/Consumer/example.com/second/12345/?param1=value1</url></urls>";
        String resourcePath = "/example.com/second/{resourceId}/";
        String responseStr = "<urls><url>https://example.com/second</url><url>https://example.com/second/</url><url>https://example.com/second?param1=value1</url><url>https://example.com/second/?param1=value1</url><url>https://example.com/second/12345</url><url>https://example.com/second/12345?param1=value1</url><url>https://example.com/second/12345/</url><url>https://example.com/second/12345/?param1=value1</url></urls>";
        this.servletUrl = this.servletUrl.replace("http", "https");
        responseStr = ConsumerGatewayUtil.rewriteUrl(servletUrl, resourcePath, responseStr);
        assertEquals(responseCorrect, responseStr);
    }

    /**
     * Remove response tag and namespace prefix. No namespace.
     */
    public void testRemoveResponseTag1() {
        String source = "<response><param1>value1</param1><param2>value2</param2></response>";
        String result = "<param1>value1</param1><param2>value2</param2>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        assertEquals(null, SOAPHelper.xmlStrToSOAPElement(result));
    }

    /**
     * Remove response tag and namespace prefix. With namespace, no prefix.
     */
    public void testRemoveResponseTag2() {
        String source = "<response xmlns:ts1=\"http://test.com/ns\"><param1>value1</param1><param2>value2</param2></response>";
        String result = "<param1>value1</param1><param2>value2</param2>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        assertEquals(null, SOAPHelper.xmlStrToSOAPElement(result));
    }

    /**
     * Remove response tag and namespace prefix. With namespace and prefix.
     */
    public void testRemoveResponseTag3() {
        String source = "<ts1:response xmlns:ts1=\"http://test.com/ns\"><ts1:param1>value1</ts1:param1><ts1:param2>value2</ts1:param2></ts1:response>";
        String result = "<param1>value1</param1><param2>value2</param2>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        assertEquals(null, SOAPHelper.xmlStrToSOAPElement(result));
    }

    /**
     * Remove response tag and namespace prefix. Only response tag has namespace
     * and prefix.
     */
    public void testRemoveResponseTag4() {
        String source = "<ts1:response xmlns:ts1=\"http://test.com/ns\"><param1>value1</param1><param2>value2</param2></ts1:response>";
        String result = "<param1>value1</param1><param2>value2</param2>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        assertEquals(null, SOAPHelper.xmlStrToSOAPElement(result));
    }

    /**
     * Remove response tag and namespace prefix. Response tag has namespace and
     * prefix, one child has the same prefix.
     */
    public void testRemoveResponseTag5() {
        String source = "<ts1:response xmlns:ts1=\"http://test.com/ns\"><ts1:param1>value1</ts1:param1><param2>value2</param2></ts1:response>";
        String result = "<param1>value1</param1><param2>value2</param2>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        assertEquals(null, SOAPHelper.xmlStrToSOAPElement(result));
    }

    /**
     * Remove response tag and namespace prefix. Response tag has namespace and
     * prefix, one child has another namespace and prefix.
     */
    public void testRemoveResponseTag6() {
        String source = "<ts1:response xmlns:ts1=\"http://test.com/ns\"><ts2:param1 xmlns:ts2=\"http://test.com/ns2\">value1</ts2:param1><param2>value2</param2></ts1:response>";
        String result = "<ts2:param1 xmlns:ts2=\"http://test.com/ns2\">value1</ts2:param1><param2>value2</param2>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        assertEquals(null, SOAPHelper.xmlStrToSOAPElement(result));
    }

    /**
     * Remove response tag and namespace prefix. Response tag has namespace and
     * prefix, one child has another namespace and prefix, and another child has
     * reponse's namespace prefix.
     */
    public void testRemoveResponseTag7() {
        String source = "<ts1:response xmlns:ts1=\"http://test.com/ns\"><ts2:param1 xmlns:ts2=\"http://test.com/ns2\">value1</ts2:param1><ts1:param2>value2</ts1:value2></ts1:response>";
        String result = "<ts2:param1 xmlns:ts2=\"http://test.com/ns2\">value1</ts2:param1><param2>value2</value2>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        assertEquals(null, SOAPHelper.xmlStrToSOAPElement(result));
    }

    /**
     * Remove response tag and namespace prefix. Response tag has no namespace,
     * one child has namespace and prefix.
     */
    public void testRemoveResponseTag8() {
        String source = "<response><ts2:param1 xmlns:ts2=\"http://test.com/ns2\">value1</ts2:param1><param2>value2</param2></response>";
        String result = "<ts2:param1 xmlns:ts2=\"http://test.com/ns2\">value1</ts2:param1><param2>value2</param2>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        assertEquals(null, SOAPHelper.xmlStrToSOAPElement(result));
    }

    /**
     * Remove response tag and namespace prefix. Response tag has no namespace,
     * children are under another element that has namespace.
     */
    public void testRemoveResponseTag9() {
        String source = "<response><ts2:param1 xmlns:ts2=\"http://test.com/ns2\"><ts2:param2>value2</ts2:param2><param3>value3</param3></ts2:param1></response>";
        String result = "<ts2:param1 xmlns:ts2=\"http://test.com/ns2\"><ts2:param2>value2</ts2:param2><param3>value3</param3></ts2:param1>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        if (SOAPHelper.xmlStrToSOAPElement(result) == null) {
            fail("Response can't be null");
        }
    }

    /**
     * Remove response tag and namespace prefix. Response tag has namespace and
     * prefix, children are under another element that has namespace.
     */
    public void testRemoveResponseTag10() {
        String source = "<ts1:response xmlns:ts1=\"http://test.com/ns\"><ts2:param1 xmlns:ts2=\"http://test.com/ns2\"><ts2:param2>value2</ts2:param2><ts1:param3>value3</ts1:param3></ts2:param1></ts1:response>";
        String result = "<ts2:param1 xmlns:ts2=\"http://test.com/ns2\"><ts2:param2>value2</ts2:param2><param3>value3</param3></ts2:param1>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        if (SOAPHelper.xmlStrToSOAPElement(result) == null) {
            fail("Response can't be null");
        }
    }

    /**
     * Remove response tag and namespace prefix. Response tag has namespace and
     * no prefix, children are under another element that has namespace.
     */
    public void testRemoveResponseTag11() {
        String source = "<response xmlns=\"http://test.com/ns\"><ts2:param1 xmlns:ts2=\"http://test.com/ns2\"><ts2:param2>value2</ts2:param2><param3>value3</param3></ts2:param1></response>";
        String result = "<ts2:param1 xmlns:ts2=\"http://test.com/ns2\"><ts2:param2>value2</ts2:param2><param3>value3</param3></ts2:param1>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        if (SOAPHelper.xmlStrToSOAPElement(result) == null) {
            fail("Response can't be null");
        }
    }

    /**
     * Remove response tag and namespace prefix. No namespace. XML uses
     * ISO-8859-1 character set.
     */
    public void testRemoveResponseTag12() throws UnsupportedEncodingException {
        String source = new String("<response><wrapper><param1>value1</param1><param2>ÄÖÅäöå</param2></wrapper></response>".getBytes(), "ISO-8859-1");
        String result = new String("<wrapper><param1>value1</param1><param2>ÄÖÅäöå</param2></wrapper>".getBytes(), "ISO-8859-1");
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        if (SOAPHelper.xmlStrToSOAPElement(result) == null) {
            fail("Response can't be null");
        }
    }

    /**
     * Remove {serviceName}response tag and namespace prefix. No namespace.
     */
    public void testRemoveResponseTag13() {
        String source = "<testServiceResponse><wrapper><param1>value1</param1><param2>value2</param2></wrapper></testServiceResponse>";
        String result = "<wrapper><param1>value1</param1><param2>value2</param2></wrapper>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        if (SOAPHelper.xmlStrToSOAPElement(result) == null) {
            fail("Response can't be null");
        }
    }

    /**
     * Remove {serviceName}response tag and namespace prefix. Response tag has
     * namespace and no prefix, children are under another element that has
     * namespace and prefix.
     */
    public void testRemoveResponseTag14() {
        String source = "<testServiceResponse xmlns=\"http://test.com/ns\"><ts1:wrapper xmlns:ts1=\"http://test.com/ns2\"><ts1:param1>value1</ts1:param1><ts1:param2>value2</ts1:param2></ts1:wrapper></testServiceResponse>";
        String result = "<ts1:wrapper xmlns:ts1=\"http://test.com/ns2\"><ts1:param1>value1</ts1:param1><ts1:param2>value2</ts1:param2></ts1:wrapper>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        if (SOAPHelper.xmlStrToSOAPElement(result) == null) {
            fail("Response can't be null");
        }
    }

    /**
     * Remove {serviceName}response tag and namespace prefix. Response tag has
     * namespace and no prefix, children are under another element that has
     * namespace and prefix.
     */
    public void testRemoveResponseTag15() {
        String source = "<ts0:testServiceResponse xmlns:ts0=\"http://test.com/ns\"><ts1:wrapper xmlns:ts1=\"http://test.com/ns2\"><ts1:param1>value1</ts1:param1><ts1:param2>value2</ts1:param2></ts1:wrapper></ts0:testServiceResponse>";
        String result = "<ts1:wrapper xmlns:ts1=\"http://test.com/ns2\"><ts1:param1>value1</ts1:param1><ts1:param2>value2</ts1:param2></ts1:wrapper>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        if (SOAPHelper.xmlStrToSOAPElement(result) == null) {
            fail("Response can't be null");
        }
    }

    /**
     * Remove {serviceName}response tag and namespace prefix.
     */
    public void testRemoveResponseTag16() {
        String source = "<ts0:testServiceResponse xmlns:ts0=\"http://test.com/ns\"><wrapper><param1>value1</param1><param2>value2</param2></wrapper></ts0:testServiceResponse>";
        String result = "<wrapper><param1>value1</param1><param2>value2</param2></wrapper>";
        assertEquals(result, ConsumerGatewayUtil.removeResponseTag(source));
        if (SOAPHelper.xmlStrToSOAPElement(result) == null) {
            fail("Response can't be null");
        }
    }
}
