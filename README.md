X-Road REST-SOAP adapter
=================

REST SOAP adapter implementation for X-Road

### Data Exchange Layer X-Road

The X-Road was launched in 2001. The data exchange layer X-Road is a technical and organisational environment, which enables secure Internet-based data exchange between the state’s information systems.

The X-Road is not only a technical solution, the exchange of data with the databases belonging to the state information system and between the databases belonging to the state information system shall be carried out through the data exchange layer of the state information system. The X-Road allows institutions/people to securely exchange data as well as to ensure people’s access to the data maintained and processed in state databases.

Public and private sector enterprises and institutions can connect their information system with the X-Road. This enables them to use X-Road services in their own electronic environment or offer their e-services via the X-Road. Joining the X-Road enables institutions to save resources, since the data exchange layer already exists. This makes data exchange more effective both inside the state institutions as well as regarding the communication between a citizen and the state.

### Links to material

* [Data Exchange Layer X-Road](https://www.ria.ee/x-road/)
* [X-Road overview ](https://speakerdeck.com/pilvivayla/x-road-overview)
* [X-Road regulations](https://speakerdeck.com/pilvivayla/x-road-regulations)
* [Palveluväylä kehitysympäristö (Finnish only)](http://palveluvayla.fi)
* [Requirements for Information Systems and Adapter
Servers](http://x-road.ee/docs/eng/x-road_service_protocol.pdf)

### SOAP-REST Hack
X-Road is SOAP based and rising amount of webservices are now build on top of REST(ful) APIs. This raises the need to SOA adapter. This does require some extra work but at the moment there is no option. 

#### Process is as follows
![rough process] (https://raw.githubusercontent.com/koulutuksenpilvivayla/rest-soap-adapter/master/images/rest-soap.png)

Below is paper/theory version of the adaptation. Practise might raise some more issues for example in usability of API. That said, our first aim is to get data moving. Making solution pretty is the next step. 

Principle is to use SOAP as container for 
* JSON and 
* XML response. 

Contemporary API's are capable of producing outputs in above given formats

#### REST/SOAP with JSON
When using REST API via SOAP, the wanted API request url is in **request** element.  
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
 <SOAP-ENV:Header>
 Header content
 </SOAP-ENV:Header>
 <SOAP-ENV:Body>
 <m:service xmlns:m=”URI”>
 <request>REST API call here</request>
 </m:service>
 </SOAP-ENV:Body>
 </SOAP-ENV:Envelope>
```

In response the easiest approach is to package REST response (JSON) inside SOAP message inside **response** element. It is most likely needed to use **CDATA** [unparsed data](http://www.w3schools.com/xml/xml_cdata.asp) wrapper around JSON content.  
```xml
<SOAP-ENV:Header>Header content</SOAP-ENV:Header>
<SOAP-ENV:Body>
<m:service Response xmlns:m=”URI”>
<request>REST API call here</request>
<response><![CDATA[ JSON response here]]></response>
</m:serviceResponse>
</SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

#### REST/SOAP with XML
When using REST API via SOAP, the wanted API request url is in **request** element.  
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
 <SOAP-ENV:Header>
 Header content
 </SOAP-ENV:Header>
 <SOAP-ENV:Body>
 <m:service xmlns:m=”URI”>
 <request>REST API call here</request>
 </m:service>
 </SOAP-ENV:Body>
 </SOAP-ENV:Envelope>
```

In response the easiest approach is to package REST response (XML) inside SOAP message inside **response** element without root element - just plain content in well-formed XML format. 
```xml
<SOAP-ENV:Header>Header content</SOAP-ENV:Header>
<SOAP-ENV:Body>
<m:service Response xmlns:m=”URI”>
<request>REST API call here</request>
<response>
<list>
<item>Item content</item>
</list>
</response>
</m:serviceResponse>
</SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```
