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

    private String baseUrl;

    @Override
    protected void setUp() {
        String buildPath = System.getProperty("consumerPath");
        this.baseUrl = "http://localhost:8080/" + buildPath + "/Consumer/";
    }

    public void testConsumerGateway1Json() {
        String result = "{\"data_source_url\":\"www.liikuntapaikat.fi\",\"name_fi\":\"Jyväskylän yliopisto, LIPAS Liikuntapaikat.fi\",\"name_sv\":\"Jyväskylä universitet, LIPAS Liikuntapaikat.fi\",\"id\":1010,\"name_en\":\"University of Jyväskylä, LIPAS Liikuntapaikat.fi\"}";
        String url = "www.hel.fi/palvelukarttaws/rest/v2/organization/";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Map<String, List<String>> urlParams = new HashMap<>();
        List<String> values = new ArrayList<>();
        values.add("1010");
        urlParams.put(Constants.PARAM_RESOURCE_ID, values);
        RESTClient restClient = RESTClientFactory.createRESTClient("get");
        // Send request to the service endpoint
        ClientResponse restResponse = restClient.send(this.baseUrl + url, "", urlParams, headers);

        String data = restResponse.getData();

        assertEquals(result, new String(data.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
        assertEquals("application/json;charset=utf-8", restResponse.getContentType());
    }

    public void testConsumerGateway1Xml() {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ts1:response xmlns:ts1=\"http://vrk-test.x-road.fi/producer\"><ts1:data_source_url>www.liikuntapaikat.fi</ts1:data_source_url><ts1:name_fi>Jyväskylän yliopisto, LIPAS Liikuntapaikat.fi</ts1:name_fi><ts1:name_sv>Jyväskylä universitet, LIPAS Liikuntapaikat.fi</ts1:name_sv><ts1:id>1010</ts1:id><ts1:name_en>University of Jyväskylä, LIPAS Liikuntapaikat.fi</ts1:name_en></ts1:response>";
        String url = "www.hel.fi/palvelukarttaws/rest/v2/organization/";
        Map<String, String> headers = new HashMap<>();
        Map<String, List<String>> urlParams = new HashMap<>();
        List<String> values = new ArrayList<>();
        values.add("1010");
        urlParams.put(Constants.PARAM_RESOURCE_ID, values);
        RESTClient restClient = RESTClientFactory.createRESTClient("get");
        // Send request to the service endpoint
        ClientResponse restResponse = restClient.send(this.baseUrl + url, "", urlParams, headers);

        String data = restResponse.getData();

        assertEquals(result, new String(data.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
        assertEquals("text/xml;charset=utf-8", restResponse.getContentType());
    }

    public void testConsumerGateway2Json() {
        String result = "[{\"data_source_url\":\"www.espoo.fi\",\"name_fi\":\"Espoon kaupunki\",\"name_sv\":\"Esbo stad\",\"id\":49,\"name_en\":\"City of Espoo\"},{\"data_source_url\":\"www.hel.fi\",\"name_fi\":\"Helsingin kaupunki\",\"name_sv\":\"Helsingfors stad\",\"id\":91,\"name_en\":\"City of Helsinki\"},{\"data_source_url\":\"www.vantaa.fi\",\"name_fi\":\"Vantaan kaupunki\",\"name_sv\":\"Vanda stad\",\"id\":92,\"name_en\":\"City of Vantaa\"},{\"data_source_url\":\"www.kauniainen.fi\",\"name_fi\":\"Kauniaisten kaupunki\",\"name_sv\":\"Grankulla stad\",\"id\":235,\"name_en\":\"City of Kauniainen\"},{\"data_source_url\":\"www.suomi.fi\",\"name_fi\":\"Valtion IT-palvelukeskus, Suomi.fi-toimitus\",\"name_sv\":\"Statens IT-servicecentral, Suomi.fi-redaktionen\",\"id\":1000,\"name_en\":\"State IT Service Centre, Suomi.fi editorial team\"},{\"data_source_url\":\"www.hus.fi\",\"name_fi\":\"HUS-kuntayhtymä\",\"name_sv\":\"Samkommunen HNS\",\"id\":1001,\"name_en\":\"HUS Hospital District\"},{\"data_source_url\":\"www.visithelsinki.fi\",\"name_fi\":\"Helsingin markkinointi Oy\",\"name_sv\":\"Helsinki Marketing Ltd\",\"id\":1002,\"name_en\":\"Helsinki Marketing Ltd\"},{\"data_source_url\":\"www.hsy.fi\",\"name_fi\":\"Helsingin seudun ympäristöpalvelut HSY\",\"name_sv\":\"Helsingforsregionens miljötjänster HRM\",\"id\":1004,\"name_en\":\"Helsinki Region Environmental Services Authority HSY\"},{\"data_source_url\":\"www.hel.fi/palvelukartta\",\"name_fi\":\"Palvelukartan toimitus\",\"name_sv\":\"Servicekartans redaktion\",\"id\":1005,\"name_en\":\"Service Map editorial team\"},{\"data_source_url\":\"www.jly.fi\",\"name_fi\":\"JLY Jätelaitosyhdistys ry\",\"name_sv\":\"JLY - Avfallsverksföreningen rf\",\"id\":1007,\"name_en\":\"JLY - Finnish Solid Waste Association\"},{\"data_source_url\":\"NOBIL.no\",\"name_fi\":\"Norsk Elbilforening, sähköautojen latauspisteet\",\"name_sv\":\"Norsk Elbilforening\",\"id\":1008,\"name_en\":\"The Norwegian Electric Vehicle Association\"},{\"data_source_url\":\"asiointi.hel.fi/tprulkoinen\",\"name_fi\":\"Ulkoisen toimipisterekisterin käyttäjäyhteisö\",\"name_sv\":\"Gemenskapen bakom externa serviceregister\",\"id\":1009,\"name_en\":\"External service point register user society\"},{\"data_source_url\":\"www.liikuntapaikat.fi\",\"name_fi\":\"Jyväskylän yliopisto, LIPAS Liikuntapaikat.fi\",\"name_sv\":\"Jyväskylä universitet, LIPAS Liikuntapaikat.fi\",\"id\":1010,\"name_en\":\"University of Jyväskylä, LIPAS Liikuntapaikat.fi\"},{\"data_source_url\":\"www.omnia.fi\",\"name_fi\":\"Espoon seudun koulutuskuntayhtymä Omnia\",\"name_sv\":\"Omnia samkommun för utbildning i Esboregionen\",\"id\":1012,\"name_en\":\"Omnia, The Joint Authority in Education in the Espoo Region\"}]";
        String url = "www.hel.fi/palvelukarttaws/rest/v2/organization/";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Map<String, List<String>> urlParams = new HashMap<>();
        RESTClient restClient = RESTClientFactory.createRESTClient("get");
        // Send request to the service endpoint
        ClientResponse restResponse = restClient.send(this.baseUrl + url, "", urlParams, headers);

        String data = restResponse.getData();

        assertEquals(result, new String(data.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
        assertEquals("application/json;charset=utf-8", restResponse.getContentType());
    }

    public void testConsumerGateway2Xml() {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ts1:response xmlns:ts1=\"http://vrk-test.x-road.fi/producer\"><ts1:array><ts1:data_source_url>www.espoo.fi</ts1:data_source_url><ts1:name_fi>Espoon kaupunki</ts1:name_fi><ts1:name_sv>Esbo stad</ts1:name_sv><ts1:id>49</ts1:id><ts1:name_en>City of Espoo</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.hel.fi</ts1:data_source_url><ts1:name_fi>Helsingin kaupunki</ts1:name_fi><ts1:name_sv>Helsingfors stad</ts1:name_sv><ts1:id>91</ts1:id><ts1:name_en>City of Helsinki</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.vantaa.fi</ts1:data_source_url><ts1:name_fi>Vantaan kaupunki</ts1:name_fi><ts1:name_sv>Vanda stad</ts1:name_sv><ts1:id>92</ts1:id><ts1:name_en>City of Vantaa</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.kauniainen.fi</ts1:data_source_url><ts1:name_fi>Kauniaisten kaupunki</ts1:name_fi><ts1:name_sv>Grankulla stad</ts1:name_sv><ts1:id>235</ts1:id><ts1:name_en>City of Kauniainen</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.suomi.fi</ts1:data_source_url><ts1:name_fi>Valtion IT-palvelukeskus, Suomi.fi-toimitus</ts1:name_fi><ts1:name_sv>Statens IT-servicecentral, Suomi.fi-redaktionen</ts1:name_sv><ts1:id>1000</ts1:id><ts1:name_en>State IT Service Centre, Suomi.fi editorial team</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.hus.fi</ts1:data_source_url><ts1:name_fi>HUS-kuntayhtymä</ts1:name_fi><ts1:name_sv>Samkommunen HNS</ts1:name_sv><ts1:id>1001</ts1:id><ts1:name_en>HUS Hospital District</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.visithelsinki.fi</ts1:data_source_url><ts1:name_fi>Helsingin markkinointi Oy</ts1:name_fi><ts1:name_sv>Helsinki Marketing Ltd</ts1:name_sv><ts1:id>1002</ts1:id><ts1:name_en>Helsinki Marketing Ltd</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.hsy.fi</ts1:data_source_url><ts1:name_fi>Helsingin seudun ympäristöpalvelut HSY</ts1:name_fi><ts1:name_sv>Helsingforsregionens miljötjänster HRM</ts1:name_sv><ts1:id>1004</ts1:id><ts1:name_en>Helsinki Region Environmental Services Authority HSY</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.hel.fi/palvelukartta</ts1:data_source_url><ts1:name_fi>Palvelukartan toimitus</ts1:name_fi><ts1:name_sv>Servicekartans redaktion</ts1:name_sv><ts1:id>1005</ts1:id><ts1:name_en>Service Map editorial team</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.jly.fi</ts1:data_source_url><ts1:name_fi>JLY Jätelaitosyhdistys ry</ts1:name_fi><ts1:name_sv>JLY - Avfallsverksföreningen rf</ts1:name_sv><ts1:id>1007</ts1:id><ts1:name_en>JLY - Finnish Solid Waste Association</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>NOBIL.no</ts1:data_source_url><ts1:name_fi>Norsk Elbilforening, sähköautojen latauspisteet</ts1:name_fi><ts1:name_sv>Norsk Elbilforening</ts1:name_sv><ts1:id>1008</ts1:id><ts1:name_en>The Norwegian Electric Vehicle Association</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>asiointi.hel.fi/tprulkoinen</ts1:data_source_url><ts1:name_fi>Ulkoisen toimipisterekisterin käyttäjäyhteisö</ts1:name_fi><ts1:name_sv>Gemenskapen bakom externa serviceregister</ts1:name_sv><ts1:id>1009</ts1:id><ts1:name_en>External service point register user society</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.liikuntapaikat.fi</ts1:data_source_url><ts1:name_fi>Jyväskylän yliopisto, LIPAS Liikuntapaikat.fi</ts1:name_fi><ts1:name_sv>Jyväskylä universitet, LIPAS Liikuntapaikat.fi</ts1:name_sv><ts1:id>1010</ts1:id><ts1:name_en>University of Jyväskylä, LIPAS Liikuntapaikat.fi</ts1:name_en></ts1:array><ts1:array><ts1:data_source_url>www.omnia.fi</ts1:data_source_url><ts1:name_fi>Espoon seudun koulutuskuntayhtymä Omnia</ts1:name_fi><ts1:name_sv>Omnia samkommun för utbildning i Esboregionen</ts1:name_sv><ts1:id>1012</ts1:id><ts1:name_en>Omnia, The Joint Authority in Education in the Espoo Region</ts1:name_en></ts1:array></ts1:response>";
        String url = "www.hel.fi/palvelukarttaws/rest/v2/organization/";
        Map<String, String> headers = new HashMap<>();
        Map<String, List<String>> urlParams = new HashMap<>();
        RESTClient restClient = RESTClientFactory.createRESTClient("get");
        // Send request to the service endpoint
        ClientResponse restResponse = restClient.send(this.baseUrl + url, "", urlParams, headers);

        String data = restResponse.getData();

        assertEquals(result, new String(data.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
        assertEquals("text/xml;charset=utf-8", restResponse.getContentType());
    }

    public void testConsumerGateway3Json() {
        String result = "{\"@context\":{\"hiddenLabel\":\"skos:hiddenLabel\",\"prefLabel\":\"skos:prefLabel\",\"skos\":\"http://www.w3.org/2004/02/skos/core#\",\"isothes\":\"http://purl.org/iso25964/skos-thes#\",\"onki\":\"http://schema.onki.fi/onki#\",\"altLabel\":\"skos:altLabel\",\"type\":\"@type\",\"@language\":\"en\",\"uri\":\"@id\",\"results\":{\"@container\":\"@list\",\"@id\":\"onki:results\"}},\"uri\":\"\",\"results\":[{\"notation\":\"cat\",\"prefLabel\":\"Catalan language\",\"vocab\":\"lexvo\",\"type\":[\"skos:Concept\",\"http://lexvo.org/ontology#Language\"],\"lang\":\"en\",\"uri\":\"http://lexvo.org/id/iso639-3/cat\"},{\"notation\":\"cat\",\"prefLabel\":\"???????\",\"vocab\":\"lexvo\",\"type\":[\"skos:Concept\",\"http://lexvo.org/ontology#Language\"],\"lang\":\"en-Dsrt\",\"uri\":\"http://lexvo.org/id/iso639-3/cat\"},{\"notation\":\"34B12\",\"prefLabel\":\"cat\",\"vocab\":\"ic\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://iconclass.org/34B12\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"afo\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"prefLabel\":\"cat\",\"vocab\":\"afo\",\"type\":[\"skos:Concept\",\"http://www.yso.fi/onto/afo-meta/Concept\"],\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/afo/p1287\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"juho\",\"type\":[\"skos:Concept\",\"http://www.yso.fi/onto/yso-meta/Concept\"],\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"jupo\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"kauno\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"keko\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"kito\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"prefLabel\":\"cat\",\"vocab\":\"koko\",\"type\":[\"skos:Concept\",\"http://www.yso.fi/onto/afo-meta/Concept\",\"http://www.yso.fi/onto/yso-meta/Concept\",\"http://www.yso.fi/onto/kauno-meta/Concept\"],\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/koko/p37252\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"kto\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"kulo\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"liito\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"mero\",\"type\":[\"skos:Concept\",\"http://www.yso.fi/onto/yso-meta/Concept\"],\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"hiddenLabel\":\"Cat\",\"prefLabel\":\"Cats\",\"vocab\":\"mesh\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/mesh/D002415\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"muso\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"pto\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"puho\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"maotao\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"prefLabel\":\"cat\",\"vocab\":\"tero\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/tero/p19378\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"tsr\",\"type\":[\"skos:Concept\",\"http://www.yso.fi/onto/yso-meta/Concept\"],\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"exvocab\":\"yso\",\"prefLabel\":\"cat\",\"vocab\":\"valo\",\"type\":\"skos:Concept\",\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"},{\"prefLabel\":\"cat\",\"vocab\":\"yso\",\"type\":[\"skos:Concept\",\"http://www.yso.fi/onto/yso-meta/Concept\"],\"lang\":\"en\",\"uri\":\"http://www.yso.fi/onto/yso/p19378\"}]}";
        String url = "api.finto.fi/rest/v1/search/";
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        Map<String, List<String>> urlParams = new HashMap<>();

        List<String> values = new ArrayList<>();
        values.add("cat");
        urlParams.put("query", values);

        values = new ArrayList<>();
        values.add("en");
        urlParams.put("lang", values);

        RESTClient restClient = RESTClientFactory.createRESTClient("get");
        // Send request to the service endpoint
        ClientResponse restResponse = restClient.send(this.baseUrl + url, "", urlParams, headers);

        String data = restResponse.getData();

        assertEquals(result, new String(data.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
        assertEquals("application/json;charset=utf-8", restResponse.getContentType());
    }

    public void testConsumerGateway3Xml() {
        String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ts1:response xmlns:ts1=\"http://vrk-test.x-road.fi/producer\"><ts1:__at__context><ts1:hiddenLabel>skos:hiddenLabel</ts1:hiddenLabel><ts1:__at__language>en</ts1:__at__language><ts1:prefLabel>skos:prefLabel</ts1:prefLabel><ts1:skos>http://www.w3.org/2004/02/skos/core#</ts1:skos><ts1:isothes>http://purl.org/iso25964/skos-thes#</ts1:isothes><ts1:onki>http://schema.onki.fi/onki#</ts1:onki><ts1:altLabel>skos:altLabel</ts1:altLabel><ts1:type>@type</ts1:type><ts1:uri>@id</ts1:uri><ts1:results><ts1:__at__id>onki:results</ts1:__at__id><ts1:__at__container>@list</ts1:__at__container></ts1:results></ts1:__at__context><ts1:uri/><ts1:results><ts1:notation>cat</ts1:notation><ts1:prefLabel>Catalan language</ts1:prefLabel><ts1:vocab>lexvo</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:type>http://lexvo.org/ontology#Language</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://lexvo.org/id/iso639-3/cat</ts1:uri></ts1:results><ts1:results><ts1:notation>cat</ts1:notation><ts1:prefLabel>???????</ts1:prefLabel><ts1:vocab>lexvo</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:type>http://lexvo.org/ontology#Language</ts1:type><ts1:lang>en-Dsrt</ts1:lang><ts1:uri>http://lexvo.org/id/iso639-3/cat</ts1:uri></ts1:results><ts1:results><ts1:notation>34B12</ts1:notation><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>ic</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://iconclass.org/34B12</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>afo</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>afo</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:type>http://www.yso.fi/onto/afo-meta/Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/afo/p1287</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>juho</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>jupo</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>kauno</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>keko</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>kito</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>koko</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:type>http://www.yso.fi/onto/afo-meta/Concept</ts1:type><ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type><ts1:type>http://www.yso.fi/onto/kauno-meta/Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/koko/p37252</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>kto</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>kulo</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>liito</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>mero</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:hiddenLabel>Cat</ts1:hiddenLabel><ts1:prefLabel>Cats</ts1:prefLabel><ts1:vocab>mesh</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/mesh/D002415</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>muso</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>pto</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>puho</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>maotao</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>tero</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/tero/p19378</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>tsr</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:exvocab>yso</ts1:exvocab><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>valo</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results><ts1:results><ts1:prefLabel>cat</ts1:prefLabel><ts1:vocab>yso</ts1:vocab><ts1:type>skos:Concept</ts1:type><ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type><ts1:lang>en</ts1:lang><ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri></ts1:results></ts1:response>";
        String url = "api.finto.fi/rest/v1/search/";
        Map<String, String> headers = new HashMap<>();
        Map<String, List<String>> urlParams = new HashMap<>();

        List<String> values = new ArrayList<>();
        values.add("cat");
        urlParams.put("query", values);

        values = new ArrayList<>();
        values.add("en");
        urlParams.put("lang", values);

        RESTClient restClient = RESTClientFactory.createRESTClient("get");
        // Send request to the service endpoint
        ClientResponse restResponse = restClient.send(this.baseUrl + url, "", urlParams, headers);

        String data = restResponse.getData();

        assertEquals(result, new String(data.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
        assertEquals("text/xml;charset=utf-8", restResponse.getContentType());
    }
}
