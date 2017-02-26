# Base java:8
FROM java:8

# Add Rest Gateway jar to container
ADD src/target/rest-gateway-*.jar rest-gateway.jar

# Entry with exec
ENTRYPOINT exec java $JAVA_OPTS -jar /rest-gateway.jar

# Expose Tomcat
EXPOSE 8080