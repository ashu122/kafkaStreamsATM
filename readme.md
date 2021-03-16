# kafkaStreamATM

A simple application to deposit and withdraw money using kafka stream, scala.

# How to run the code

- Install - docker, maven and download kafka-package(kafka_2.12-2.4.1)
- Run ./build.sh file, this script needs the path of the kafka folder, use these commands.
    -  chmod +x build.sh
    -  ./build.sh /home/ayush/Downloads/kafka_2.12-2.4.1/bin
- Send Credit and Debit messages to "atm.command" topic for any account, use these commands, make sure to change the kafka package path.
    - /home/ayush/Downloads/kafka_2.12-2.4.1/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic atm.command, once connected to the producer send some Credit and Debit json messages.\
    **Messages in this topic looks like this -**
    - {"account":"AC12", "amount":"100", "type":"Credit"}
    - {"account":"AC12", "amount":"100", "type":"Debit"}
    - {"account":"AC12", "amount":"400", "type":"Debit"}
- Connect to the kafka consumer topic "atm.account.result", this topic contains the aggreagte amount present in each account after each transaction, use these commands.
    - /home/ayush/Downloads/kafka_2.12-2.4.1/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic atm.account.result --from-beginning --formatter kafka.tools.DefaultMessageFormatter --property print.key=true --property print.value=true --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer --property value.deserializer=org.apache.kafka.common.serialization.DoubleDeserializerbin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic atm.account.result --from-beginning\
    **Messages in this topic looks like this -**
    - AC12	100.0
    - AC12	0.0
- Connect to the kafka consumer topic "atm.account.refused", all the debit transaction that are failed because of not adequate amount present in the account - goes to this topic.
    - bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic atm.account.refused --from-beginning\
    **Messages in this topic looks like this -** \
    - {"account":"AC12","amount":"400","type":"Debit"}
    
# Description

- build.sh script creates 3 container for kafka, zookeeper and kafka-stream(our app), it also creates 4 kafka topics that our application uses, the names of the topics are - atm.account, atm.command, atm.account.result, atm.account.refused.
- "atm.command" is the producer that we use to send the transaction data, it required json messages. A sample json looks like this
{"account":"AC12", "amount":"100", "type":"Credit"}
{"account":"AC12", "amount":"1000", "type":"Debit"}
Type value should be either "Credit" or "Debit" and it is case sensitive.
- "atm.account" is used internally by the application
- "atm.account.result" is the our result topic.
- "atm.account.refused" contains failed trasactions

# TODO - Do Stress Testing
