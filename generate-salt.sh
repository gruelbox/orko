#!/bin/bash
java \
-cp \
orko-app/target/orko-app.jar \
com.gruelbox.orko.auth.Hasher \
--salt
