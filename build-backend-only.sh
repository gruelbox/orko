#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
mvn clean package -f "$DIR" -U -Pbundle -Dmaven.test.skip=true -Dskip.failsafe.tests=true -Dcheckstyle.skip=true -T 1C