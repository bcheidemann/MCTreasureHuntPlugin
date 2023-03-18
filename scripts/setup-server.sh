#!/usr/bin/env bash
projectDir=$PWD
serverDir="$projectDir/server/1.19.3"

cd $serverDir
echo "Starting server..."
$JAVA_HOME/bin/java -Xms2G -Xmx2G -jar paper-1.19.3-365.jar --nogui
echo "Accepting EULA..."
echo "eula=true" > $serverDir/eula.txt
echo "Creating plugins directory..."
mkdir $serverDir/plugins
echo "Creating plugin config directory..."
mkdir -p $serverDir/plugins/MCTreasureHuntPlugin
echo "Copying plugin config..."
echo "cp $projectDir/config/checkpoints.json $serverDir/plugins/MCTreasureHuntPlugin/checkpoints.json"
echo "Done."
