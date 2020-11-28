#!/bin/bash
mvn release:prepare \
    -DpreparationGoals=clean \
    -DautoVersionSubmodules=true \
    -Pui,e2etest