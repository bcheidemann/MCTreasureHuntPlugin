#!/usr/bin/env bash
ASSET_DOWNLOAD_URL="https://github.com/google/google-java-format/releases/download/v1.16.0/google-java-format-1.16.0-all-deps.jar"
ASSET_FILE_PATH="tools/google-java-format-1.16.0-all-deps.jar"

if ! test -f "$ASSET_FILE_PATH";
then
  echo "Downloading google-java-format..."
  curl -L "$ASSET_DOWNLOAD_URL" -o "$ASSET_FILE_PATH"
fi

echo "Formatting..."
java -jar "$ASSET_FILE_PATH" $@ \
  src/main/java/uk/co/catlord/spigot/MCTreasureHuntPlugin/*.java \
  src/main/java/uk/co/catlord/spigot/MCTreasureHuntPlugin/**/*.java
