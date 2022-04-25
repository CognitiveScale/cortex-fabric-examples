import argparse
import json
import threading
from json import JSONDecodeError
import logging
from confluent_kafka import Producer, Consumer, KafkaException
import sys
import cuid
import random
from BlockingCache import BlockingCache
import signal

signal.signal(signal.SIGINT, lambda q: exit(1))
# Create logger for consumer (logs will be emitted when poll() is called)
consumer_logger = logging.getLogger('consumer')
consumer_logger.setLevel(logging.DEBUG)
handler = logging.StreamHandler()
handler.setFormatter(logging.Formatter('%(asctime)-15s %(levelname)-8s %(message)s'))
consumer_logger.addHandler(handler)
cache=None
headers= {}

def gen_payload():
    randomlist = []
    for i in range(0, random.randint(1, 10)):
        n = random.randint(1, 30)
        randomlist.append(n)
    return {'numbers': randomlist}


def gen_invoke_request():
    cuidStr = cuid.cuid()
    message = {
        "correlationId": cuidStr,
        "agentName": "cortex/hello_agent",
        "projectId": "johan",
        "serviceName": "input",
        "payload": gen_payload(),
    }
    if cache is not None:
        cache.put(cuidStr, message)
    print(f'>>> SENT {cuidStr}')
    return json.dumps(message)


def handleCacheResponse(msg):
    correlationId = msg.get('correlationId')
    if correlationId is None:
        return consumer_logger.error(f'Message is missing correlationId {json.dumps(msg)}')
    request = cache.peek(correlationId)
    if request is None:
        return consumer_logger.error(f'Request not found for correlationId {correlationId}: {json.dumps(msg)}')
    # TODO do something with the response ..
    print(
        f'<<< GOT ID: {correlationId} STATUS:{msg.get("status")} REQ:{json.dumps(request.get("payload"))} RESP:{json.dumps(msg.get("response"))}')
    cache.remove(correlationId)


def start_cache_consumer(conf, topic):
    c = Consumer(conf, logger=consumer_logger)

    def print_assignment(consumer, partitions):
        print('Assignment:', partitions)

    # Subscribe to topics
    c.subscribe([topic], on_assign=print_assignment)

    # Read messages from Kafka, print to stdout
    try:
        while True:
            msg = c.poll(timeout=1.0)
            if msg is None:
                continue
            if msg.error():
                raise KafkaException(msg.error())
            else:
                try:
                    msgJson = json.loads(msg.value())
                    handleCacheResponse(msgJson)
                except JSONDecodeError:
                    print(f'Non-JSON response {msg.value()}')
                finally:
                    c.commit(message=msg, asynchronous=False)  # commit offset
    except KeyboardInterrupt:
        sys.stderr.write('%% Aborted by user\n')
    finally:
        # Close down consumer to commit final offsets.
        c.close()


def start_console_consumer(conf, topic, expectedCount = 0):
    c = Consumer(conf, logger=consumer_logger)
    receivedCount = 0
    def print_assignment(consumer, partitions):
        print('Assignment:', partitions)

    # Subscribe to topics
    c.subscribe([topic], on_assign=print_assignment)

    # Read messages from Kafka, print to stdout
    try:
        while True:
            msg = c.poll(timeout=1.0)
            if msg is None:
                continue
            if msg.error():
                raise KafkaException(msg.error())
            else:
                receivedCount += 1;
                try:
                    msg_json = json.loads(msg.value())
                    print(f'<<< RECEIVED STATUS:{msg_json.get("status")} RESP:{json.dumps(msg_json.get("response"))}')
                except JSONDecodeError:
                    print(f'Non-JSON response {msg.value()}')
                finally:
                    c.commit(message=msg, asynchronous=False)  # commit offset
                if 0 < expectedCount <= receivedCount:
                    print(f'Received {receivedCount} messages')
                    break
    except KeyboardInterrupt:
        sys.stderr.write('%% Aborted by user\n')
    finally:
        # Close down consumer to commit final offsets.
        c.close()


