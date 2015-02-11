package com.pkrete.xrd4j.tools.rest_gateway.util;

import com.pkrete.xrd4j.common.member.ConsumerMember;
import com.pkrete.xrd4j.common.member.ProducerMember;
import com.pkrete.xrd4j.common.util.MessageHelper;
import com.pkrete.xrd4j.tools.rest_gateway.endpoint.ConsumerEndpoint;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides utility methods for Consumer Gateway implementation.
 *
 * @author Petteri Kivimäki
 */
public class ConsumerGatewayUtil {

    private final static Logger logger = LoggerFactory.getLogger(ConsumerGatewayUtil.class);

    /**
     * Goes through the given properties and extracts all the defined consumer
     * endpoints. Returns a map containing service id - consumer endpoint
     * key-value pairs.
     * @param endpoints consumer properties
     * @param props REST Consumer Gateway general properties
     * @return map containing service id - consumer endpoint key-value pairs
     */
    public static Map<String, ConsumerEndpoint> extractConsumers(Properties endpoints, Properties props) {
        Map<String, ConsumerEndpoint> results = new TreeMap<String, ConsumerEndpoint>();
        logger.info("Start extracting consumer endpoints from properties.");
        if (endpoints == null || endpoints.isEmpty()) {
            logger.warn("No endpoints were founds. The list was null or empty.");
            return results;
        }

        int i = 0;
        String key = Integer.toString(i);

        // Loop through all the endpoints
        while (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_ID)) {

            String clientId = props.getProperty(Constants.CONSUMER_PROPS_ID_CLIENT);
            String serviceId = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_ID);
            String path = endpoints.getProperty(key + "." + Constants.CONSUMER_PROPS_PATH);
            String verb = "GET";

            if (serviceId == null || serviceId.isEmpty() || path == null || path.isEmpty()) {
                logger.warn("ID or path is null or empty. Consumer endpoint skipped.");
                i++;
                key = Integer.toString(i);
                continue;
            }
            // Path must end with "/"
            if (!path.endsWith("/")) {
                path += "/";
            }

            logger.info("New consumer endpoint found. ID : \"{}\", path : \"{}\".", serviceId, path);

            ConsumerEndpoint endpoint = new ConsumerEndpoint(serviceId, clientId, path);

            // Create ProducerMember object
            if (!ConsumerGatewayUtil.setProducerMember(endpoint)) {
                logger.warn("Creating producer member failed. Consumer endpoint skipped.");
                i++;
                key = Integer.toString(i);
                continue;
            }

