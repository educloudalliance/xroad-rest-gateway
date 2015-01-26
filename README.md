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


## Data Exchange Layer X-Road

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
* [XRd4J - Java Library for X-Road v6](https://github.com/petkivim/xrd4j)

