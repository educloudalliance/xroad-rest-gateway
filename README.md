# Joint X-Road REST Gateway development

This repository will be the home for REST/JSON support solutions in X-Road. Mandate for joint development is based on [MoU which was signed by Katainen and Ansip](https://github.com/educloudalliance/xroad-rest-gateway/blob/master/MoU-Ansip-Katainen.md). The development and repository is shared between Estonia and Finland. Below is list of people who initiated the co-operation in Skype meeting which was held 18.12.2014. 

People involved in initiation of co-operation:
* Andres Kütt (Estonian Information System Authority, RIA, Estonia)
* Alar Jõeste (Cybernetica, Estonia)
* Margus Freudenthal (Cybernetica, Estonia)
* Petteri Kivimäki (Population Register Centre, Finland)
* Jarkko Moilanen (Ministry of Education and Culture, Finland)

## Aim 2015
In this repository you will find (2015) Proof of Concept level code for service that will enable REST support in X-Road version 6. The solution will not be part of Security Server, but more like a "REST Proxy". The solution could be included to Secutiry Server in the future, but that remains to be seen. No plans for that has been made.

First aim is to get first practical REST API integrated to X-Road, document the process and open the code. We also need to test and evaluate the toolchain for example for WSDL-RAML conversions and other things.

In parallel with the technical development we will collect more use cases from Finland about REST/JSON APIs that need to be integrated with X-Road. Aim is not to make automated solution which covers 100% of cases. We will cheer loudly if 80% coverage is achieved.

## Try It Out

The fastest and easiest way to try out the application is to [download](https://github.com/educloudalliance/xroad-rest-gateway/releases/download/v0.0.10/rest-gateway-0.0.10.jar) the executable jar version (```rest-gateway-0.0.10.jar```) and run it: ```java -jar rest-gateway-0.0.10.jar```. The application is accessible at:

```
http://localhost:8080/rest-gateway-0.0.10/Provider

http://localhost:8080/rest-gateway-0.0.10/Consumer
```

The Provider WSDL description is accessible at:

```
http://localhost:8080/rest-gateway-0.0.10/Provider?wsdl
```

Executable JAR version supports ```propertiesDirectory``` command line variable that makes it possible to load REST Gateway configuration files from defined directory outside of the JAR file. Otherwise the default configuration shipped with the JAR is used. For example

```
java -jar -DpropertiesDirectory=/my/custom/path rest-gateway.jar
```

More detailed usage examples are available in [wiki](https://github.com/educloudalliance/xroad-rest-gateway/wiki/REST-Gateway-0.0.10#usage).

## Running the Docker Image

REST Gatewat is available as Docker image. 

```
docker run -p 8080:8080 petkivim/xroad-rest-gateway
```

If customized properties are used, the host directory containing the properties files must be mounted as a data directory. In addition, the directory containing the properties files inside the container must be set using ```JAVA_OPTS``` and```propertiesDirectory``` property.

```
docker run -p 8080:8080 -v /host/dir/conf:/my/conf -e "JAVA_OPTS=-DpropertiesDirectory=/my/conf"  petkivim/xroad-rest-gateway
```

## Building the Docker Image

While you are in the project root directory, build the image using the docker build command. The ```-t``` parameter gives your image a tag, so you can run it more easily later. Don’t forget the ```.``` command, which tells the docker build command to look in the current directory for a file called Dockerfile.

```
docker build -t xroad-rest-gateway .
```

## Data Exchange Layer X-Road

The X-Road was launched in 2001. The data exchange layer X-Road is a technical and organisational environment, which enables secure Internet-based data exchange between the state’s information systems.

The X-Road is not only a technical solution, the exchange of data with the databases belonging to the state information system and between the databases belonging to the state information system shall be carried out through the data exchange layer of the state information system. The X-Road allows institutions/people to securely exchange data as well as to ensure people’s access to the data maintained and processed in state databases.

Public and private sector enterprises and institutions can connect their information system with the X-Road. This enables them to use X-Road services in their own electronic environment or offer their e-services via the X-Road. Joining the X-Road enables institutions to save resources, since the data exchange layer already exists. This makes data exchange more effective both inside the state institutions as well as regarding the communication between a citizen and the state.

### RPM Packaging

The X-Road REST Gateway also builds RPMs for use with RHEL (or derivatives) and Apache Tomcat.

    $ mvn -f src/pom.xml -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true {clean,package}

### Encryption of Message Content

Starting from version 0.0.10 REST Gateway supports encryption/decryption of message content. More information and instructions for configuration can be found in [wiki](https://github.com/educloudalliance/xroad-rest-gateway/wiki/Encryption).

By default plaintext configuration is enabled. The software can be built with encryption configuration enabled using the command below.

```mvn clean install -P encrypted```

Running integration tests with plaintext configuration enabled:

```mvn clean install -P itest -P plaintext```

Running integration tests with encryption configuration enabled:

```mvn clean install -P itest -P encrypted```

### Links to material

* [Data Exchange Layer X-Road](https://www.ria.ee/x-road/)
* [X-Road overview ](https://speakerdeck.com/educloudalliance/x-road-overview)
* [X-Road regulations](https://speakerdeck.com/educloudalliance/x-road-regulations)
* [Palveluväylä kehitysympäristö (Finnish only)](http://palveluvayla.fi)
* [Requirements for Information Systems and Adapter
Servers](http://x-road.ee/docs/eng/x-road_service_protocol.pdf)
* [XRd4J - Java Library for X-Road v6](https://github.com/petkivim/xrd4j)

