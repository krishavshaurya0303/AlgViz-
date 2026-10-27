#!/bin/sh
# Start AlgoViz on macOS / Linux. Needs Java 17+ (JDK) installed.
cd "$(dirname "$0")"
if ! command -v java >/dev/null 2>&1; then
  echo "Java is not installed. Install JDK 17 or newer from https://adoptium.net and run this again."
  exit 1
fi
echo "Starting AlgoViz... the first start downloads JavaFX and can take a few minutes."
chmod +x mvnw
./mvnw -q javafx:run
