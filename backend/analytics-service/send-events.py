#!/usr/bin/env python3
"""
Script to send test events to Kafka for Analytics Service verification.
Requires: kafka-python library
Install: pip install kafka-python
"""

import json
from kafka import KafkaProducer
from datetime import datetime

KAFKA_BOOTSTRAP = "localhost:9092"
TOPIC = "learning-events"

def send_event(producer, event):
    """Send an event to Kafka"""
    try:
        producer.send(TOPIC, value=event)
        producer.flush()
        print(f"✓ Sent event: {event['eventType']}")
        return True
    except Exception as e:
        print(f"✗ Failed to send event: {e}")
        return False

def main():
    print("=" * 50)
    print("Sending test events to Kafka")
    print("=" * 50)
    
    # Create Kafka producer
    try:
        producer = KafkaProducer(
            bootstrap_servers=[KAFKA_BOOTSTRAP],
            value_serializer=lambda v: json.dumps(v).encode('utf-8')
        )
    except Exception as e:
        print(f"✗ Failed to connect to Kafka: {e}")
        print("Make sure Kafka is running on localhost:9092")
        return
    
    # Event 1: LESSON_STARTED
    lesson_started = {
        "eventType": "LESSON_STARTED",
        "userId": "user123",
        "pathId": "pathA",
        "courseId": "courseB",
        "lessonId": "lesson1",
        "occurredAt": "2025-01-26T10:00:00Z"
    }
    
    print("\n[1] Sending LESSON_STARTED event...")
    send_event(producer, lesson_started)
    
    # Wait a bit
    import time
    time.sleep(1)
    
    # Event 2: LESSON_COMPLETED
    lesson_completed = {
        "eventType": "LESSON_COMPLETED",
        "userId": "user123",
        "pathId": "pathA",
        "courseId": "courseB",
        "lessonId": "lesson1",
        "occurredAt": "2025-01-26T10:10:00Z"
    }
    
    print("\n[2] Sending LESSON_COMPLETED event...")
    send_event(producer, lesson_completed)
    
    producer.close()
    
    print("\n" + "=" * 50)
    print("Events sent successfully!")
    print("Wait a few seconds for the Analytics Service to process them.")
    print("=" * 50)

if __name__ == "__main__":
    main()

