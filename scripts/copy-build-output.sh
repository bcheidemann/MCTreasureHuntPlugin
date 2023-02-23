#!/usr/bin/env bash
projectDir=$PWD
serverDir="$projectDir/server/1.19.3"

rm $serverDir/plugins/MCTreasureHuntPlugin-1.0.jar
cp target/MCTreasureHuntPlugin-1.0.jar server/1.19.3/plugins/MCTreasureHuntPlugin-1.0.jar
