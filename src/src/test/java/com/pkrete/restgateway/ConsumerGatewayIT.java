package com.pkrete.restgateway;

import com.pkrete.restgateway.util.Constants;
import com.pkrete.xrd4j.rest.ClientResponse;
import com.pkrete.xrd4j.rest.client.RESTClient;
import com.pkrete.xrd4j.rest.client.RESTClientFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/**
 * This class contains integrations tests for REST Gateway.
 *
 * @author Petteri Kivimäki
 */
public class ConsumerGatewayIT extends TestCase {

    private static final String CONTENT_TYPE_XML = Constants.TEXT_XML + ";" + Constants.CHARSET_UTF8;
    private static final String CONTENT_TYPE_JSON = Constants.APPLICATION_JSON + ";" + Constants.CHARSET_UTF8;
    private final Map<Integer, String> urls = new HashMap<>();
    private final Map<Integer, Map<String, List<String>>> urlParams = new HashMap<>();

    @Override
    protected void setUp() {
        String buildPath = System.getProperty("consumerPath");
        String baseUrl = "http://localhost:8080/" + buildPath + "/Consumer/";

        // Set up test case 1
        urls.put(1, baseUrl + "www.hel.fi/palvelukarttaws/rest/v2/organization/");
        Map<String, List<String>> caseParams = new HashMap<>();
        List<String> values = new ArrayList<>();
        values.add("1010");
        caseParams.put(Constants.PARAM_RESOURCE_ID, values);
        this.urlParams.put(1, caseParams);

        // Set up test case 2
        urls.put(2, baseUrl + "www.hel.fi/palvelukarttaws/rest/v2/organization/");
        this.urlParams.put(2, new HashMap<String, List<String>>());

        // Set up test case 3
        urls.put(3, baseUrl + "api.finto.fi/rest/v1/search/");
        caseParams = new HashMap<>();
        values = new ArrayList<>();
        values.add("cat");
        caseParams.put("query", values);
        values = new ArrayList<>();
        values.add("en");
        caseParams.put("lang", values);
        values = new ArrayList<>();
        values.add("yso");
        caseParams.put("vocab", values);
        this.urlParams.put(3, caseParams);

        // Set up test case 4
        urls.put(4, baseUrl + "api.kirjastot.fi/v3/organisation/");
        caseParams = new HashMap<>();
        values = new ArrayList<>();
        values.add("kallio");
        caseParams.put("name", values);
        values = new ArrayList<>();
        values.add("helsinki");
        caseParams.put("city.name", values);
        this.urlParams.put(4, caseParams);
    }

