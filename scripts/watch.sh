#!/usr/bin/env bash
serverName="MCTreasureHuntPluginDevServer"

reloadbkg() {
  echo "Reloading..."
  echo ""
  sleep 2
  ./scripts/build.sh
  ./scripts/copy-build-output.sh
  screen -S "$serverName" -p 0 -X stuff "reload confirm$(echo -ne '\r')"
  echo ""
  echo "Reload complete."
  echo "Watching for changes..."
}

reload() {
  if [[ -z "$BKG_PID" ]] || ! ps -p $BKG_PID > /dev/null
  then
    reloadbkg > /dev/stdout &
    BKG_PID=$!
  fi
}

inotifywait -m -r ./src -e modify |
  while read directory file; do
    reload
  done