            // Set default values to namespace properties
            endpoint.setNamespaceDeserialize(props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE));
            endpoint.setNamespaceSerialize(props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE));
            endpoint.setPrefix(props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE));

            // Client id
            if (endpoints.containsKey(key + "." + Constants.CONSUMER_PROPS_ID_CLIENT)) {
                String value = endpoints.getProperty(key + "." + Constants.CONSUMER_PROPS_ID_CLIENT);
                endpoint.setClientId(value);
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.CONSUMER_PROPS_ID_CLIENT, value);
            }
            // HTTP verb
            if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_VERB)) {
                String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_VERB);
                if (value != null) {
                    value = value.toUpperCase();
                }
                endpoint.setHttpVerb(value);
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.ENDPOINT_PROPS_VERB, value);
            }
            // Modify URLs
            if (endpoints.containsKey(key + "." + Constants.CONSUMER_PROPS_MOD_URL)) {
                String value = endpoints.getProperty(key + "." + Constants.CONSUMER_PROPS_MOD_URL);
                endpoint.setModifyUrl(MessageHelper.strToBool(value));
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.CONSUMER_PROPS_MOD_URL, value);
            }
            // ServiceResponse namespace
            if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE)) {
                String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE);
                endpoint.setNamespaceDeserialize(value);
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE, value);
            }
            // ServiceRequest namespace
            if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE)) {
                String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE);
                endpoint.setNamespaceSerialize(value);
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE, value);
            }
            // ServiceRequest namespace prefix
            if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE)) {
                String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE);
                endpoint.setPrefix(value);
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE, value);
            }

            // Create ConsumerMember object
            if (!ConsumerGatewayUtil.setConsumerMember(endpoint)) {
                logger.warn("Creating consumer member failed. Consumer endpoint skipped.");
                i++;
                key = Integer.toString(i);
                continue;
            }

            // Set namespaces
            endpoint.getProducer().setNamespaceUrl(endpoint.getNamespaceSerialize());
            endpoint.getProducer().setNamespacePrefix(endpoint.getPrefix());

            results.put(verb + " " + path, endpoint);

            // Increase counter by one
            i++;
            // Update keyD
            key = Integer.toString(i);
        }

        logger.info("{} consumer endpoints extracted from properties.", results.size());
        return ((TreeMap) results).descendingMap();
    }

    /**
     * Goes through the given endpoint and tries to find an endpoint which
     * id matches the given service id. If no match is found, then tries
     * to find a partial match.
     * @param serviceId service id to be looked for
     * @param endpoints list of endpoints
     * @return endpoint matching the given service id or null
     */
    public static ConsumerEndpoint findMatch(String serviceId, Map<String, ConsumerEndpoint> endpoints) {
        if (endpoints.containsKey(serviceId)) {
            logger.debug("Found match by service id : \"{}\".", serviceId);
            return endpoints.get(serviceId);
        }
        for (String key : endpoints.keySet()) {
            String keyMod = key.replaceAll("\\{resourceId\\}", "([\\\\w\\\\-]+?)");
            logger.trace("Modified key used for comparison : \"{}\".", keyMod);
            if (serviceId.matches(keyMod)) {
                logger.debug("Found partial match by service id. Request value : \"{}\", matching value : \"{}\".", serviceId, key);
                ConsumerEndpoint endpoint = endpoints.get(key);
                int index = key.indexOf("{");
                if (index != -1) {
                    // Get the resource id - starts from the first "{"
                    String resourceId = serviceId.substring(index);
                    // If the last character is "/", remove it
                    if (resourceId.endsWith("/")) {
                        resourceId = resourceId.substring(0, resourceId.length() - 1);
                    }
                    endpoint.setResourceId(resourceId);
                    logger.trace("Set resource id : \"{}\"", resourceId);
                }
                return endpoint;
            }
        }
        logger.debug("No match by service id was found. Service id : \"{}\".", serviceId);
        return null;
    }

    /**
     * Rewrites all the URLs in the responseStr that are matching the
     * resourcePath to point the Consumer Gateway servlet.
     * @param servletUrl URL of Consumer Gateway serlvet
     * @param resourcePath path that's rewritten to point the Consumer
     * Gateway
     * @param responseStr response to be modified
     * @return modified response
     */
    public static String rewriteUrl(String servletUrl, String resourcePath, String responseStr) {
        logger.debug("Rewrite URLs in the response to point Consumer Gateway.");
        logger.debug("Consumer Gateway URL : \"{}\".", servletUrl);
        try {
            // Remove "/{resourceId}" from resource path, and omit
            // first and last slash ('/') character
            resourcePath = resourcePath.substring(1, resourcePath.length() - 1).replaceAll("/\\{resourceId\\}", "");
            logger.debug("Resourse URL that's replaced with Consumer Gateway URL : \"http(s)://{}\".", resourcePath);
            logger.debug("New resource URL : \"{}{}\".", servletUrl, resourcePath);
            // Modify the response
            return responseStr.replaceAll("http(s|):\\/\\/" + resourcePath, servletUrl + resourcePath);
        } catch (Exception ex) {
            logger.error("Reqriting the URLs failed!");
            logger.error(ex.getMessage(), ex);
            return responseStr;
        }
    }

    /**
     * Copies the client id string into an array. [0] = instance,
     * [1] = memberClass, [2] = memberCode, [3] = subsystem. If the structure
     * of the string is not correct, null is returned.
     * @param clientId client id string
     * @return client id in an array
     */
    private static String[] clientIdToArr(String clientId) {
        if (clientId == null) {
            return null;
        }
        String[] clientArr = clientId.split("\\.");
        if (clientArr.length == 4) {
            return clientArr;
        }
        return null;
    }

    /**
     * Constructs and initializes a ConsumerMember object related to the given
     * ConsumerEndpoint. The object is constructed according to the value
     * of the clientId variable.
     * @param endpoint ConsumerEndpoint object
     * @return true if and only if creating ConsumerMember object succeeded;
     * otherwise false
     */
    private static boolean setConsumerMember(ConsumerEndpoint endpoint) {
        String[] clientIdArr = ConsumerGatewayUtil.clientIdToArr(endpoint.getClientId());
        if (clientIdArr == null) {
            logger.warn("Incorrect \"{}\" value : \"{}\".", Constants.CONSUMER_PROPS_ID_CLIENT, endpoint.getClientId());
            return false;
        } else {
            try {
                String instance = clientIdArr[0];
                String memberClass = clientIdArr[1];
                String memberCode = clientIdArr[2];
                String subsystem = clientIdArr[3];
                endpoint.setConsumer(new ConsumerMember(instance, memberClass, memberCode, subsystem));
                logger.debug("Consumer member successfully created.");
                return true;
            } catch (Exception ex) {
                logger.warn("Creating consumer member failed.");
                return false;
            }
        }
    }

    /**
     * Copies the client id string into an array. [0] = instance,
     * [1] = memberClass, [2] = memberCode, [3] = subsystem, [4] = service,
     * [5] = version. If the structure of the string is not correct, null is
     * returned.
     * @param serviceId service id string
     * @return service id in an array
     */
    private static String[] serviceIdToArr(String serviceId) {
        if (serviceId == null) {
            return null;
        }
        String[] serviceArr = serviceId.split("\\.");
        if (serviceArr.length == 6) {
            return serviceArr;
        }
        return null;
    }

    /**
     * Constructs and initializes a ProducerMember object related to the given
     * ConsumerEndpoint. The object is constructed according to the value
     * of the serviceId variable.
     * @param endpoint ConsumerEndpoint object
     * @return true if and only if creating ProducerMember object succeeded;
     * otherwise false
     */
    private static boolean setProducerMember(ConsumerEndpoint endpoint) {
        String[] serviceIdArr = ConsumerGatewayUtil.serviceIdToArr(endpoint.getServiceId());
        if (serviceIdArr == null) {
            logger.warn("Incorrect \"{}\" value : \"{}\".", Constants.ENDPOINT_PROPS_ID, endpoint.getServiceId());
            return false;
        } else {
            try {
                String instance = serviceIdArr[0];
                String memberClass = serviceIdArr[1];
                String memberCode = serviceIdArr[2];
                String subsystem = serviceIdArr[3];
                String service = serviceIdArr[4];
                String version = serviceIdArr[5];
                endpoint.setProducer(new ProducerMember(instance, memberClass, memberCode, subsystem, service, version));
                logger.debug("Producer member succesfully created.");
                return true;
            } catch (Exception ex) {
                logger.warn("Creating producer member failed.");
                return false;
            }
        }
    }
	
    /**
     * Creates a ConsumerEndpoint that points to a service which configuration
     * information is not in the configuration file. Resource path is used as 
     * service id, namespace and prefix can be defined using HTTP headers.
     * @param request HTTP request
     * @param props consumer gateway properties that contain default namespace
     * and prefix
     * @param resourcePath resource path that was called, used as service id
     * @return ConsumerEndpoint object
     */
    public static ConsumerEndpoint createUnconfiguredEndpoint(HttpServletRequest request, Properties props, String resourcePath) {
        logger.debug("Create a consumer endpoint that points to a service defined by resource path.");
        String resourceId = null;      
        // Check if a resource id is present in the resource path. 
        // Pattern for resource path and resource id.
        String pattern = "/(.+?)/(.+)";
        Pattern regex = Pattern.compile(pattern);
        Matcher m = regex.matcher(resourcePath);
        // If resource id is found, pplit resource id and resource path
        if (m.find()) {
            resourcePath = m.group(1);
            resourceId = m.group(2).substring(0, m.group(2).length() - 1);
            logger.info("Resource id detected. Resource path : \"{}\". Resource id : \"{}\".", resourcePath, resourceId);
        } else {
            // Remove slashes, they're not part of service id
            resourcePath = resourcePath.replaceAll("/", "");
        }
        // Get client id
        String clientId = props.getProperty(Constants.CONSUMER_PROPS_ID_CLIENT);
        // Create new endpoint
        ConsumerEndpoint endpoint = new ConsumerEndpoint(resourcePath, clientId, "");
        // Set resouce id
        endpoint.setResourceId(resourceId);
        // Parse consumer and producer from ids
        if (!ConsumerGatewayUtil.setConsumerMember(endpoint) || !ConsumerGatewayUtil.setProducerMember(endpoint)) {
            // Set endpoint to null if parsing failed
            endpoint = null;
        } else {
            // Get defalut namespace and prefix from properties
            String ns = props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE);
            String prefix = props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE);
            // Get namespace and prefix headers
            String nsHeader = request.getHeader(Constants.XRD_HEADER_NAMESPACE_SERIALIZE);
            String prefixHeader = request.getHeader(Constants.XRD_HEADER_NAMESPACE_PREFIX_SERIALIZE);
            // Set namespace received from header, if not null or empty
            if (nsHeader != null && !nsHeader.isEmpty()) {
                ns = nsHeader;
                logger.debug("\"{}\" HTTP header found. Value : \"{}\".", Constants.XRD_HEADER_NAMESPACE_SERIALIZE, ns);
            }
            // Set prefix received from header, if not null or empty
            if (prefixHeader != null && !prefixHeader.isEmpty()) {
                prefix = prefixHeader;
                logger.debug("\"{}\" HTTP header found. Value : \"{}\".", Constants.XRD_HEADER_NAMESPACE_PREFIX_SERIALIZE, prefix);
            }
            // Set namespaces
            endpoint.getProducer().setNamespaceUrl(ns);
            endpoint.getProducer().setNamespacePrefix(prefix);
        }
        return endpoint;
    }
}
