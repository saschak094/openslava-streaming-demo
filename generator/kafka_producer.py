#!/usr/bin/python
"""Simulates user activities of a websites and sends the values to Kafka """
import time
import json
import argparse
import random
import datetime
from kafka import KafkaProducer

# Dummy user data
actions = ['login', 'checked_balance', 'cash_transfered',
           'creditcard_payment', 'contacted_support', 'logout']
users = [('U001', 'DarthVader'), ('U002', 'C-3PO'), ('U003', 'Chewbacca'), ('U004', 'HanSolo'), ('U005', 'LukeSkyWalker'), ('U006', 'ObiWan'), ('U007', 'R2D2'), ('U008', 'Yoda'),
         ('U009', 'SpiderMan'), ('U010', 'IronMan'), ('U011', 'DeadPool'), ('U012', 'Thor'), ('U013', 'Hulk')]


def produce():
    """Simulates a user activity and stores the object in Kafka"""
    # argument parsing
    args = parse_args()
    broker = args.broker_host + ':9092'
    topic = args.kafka_topic
    print 'Starting up ... Broker: ' + broker
    # connect to Kafka
    producer = KafkaProducer(bootstrap_servers=broker)
    counter = 1
    while True:
        # send messages
        for user in users:
            user_activity = generate_activity(user)
            producer.send(topic, user_activity)
            print 'Message ' + str(counter) + ' send...'
            time.sleep(0.5)
            counter += 1


def generate_activity(user):
    """Generates a user activity formatted as JSON"""
    data = {}
    random_index = random.randint(0, 5)
    data['uid'] = user[0]
    data['username'] = user[1]
    data['action'] = actions[random_index]
    data['ts'] = datetime.datetime.now().isoformat()
    return json.dumps(data)


def parse_args():
    """Parses the necessary environment parameters"""
    parser = argparse.ArgumentParser(
        description='Handover of environment properties')
    parser.add_argument('broker_host',
                        help='hostname/ip of the kafka broker')
    parser.add_argument('kafka_topic',
                        help='name of the kafka topic to store the messages')
    return parser.parse_args()


def main():
    """Start simulating user activity"""
    produce()


if __name__ == "__main__":
    main()
