#!/bin/bash
java -cp \
orko-auth/target/classes:orko-app/target/dependency/* \
com.gruelbox.orko.auth.Hasher \
--hash $1 $2