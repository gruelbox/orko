#!/bin/bash
java -cp \
orko-app/target/classes:orko-app/target/dependency/* \
com.gruelbox.orko.app.monolith.MonolithApplication \
server \
./orko-app/example-config.yml