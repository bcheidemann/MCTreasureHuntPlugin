#!/usr/bin/env bash
projectDir=$PWD
serverDir="$projectDir/server/1.19.3"
serverName="MCTreasureHuntPluginDevServer"
serverLogFile="logs/server.log"
launchCommand="$JAVA_HOME/bin/java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044 -Xms2G -Xmx2G -jar paper-1.19.3-365.jar --nogui"

cd $serverDir
screen -S $serverName -L -Logfile $serverLogFile -mS $launchCommand
cd $projectDir
