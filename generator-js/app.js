const kafka = require('kafka-node')
const HighLevelProducer = kafka.HighLevelProducer
const KafkaClient = kafka.KafkaClient

// build up the Kafka client zookeeper-less
const client = new KafkaClient({
  kafkaHost: 'localhost:9092'
})

// configure
const argv = require('optimist').argv
const topic = argv.topic || 'openslava'
let count = 100
let rets = 0
const producer = new HighLevelProducer(client)

producer.on('ready', function () {
  setInterval(send, 500)
})

producer.on('error', function (err) {
  console.log('error', err)
})

function send () {
  let message = JSON.stringify({
    ts: new Date().toISOString()
  })

  producer.send([
    {topic: topic, messages: [message]}
  ], function (err, data) {
    if (err) console.log(err)
    else console.log('send %d messages', ++rets)
    if (rets === count) process.exit()
  })
}
