#!/bin/bash
mvn release:prepare \
    -Dtag=0.8.5  \
    -DpreparationGoals=clean \
    -DautoVersionSubmodules=true
    -Prelease \
    -Darguments='-Prelease'
