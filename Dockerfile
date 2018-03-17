FROM java:8-jre
EXPOSE 8080 8081
COPY target /app/
CMD java $JAVA_OPTS -Ddw.server.connector.port=$PORT -jar target/oco-0.0.1-SNAPSHOT.jar server example-config.yml
