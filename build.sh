#!/bin/bash
mvn clean package -Pproduction -Dmaven.test.skip=true -Dskip.failsafe.tests=true -T 1C