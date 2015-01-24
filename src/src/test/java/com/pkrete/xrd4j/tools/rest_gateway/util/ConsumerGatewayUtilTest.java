package com.pkrete.xrd4j.tools.rest_gateway.util;

import com.pkrete.xrd4j.common.exception.XRd4JException;
import com.pkrete.xrd4j.common.member.ConsumerMember;
import com.pkrete.xrd4j.common.member.MemberClass;
import com.pkrete.xrd4j.common.member.ProducerMember;
import com.pkrete.xrd4j.common.member.SDSBInstance;
import com.pkrete.xrd4j.tools.rest_gateway.endpoint.ConsumerEndpoint;
import java.util.Map;
import java.util.Properties;
import junit.framework.TestCase;

/**
 * Test cases for ConsumerGatewayUtil class.
 *
 * @author Petteri Kivimäki
 */
public class ConsumerGatewayUtilTest extends TestCase {

    private Properties props;
    private Properties endpoints;
    private Map<String, ConsumerEndpoint> map;

    /**
     * Initializes instance variables for test cases.
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.props = new Properties();
        this.endpoints = new Properties();
        // Set up default properties
        this.props.put(Constants.CONSUMER_PROPS_ID_CLIENT, "FI_PILOT.GOV.0245437-2.ConsumerService");
        this.props.put(Constants.CONSUMER_PROPS_SECURITY_SERVER_URL, "http://localhost:8080/RESTGateway/Provider");
        this.props.put(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE, "http://serialize.com");
        this.props.put(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE, "ts1");
        this.props.put(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE, "http://deserialize.com");
        // Set up endpoints
        this.endpoints.put("0." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1");
        this.endpoints.put("0." + Constants.ENDPOINT_PROPS_VERB, "get");
        this.endpoints.put("0." + Constants.CONSUMER_PROPS_PATH, "/www.hel.fi/palvelukarttaws/rest/v2/organization/");

        this.endpoints.put("1." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.Demo2Service.getOrganization.v1");
        this.endpoints.put("1." + Constants.ENDPOINT_PROPS_VERB, "get");
        this.endpoints.put("1." + Constants.CONSUMER_PROPS_PATH, "/www.hel.fi/palvelukarttaws/rest/v2/organization/{resourceId}");
        this.endpoints.put("1." + Constants.CONSUMER_PROPS_ID_CLIENT, "FI_PILOT.GOV.0245437-2.TestService");
        this.endpoints.put("1." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE, "http://serialize.com/custom");
        this.endpoints.put("1." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE, "test");
        this.endpoints.put("1." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE, "http://deserialize.com/custom");

        this.endpoints.put("2." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.Demo2Service.getCompany.v1");
        this.endpoints.put("2." + Constants.ENDPOINT_PROPS_VERB, "get");
        this.endpoints.put("2." + Constants.CONSUMER_PROPS_PATH, "/avoindata.prh.fi/opendata/bis/v1/{resourceId}");
        this.endpoints.put("2." + Constants.CONSUMER_PROPS_MOD_URL, "true");

        this.endpoints.put("3." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.testApi.v1");
        this.endpoints.put("3." + Constants.ENDPOINT_PROPS_VERB, "get");
        this.endpoints.put("3." + Constants.CONSUMER_PROPS_PATH, "/test.com/api");

        this.endpoints.put("4." + Constants.ENDPOINT_PROPS_ID, "FI_PILOT.GOV.1019125-0.Demo2Service.exampleApi.v1");
        this.endpoints.put("4." + Constants.ENDPOINT_PROPS_VERB, "get");
        this.endpoints.put("4." + Constants.CONSUMER_PROPS_PATH, "/example.com/api");
        this.endpoints.put("4." + Constants.CONSUMER_PROPS_ID_CLIENT, "FI_PILOT.GOV0245437-2");
        // Extract endpoints
        this.map = ConsumerGatewayUtil.extractConsumers(this.endpoints, this.props);
    }

    /**
     * The first endpoint on the list. No overridden properties.
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
        assertEquals(new ConsumerMember(SDSBInstance.FI_PILOT, MemberClass.GOV, "0245437-2", "ConsumerService").toString(), temp.getConsumer().toString());
        assertEquals(new ProducerMember(SDSBInstance.FI_PILOT, MemberClass.GOV, "1019125-0", "Demo2Service", "getOrganizationList", "v1").toString(), temp.getProducer().toString());
    }

    /**
     * The second endpoint on the list. Overridden properties.
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
        assertEquals(new ConsumerMember(SDSBInstance.FI_PILOT, MemberClass.GOV, "0245437-2", "TestService").toString(), temp.getConsumer().toString());
        assertEquals(new ProducerMember(SDSBInstance.FI_PILOT, MemberClass.GOV, "1019125-0", "Demo2Service", "getOrganization", "v1").toString(), temp.getProducer().toString());
    }

    /**
     * The third endpoint on the list. No overridden properties.
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
        assertEquals(new ConsumerMember(SDSBInstance.FI_PILOT, MemberClass.GOV, "0245437-2", "ConsumerService").toString(), temp.getConsumer().toString());
        assertEquals(new ProducerMember(SDSBInstance.FI_PILOT, MemberClass.GOV, "1019125-0", "Demo2Service", "getCompany", "v1").toString(), temp.getProducer().toString());
    }

    /**
     * The fourth endpoint on the list. Invalid configuration - service id
     * lacks subsystem.
     * @throws XRd4JException if there's a XRd4J error
     */
    public void testExtractConsumer4() throws XRd4JException {
        ConsumerEndpoint temp = ConsumerGatewayUtil.findMatch("GET /test.com/api/", map);
        assertEquals(true, temp == null);
    }

    /**
     * The fifth endpoint on the list. Invalid configuration - client id
     * lacks subsystem.
     * @throws XRd4JException if there's a XRd4J error
     */
    public void testExtractConsumer5() throws XRd4JException {
        ConsumerEndpoint temp = ConsumerGatewayUtil.findMatch("GET /example.com/api/", map);
        assertEquals(true, temp == null);
    }
}