def start_producer(conf, topic, opts):
    p = Producer(**conf)

    def delivery_callback(err, msg):
        if err:
            sys.stderr.write('%% Message failed delivery: %s\n' % err)
        # else:
        #     sys.stderr.write('%% Message delivered to %s [%d] @ %d\n' %
        #                      (msg.topic(), msg.partition(), msg.offset()))

    msg_headers = opts.get("headers")
    for i in range(0, opts.get("num_records", 10)):
        try:
            p.produce(topic, value=gen_invoke_request(), headers=msg_headers, callback=delivery_callback)
        except BufferError:
            sys.stderr.write('%% Local producer queue is full (%d messages awaiting delivery): try again\n' % len(p))
    sys.stderr.write('%% Waiting for %d deliveries\n' % len(p))
    p.flush()  # Ensure all message are sent


def service_shutdown(signum, frame):
    print('Caught signal %d' % signum)
    exit(1)

if __name__ == '__main__':
    signal.signal(signal.SIGTERM, service_shutdown)
    signal.signal(signal.SIGINT, service_shutdown)

    parser = argparse.ArgumentParser()
    parser.add_argument('-b', '--bootstrap', help="Bootstrap url", required=True)
    parser.add_argument('-g', '--group', help="Consumer Group", required=True)
    parser.add_argument('-i', '--intopic', help="inbond topic (consumer)", required=True)
    parser.add_argument('-o', '--outtopic', help="outbound topic (producer)", required=True)
    parser.add_argument('-e', '--errtopic', help="error topic (consumer)")
    parser.add_argument('--headers', help="JSON string containing headers to attach to the messages")
    parser.add_argument('-n', '--numrecords', type=int, default=10, help="Number of records to generate (default: 10)")
    parser.add_argument('--maxcache', type=int, default=0, help="Maximum of simultaneous requests, zero disables caching (default: 0)")

    args = parser.parse_args()
    if args.headers is not None:
        headers = json.loads(args.headers)
    sys.stderr.write(f'%% Creating {args.numrecords} messages\n')
    producer_conf = {'bootstrap.servers': args.bootstrap}
    consumer_conf = {
        'bootstrap.servers': args.bootstrap,
        'group.id': args.group,
        'session.timeout.ms': 6000,
        'enable.auto.commit': True,
        'auto.commit.interval.ms': 5000,
        'auto.offset.reset': 'earliest',  # start at the beginning if no offset for consumer group
    }

    # If max cache set use BlockingCache to limit inflight requests
    if args.maxcache > 0:
        cache = BlockingCache(max_items=args.maxcache)

    # Check to see if -T option exists
    def worker():
        if cache is None:
            # if no cache just raed responses expect at least numrecords to come back
            start_console_consumer(consumer_conf, args.intopic, args.numrecords)
        else:
            # Is cache is defined we want to match request response and emulate synchronous requests
            start_cache_consumer(consumer_conf, args.intopic)

    def error_worker():
        start_console_consumer(consumer_conf, args.errTopic)

    # spawn some threads to run worker
    workerThread = threading.Thread(target=worker, daemon=True)
    workerThread.start()
    start_producer(producer_conf, args.outtopic, {
        'num_records': args.numrecords,
        # supports either format for JWT token
        # 'headers': { "token": "eyJhbGciOiJFZERTQSIsImtpZCI6ImMzbFN3dnFHZHJsbVR5NURXRWhWZEpqaEp2RmhhTG0ta1l....."},
        # 'headers': { "Authorization": "bearer eyJhbGciOiJFZERTQSIsImtpZCI6ImMzbFN3dnFHZHJsbVR5NURXRWhWZEpq...."},
        'headers': headers,
    })
    # TODO add timeout + error ???
    if cache is not None:
        sys.stderr.write(f'%% Waiting for {cache.size()} requests to complete\n')
        cache.waitEmpty()
    else:
        workerThread.join();
