# oco

How to start the oco application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/oco-0.0.1-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8080`

Health Check
---

To see your applications health enter url `http://localhost:8081/healthcheck`
