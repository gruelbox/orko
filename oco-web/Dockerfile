FROM openjdk:8
ARG JAR_FILE
ADD target/lib /usr/share/app/lib
ADD target/oco-web.jar /usr/share/app/app.jar
ADD docker-config.yml /usr/share/app/config.yml
CMD java -classpath "/usr/share/app/lib" -jar /usr/share/app/app.jar server /usr/share/app/config.yml
EXPOSE 8080