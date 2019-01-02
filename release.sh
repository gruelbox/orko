#!/bin/bash
mvn -Prelease --batch-mode -Dtag=0.8.5 release:prepare \
                 -DreleaseVersion=0.8.5 \
                 -DdevelopmentVersion=0.8.6-SNAPSHOT \
                 -DskipTests=true
