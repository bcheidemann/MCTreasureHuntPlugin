#!/usr/bin/env bash
projectDir=$PWD
serverDir="$projectDir/server/1.19.3"

cd $serverDir
$JAVA_HOME/bin/java -Xms2G -Xmx2G -jar paper-1.19.3-365.jar --nogui
