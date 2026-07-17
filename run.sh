#!/bin/bash
# Compiles and runs the Smart Library Circulation & Automation System.
# Usage: ./run.sh
set -e
mkdir -p bin
echo "Compiling..."
javac -d bin $(find src -name "*.java")
echo "Running..."
java -cp bin Main
