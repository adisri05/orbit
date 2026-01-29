#!/bin/bash

# Script to send test events to Kafka for Analytics Service verification
# Usage: ./send-test-events.sh

KAFKA_BOOTSTRAP="localhost:9092"
TOPIC="learning-events"

echo "Sending LESSON_STARTED event..."
cat <<EOF | kafka-console-producer --bootstrap-server $KAFKA_BOOTSTRAP --topic $TOPIC
{
  "eventType": "LESSON_STARTED",
  "userId": "user123",
  "pathId": "pathA",
  "courseId": "courseB",
  "lessonId": "lesson1",
  "occurredAt": "2025-01-26T10:00:00Z"
}
EOF

sleep 1

echo "Sending LESSON_COMPLETED event..."
cat <<EOF | kafka-console-producer --bootstrap-server $KAFKA_BOOTSTRAP --topic $TOPIC
{
  "eventType": "LESSON_COMPLETED",
  "userId": "user123",
  "pathId": "pathA",
  "courseId": "courseB",
  "lessonId": "lesson1",
  "occurredAt": "2025-01-26T10:10:00Z"
}
EOF

echo "Events sent successfully!"

