#!/bin/bash

# Start Recommendation Service
# Run this in a separate terminal

echo "=========================================="
echo "Starting Recommendation Service"
echo "=========================================="
echo ""

cd "$(dirname "$0")" || exit 1

echo "Current directory: $(pwd)"
echo ""
echo "Starting service on port 8084..."
echo "Press Ctrl+C to stop"
echo ""

mvn spring-boot:run

