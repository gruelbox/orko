#!/bin/bash
mvn clean package -Pproduction -Dmaven.test.skip=true -Dskip.failsafe.tests=true -Dspotbugs.skip=true -Dcheckstyle.skip=true -T 1C