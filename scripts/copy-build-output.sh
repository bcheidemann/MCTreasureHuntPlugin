#!/usr/bin/env bash
projectDir=$PWD
serverDir="$projectDir/server/1.19.3"

rm $serverDir/plugins/MCTreasureHuntPlugin-latest.jar
cp target/MCTreasureHuntPlugin-latest.jar server/1.19.3/plugins/MCTreasureHuntPlugin-latest.jar
