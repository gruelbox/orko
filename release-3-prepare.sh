#!/bin/bash
mvn release:prepare \
    -DpreparationGoals=clean \
    -DautoVersionSubmodules=true \
    -Pintegration-test
