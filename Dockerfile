FROM maven:3.6.1-jdk-8 AS build
COPY . /usr/src/
WORKDIR /usr/src
RUN mvn -B -U clean package -Dskip.failsafe.tests=true -Dskip.surefire.tests=true -Dspotbugs.skip=true -Dcheckstyle.skip=true

FROM java:8
COPY --from=build /usr/src/orko-app/target/orko-app.jar /opt/orko-app.jar
COPY --from=build /usr/src/orko-app/src/main/jib/docker-config.yml /opt/docker-config.yml
EXPOSE 8080
CMD ["java", "-jar", "-server", "-Xmx186m", "-Xms186m", "-Xss512k", "-XX:MaxMetaspaceSize=96m", "-XX:+UseG1GC", "-Dsun.net.inetaddr.ttl=60", "-Dio.netty.leakDetectionLevel=advanced", "-Dmorf.mysql.noadmin=true", "-Ddw.server.connector.port=8080", "-jar", "/opt/orko-app.jar", "server", "/opt/docker-config.yml"]