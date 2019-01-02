#!/bin/bash
set -e
./licences.sh
mvn release:prepare \
    -DpreparationGoals=clean \
    -DautoVersionSubmodules=true
    -Pintegration-test