    /**
     * REST API for City of Helsinki Service Map - List of Organizations - JSON
     */
    public void testConsumerGateway1Json() {
        String result = "{\"data_source_url\":\"www.liikuntapaikat.fi\",\"name_fi\":\"Jyväskylän yliopisto, LIPAS Liikuntapaikat.fi\",\"name_sv\":\"Jyväskylä universitet, LIPAS Liikuntapaikat.fi\",\"id\":1010,\"name_en\":\"University of Jyväskylä, LIPAS Liikuntapaikat.fi\"}";
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.HTTP_HEADER_ACCEPT, Constants.APPLICATION_JSON);
        sendData(this.urls.get(1), "get", this.urlParams.get(1), headers, result, CONTENT_TYPE_JSON);
    }

    /**
     * REST API for City of Helsinki Service Map - List of Organizations - XML
     */
    public void testConsumerGateway1Xml() {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ts1:response xmlns:ts1=\"http://vrk-test.x-road.fi/producer\"><ts1:data_source_url>www.liikuntapaikat.fi</ts1:data_source_url><ts1:name_fi>Jyväskylän yliopisto, LIPAS Liikuntapaikat.fi</ts1:name_fi><ts1:name_sv>Jyväskylä universitet, LIPAS Liikuntapaikat.fi</ts1:name_sv><ts1:id>1010</ts1:id><ts1:name_en>University of Jyväskylä, LIPAS Liikuntapaikat.fi</ts1:name_en></ts1:response>";
        sendData(this.urls.get(1), "get", this.urlParams.get(1), new HashMap<String, String>(), result, CONTENT_TYPE_XML);
    }

    /**
     * REST API for City of Helsinki Service Map - Single Organization - JSON
     */
    public void testConsumerGateway2Json() {
        String result = "[{\"data_source_url\":\"www.espoo.fi\",\"name_fi\":\"Espoon kaupunki\",\"name_sv\":\"Esbo stad\",\"id\":49,\"name_en\":\"City of Espoo\"},{\"data_source_url\":\"www.hel.fi\",\"name_fi\":\"Helsingin kaupunki\",\"name_sv\":\"Helsingfors stad\",\"id\":91,\"name_en\":\"City of Helsinki\"},{\"data_source_url\":\"www.vantaa.fi\",\"name_fi\":\"Vantaan kaupunki\",\"name_sv\":\"Vanda stad\",\"id\":92,\"name_en\":\"City of Vantaa\"},{\"data_source_url\":\"www.kauniainen.fi\",\"name_fi\":\"Kauniaisten kaupunki\",\"name_sv\":\"Grankulla stad\",\"id\":235,\"name_en\":\"City of Kauniainen\"},{\"data_source_url\":\"www.suomi.fi\",\"name_fi\":\"Valtion IT-palvelukeskus, Suomi.fi-toimitus\",\"name_sv\":\"Statens IT-servicecentral, Suomi.fi-redaktionen\",\"id\":1000,\"name_en\":\"State IT Service Centre, Suomi.fi editorial team\"},{\"data_source_url\":\"www.hus.fi\",\"name_fi\":\"HUS-kuntayhtymä\",\"name_sv\":\"Samkommunen HNS\",\"id\":1001,\"name_en\":\"HUS Hospital District\"},{\"data_source_url\":\"www.visithelsinki.fi\",\"name_fi\":\"Helsingin markkinointi Oy\",\"name_sv\":\"Helsinki Marketing Ltd\",\"id\":1002,\"name_en\":\"Helsinki Marketing Ltd\"},{\"data_source_url\":\"www.hsy.fi\",\"name_fi\":\"Helsingin seudun ympäristöpalvelut HSY\",\"name_sv\":\"Helsingforsregionens miljötjänster HRM\",\"id\":1004,\"name_en\":\"Helsinki Region Environmental Services Authority HSY\"},{\"data_source_url\":\"www.hel.fi/palvelukartta\",\"name_fi\":\"Palvelukartan toimitus\",\"name_sv\":\"Servicekartans redaktion\",\"id\":1005,\"name_en\":\"Service Map editorial team\"},{\"data_source_url\":\"www.jly.fi\",\"name_fi\":\"JLY Jätelaitosyhdistys ry\",\"name_sv\":\"JLY - Avfallsverksföreningen rf\",\"id\":1007,\"name_en\":\"JLY - Finnish Solid Waste Association\"},{\"data_source_url\":\"NOBIL.no\",\"name_fi\":\"Norsk Elbilforening, sähköautojen latauspisteet\",\"name_sv\":\"Norsk Elbilforening\",\"id\":1008,\"name_en\":\"The Norwegian Electric Vehicle Association\"},{\"data_source_url\":\"asiointi.hel.fi/tprulkoinen\",\"name_fi\":\"Ulkoisen toimipisterekisterin käyttäjäyhteisö\",\"name_sv\":\"Gemenskapen bakom externa serviceregister\",\"id\":1009,\"name_en\":\"External service point register user society\"},{\"data_source_url\":\"www.liikuntapaikat.fi\",\"name_fi\":\"Jyväskylän yliopisto, LIPAS Liikuntapaikat.fi\",\"name_sv\":\"Jyväskylä universitet, LIPAS Liikuntapaikat.fi\",\"id\":1010,\"name_en\":\"University of Jyväskylä, LIPAS Liikuntapaikat.fi\"},{\"data_source_url\":\"www.omnia.fi\",\"name_fi\":\"Espoon seudun koulutuskuntayhtymä Omnia\",\"name_sv\":\"Omnia samkommun för utbildning i Esboregionen\",\"id\":1012,\"name_en\":\"Omnia, The Joint Authority in Education in the Espoo Region\"}]";
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.HTTP_HEADER_ACCEPT, Constants.APPLICATION_JSON);
        sendData(this.urls.get(2), "get", this.urlParams.get(2), headers, result, CONTENT_TYPE_JSON);
    }

    /**
     * REST API for City of Helsinki Service Map - Single Organization - XML
     */
    public void testConsumerGateway2Xml() {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ts1:response xmlns:ts1=\"http://vrk-test.x-road.fi/producer\"><ts1:array><ts1:data_source_url>www.espoo.fi</ts1:data_source_url><ts1:name_fi>Espoon kaupunki</ts1:name_fi><ts1:name_sv>Esbo stad</ts1:name_sv><ts1:id>49</ts1:id><ts1:name_en>City of Espoo</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.hel.fi</ts1:data_source_url><ts1:name_fi>Helsingin kaupunki</ts1:name_fi><ts1:name_sv>Helsingfors stad</ts1:name_sv><ts1:id>91</ts1:id><ts1:name_en>City of Helsinki</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.vantaa.fi</ts1:data_source_url><ts1:name_fi>Vantaan kaupunki</ts1:name_fi><ts1:name_sv>Vanda stad</ts1:name_sv><ts1:id>92</ts1:id><ts1:name_en>City of Vantaa</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.kauniainen.fi</ts1:data_source_url><ts1:name_fi>Kauniaisten kaupunki</ts1:name_fi><ts1:name_sv>Grankulla stad</ts1:name_sv><ts1:id>235</ts1:id><ts1:name_en>City of Kauniainen</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.suomi.fi</ts1:data_source_url><ts1:name_fi>Valtion IT-palvelukeskus, Suomi.fi-toimitus</ts1:name_fi><ts1:name_sv>Statens IT-servicecentral, Suomi.fi-redaktionen</ts1:name_sv><ts1:id>1000</ts1:id><ts1:name_en>State IT Service Centre, Suomi.fi editorial team</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.hus.fi</ts1:data_source_url><ts1:name_fi>HUS-kuntayhtymä</ts1:name_fi><ts1:name_sv>Samkommunen HNS</ts1:name_sv><ts1:id>1001</ts1:id><ts1:name_en>HUS Hospital District</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.visithelsinki.fi</ts1:data_source_url><ts1:name_fi>Helsingin markkinointi Oy</ts1:name_fi><ts1:name_sv>Helsinki Marketing Ltd</ts1:name_sv><ts1:id>1002</ts1:id><ts1:name_en>Helsinki Marketing Ltd</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.hsy.fi</ts1:data_source_url><ts1:name_fi>Helsingin seudun ympäristöpalvelut HSY</ts1:name_fi><ts1:name_sv>Helsingforsregionens miljötjänster HRM</ts1:name_sv><ts1:id>1004</ts1:id><ts1:name_en>Helsinki Region Environmental Services Authority HSY</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.hel.fi/palvelukartta</ts1:data_source_url><ts1:name_fi>Palvelukartan toimitus</ts1:name_fi><ts1:name_sv>Servicekartans redaktion</ts1:name_sv><ts1:id>1005</ts1:id><ts1:name_en>Service Map editorial team</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.jly.fi</ts1:data_source_url><ts1:name_fi>JLY Jätelaitosyhdistys ry</ts1:name_fi><ts1:name_sv>JLY - Avfallsverksföreningen rf</ts1:name_sv><ts1:id>1007</ts1:id><ts1:name_en>JLY - Finnish Solid Waste Association</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>NOBIL.no</ts1:data_source_url><ts1:name_fi>Norsk Elbilforening, sähköautojen latauspisteet</ts1:name_fi><ts1:name_sv>Norsk Elbilforening</ts1:name_sv><ts1:id>1008</ts1:id><ts1:name_en>The Norwegian Electric Vehicle Association</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>asiointi.hel.fi/tprulkoinen</ts1:data_source_url><ts1:name_fi>Ulkoisen toimipisterekisterin käyttäjäyhteisö</ts1:name_fi><ts1:name_sv>Gemenskapen bakom externa serviceregister</ts1:name_sv><ts1:id>1009</ts1:id><ts1:name_en>External service point register user society</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.liikuntapaikat.fi</ts1:data_source_url><ts1:name_fi>Jyväskylän yliopisto, LIPAS Liikuntapaikat.fi</ts1:name_fi><ts1:name_sv>Jyväskylä universitet, LIPAS Liikuntapaikat.fi</ts1:name_sv><ts1:id>1010</ts1:id><ts1:name_en>University of Jyväskylä, LIPAS Liikuntapaikat.fi</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.omnia.fi</ts1:data_source_url><ts1:name_fi>Espoon seudun koulutuskuntayhtymä Omnia</ts1:name_fi><ts1:name_sv>Omnia samkommun för utbildning i Esboregionen</ts1:name_sv><ts1:id>1012</ts1:id><ts1:name_en>Omnia, The Joint Authority in Education in the Espoo Region</ts1:name_en></ts1:array></ts1:response>";
        sendData(this.urls.get(2), "get", this.urlParams.get(2), new HashMap<String, String>(), result, CONTENT_TYPE_XML);
    }

    /**
     * Finto : Finnish Thesaurus and Ontology Service - Search - JSON
     */
    public void testConsumerGateway3Json() {
        String result = "{\"@context\":{\"hiddenLabel\":\"skos:hiddenLabel\",\"prefLabel\":\"skos:prefLabel\",\"skos\":\"http://www.w3.org/2004/02/skos/core#\",\"isothes\":\"http://purl.org/iso25964/skos-thes#\",\"onki\":\"http://schema.onki.fi/onki#\",\"altLabel\":\"skos:altLabel\",\"type\":\"@type\",\"@language\":\"en\",\"uri\":\"@id\",\"results\":{\"@container\":\"@list\",\"@id\":\"onki:results\"}},\"uri\":\"\",\"results\":{\"localname\":\"p19378\",\"prefLabel\":\"cat\",\"vocab\":\"yso\",\"type\":[\"skos:Concept\",\"http://www.yso.fi/onto/yso-meta/Concept\"],\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"}}";
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.HTTP_HEADER_ACCEPT, Constants.APPLICATION_JSON);

        sendData(this.urls.get(3), "get", this.urlParams.get(3), headers, result, CONTENT_TYPE_JSON);
    }

    /**
     * Finto : Finnish Thesaurus and Ontology Service - Search - XML
     */
    public void testConsumerGateway3Xml() {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ts1:response xmlns:ts1=\"http://vrk-test.x-road.fi/producer\"><ts1:__at__context><ts1:hiddenLabel>skos:hiddenLabel</ts1:hiddenLabel><ts1:__at__language>en</ts1:__at__language><ts1:prefLabel>skos:prefLabel</ts1:prefLabel><ts1:skos>http://www.w3.org/2004/02/skos/core#</ts1:skos><ts1:isothes>http://purl.org/iso25964/skos-thes#</ts1:isothes><ts1:onki>http://schema.onki.fi/onki#</ts1:onki><ts1:altLabel>skos:altLabel</ts1:altLabel><ts1:type>@type</ts1:type><ts1:uri>@id</ts1:uri><ts1:results><ts1:__at__id>onki:results</ts1:__at__id><ts1:__at__container>@list</ts1:__at__container></ts1:results></ts1:__at__context><ts1:uri/><ts1:results><ts1:localname>p19378</ts1:localname><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>yso</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results></ts1:response>";
        sendData(this.urls.get(3), "get", this.urlParams.get(3), new HashMap<String, String>(), result, CONTENT_TYPE_XML);
    }

    /**
     * Finnish Library Directory - JSON - N.B.! Response contains cyrillic
     * characters.
     */
    public void testConsumerGateway4Json() {
        String result = "{\"result\":{\"total\":1,\"count\":1,\"items\":{\"organisation\":{\"parent\":84846,\"web_library\":\"\",\"address\":{\"area\":{\"multilang\":true,\"value\":[{\"lang\":\"fi\",\"content\":\"Kallio\"},{\"lang\":\"sv\",\"content\":\"Kallio\"},{\"lang\":\"en\",\"content\":\"Kallio\"},{\"lang\":\"se\",\"content\":\"Kallio\"},{\"lang\":\"ru\",\"content\":\"Kallio\"}]},\"zipcode\":\"00530\",\"box_number\":\"\",\"city\":{\"multilang\":true,\"value\":[{\"lang\":\"fi\",\"content\":\"Helsinki\"},{\"lang\":\"sv\",\"content\":\"Helsingfors\"},{\"lang\":\"en\",\"content\":\"Helsinki\"},{\"lang\":\"se\",\"content\":\"Helsinki\"},{\"lang\":\"ru\",\"content\":\"Helsinki\"}]},\"street\":{\"multilang\":true,\"value\":[{\"lang\":\"fi\",\"content\":\"Viides linja 11\"},{\"lang\":\"sv\",\"content\":\"Femte linjen 11\"},{\"lang\":\"en\",\"content\":\"Viides linja 11\"},{\"lang\":\"se\",\"content\":\"Viides linja 11\"},{\"lang\":\"ru\",\"content\":\"Viides linja 11\"}]},\"coordinates\":{\"lon\":24.95355311,\"lat\":60.18372258}},\"city\":15885,\"consortium\":2093,\"type\":\"library\",\"provincial_library\":396,\"branch_type\":\"library\",\"name\":{\"multilang\":true,\"value\":[{\"lang\":\"fi\",\"content\":\"Kallion kirjasto\"},{\"lang\":\"sv\",\"content\":\"Berghälls bibliotek\"},{\"lang\":\"en\",\"content\":\"Kallio Library\"},{\"lang\":\"se\",\"content\":\"Kallion kirjasto\"},{\"lang\":\"ru\",\"content\":\"Библиотека Каллио\"}]},\"short_name\":{\"multilang\":true,\"value\":[{\"lang\":\"fi\",\"content\":\"Kallio\"},{\"lang\":\"sv\",\"content\":\"Berghäll\"},{\"lang\":\"en\",\"content\":\"Kallio\"},{\"lang\":\"se\",\"content\":\"Kallio\"},{\"lang\":\"ru\",\"content\":\"Каллио\"}]},\"id\":84860,\"region\":1001,\"email\":{\"multilang\":true,\"value\":[{\"lang\":\"fi\",\"content\":\"kallion_kirjasto@hel.fi\"},{\"lang\":\"sv\",\"content\":\"kallion_kirjasto@hel.fi\"},{\"lang\":\"en\",\"content\":\"kallion_kirjasto@hel.fi\"},{\"lang\":\"se\",\"content\":\"kallion_kirjasto@hel.fi\"},{\"lang\":\"ru\",\"content\":\"kallion_kirjasto@hel.fi\"}]},\"slug\":{\"multilang\":true,\"value\":[{\"lang\":\"fi\",\"content\":\"kallio\"},{\"lang\":\"sv\",\"content\":\"kallio\"},{\"lang\":\"en\",\"content\":\"kallio\"},{\"lang\":\"se\",\"content\":\"kallio\"},{\"lang\":\"ru\",\"content\":\"kallio\"}]},\"homepage\":{\"multilang\":true,\"value\":[{\"lang\":\"fi\",\"content\":\"http://www.helmet.fi/kallionkirjasto\"},{\"lang\":\"sv\",\"content\":\"http://www.helmet.fi/berghallsbibliotek\"},{\"lang\":\"en\",\"content\":\"http://www.helmet.fi/kalliolibrary\"},{\"lang\":\"se\",\"content\":\"http://www.helmet.fi/kallionkirjasto\"},{\"lang\":\"ru\",\"content\":\"http://www.helmet.fi/kallionkirjasto\"}]}}}}}";
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.HTTP_HEADER_ACCEPT, Constants.APPLICATION_JSON);
        sendData(this.urls.get(4), "get", this.urlParams.get(4), headers, result, CONTENT_TYPE_JSON);
    }

    /**
     * Finnish Library Directory - XML - N.B.! Response contains cyrillic
     * characters.
     */
    public void testConsumerGateway4Xml() {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><result count=\"1\" total=\"1\"><items><organisation><web_library/><parent>84846</parent><branch_type>library</branch_type><short_name multilang=\"true\"><value lang=\"fi\">Kallio</value><value lang=\"sv\">Berghäll</value><value lang=\"en\">Kallio</value><value lang=\"se\">Kallio</value><value lang=\"ru\">Каллио</value></short_name><type>library</type><homepage multilang=\"true\"><value lang=\"fi\">http://www.helmet.fi/kallionkirjasto</value><value lang=\"sv\">http://www.helmet.fi/berghallsbibliotek</value><value lang=\"en\">http://www.helmet.fi/kalliolibrary</value><value lang=\"se\">http://www.helmet.fi/kallionkirjasto</value><value lang=\"ru\">http://www.helmet.fi/kallionkirjasto</value></homepage><city>15885</city><id>84860</id><consortium>2093</consortium><address><area multilang=\"true\"><value lang=\"fi\">Kallio</value><value lang=\"sv\">Kallio</value><value lang=\"en\">Kallio</value><value lang=\"se\">Kallio</value><value lang=\"ru\">Kallio</value></area><street multilang=\"true\"><value lang=\"fi\">Viides linja 11</value><value lang=\"sv\">Femte linjen 11</value><value lang=\"en\">Viides linja 11</value><value lang=\"se\">Viides linja 11</value><value lang=\"ru\">Viides linja 11</value></street><zipcode>00530</zipcode><box_number/><coordinates><lon>24.95355311</lon><lat>60.18372258</lat></coordinates><city multilang=\"true\"><value lang=\"fi\">Helsinki</value><value lang=\"sv\">Helsingfors</value><value lang=\"en\">Helsinki</value><value lang=\"se\">Helsinki</value><value lang=\"ru\">Helsinki</value></city></address><email multilang=\"true\"><value lang=\"fi\">kallion_kirjasto@hel.fi</value><value lang=\"sv\">kallion_kirjasto@hel.fi</value><value lang=\"en\">kallion_kirjasto@hel.fi</value><value lang=\"se\">kallion_kirjasto@hel.fi</value><value lang=\"ru\">kallion_kirjasto@hel.fi</value></email><name multilang=\"true\"><value lang=\"fi\">Kallion kirjasto</value><value lang=\"sv\">Berghälls bibliotek</value><value lang=\"en\">Kallio Library</value><value lang=\"se\">Kallion kirjasto</value><value lang=\"ru\">Библиотека Каллио</value></name><slug multilang=\"true\"><value lang=\"fi\">kallio</value><value lang=\"sv\">kallio</value><value lang=\"en\">kallio</value><value lang=\"se\">kallio</value><value lang=\"ru\">kallio</value></slug><region>1001</region><provincial_library>396</provincial_library></organisation></items></result>";
        sendData(this.urls.get(4), "get", this.urlParams.get(4), new HashMap<String, String>(), result, CONTENT_TYPE_XML);
    }

    private void sendData(String url, String verb, Map<String, List<String>> urlParams, Map<String, String> headers, String expectedResponse, String expectedContentType) {
        RESTClient restClient = RESTClientFactory.createRESTClient(verb);
        // Send request to the service endpoint
        ClientResponse restResponse = restClient.send(url, "", urlParams, headers);

        String data = restResponse.getData();

        assertEquals(expectedResponse, new String(data.getBytes(), StandardCharsets.UTF_8));
        assertEquals(expectedContentType, restResponse.getContentType());
    }
}
