#!/bin/bash

./gradlew clean assemble
mkdir exec
cd exec
cp ../build/libs/*.war retropie-romfilter.war
java -jar retropie-romfilter.war
