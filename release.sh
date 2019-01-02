#!/bin/bash
set -e
./licences.sh
git add -A
git diff-index --quiet HEAD || git commit -m "Update licence headers"
mvn release:prepare \
    -DpreparationGoals=clean \
    -DautoVersionSubmodules=true \
    -Pintegration-test
