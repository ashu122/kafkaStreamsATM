package com.atm.account

import java.time.Duration
import java.util.Properties

import com.fasterxml.jackson.databind.JsonNode
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.{KStream, KTable, Materialized}
import org.apache.kafka.streams.state.{KeyValueStore, QueryableStoreTypes, ReadOnlyKeyValueStore}
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig}

object AccountStream{
  var invariances: AccountService = _
  var balanceByAccountStore: ReadOnlyKeyValueStore[String, Double] = _

  def startStreams(props: Properties): Unit = {
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "account-stream")

    val builder = new StreamsBuilder
    val branches: Array[KStream[String, JsonNode]] = builder
      .stream[String, JsonNode]("atm.account")
      .branch(
        (account: String, jsonCommand: JsonNode) => {
          invariances.hasBalance(account, jsonCommand.get("amount").asDouble(0.0), jsonCommand.get("type").asText())
        },
        (account: String, jsonCommand: JsonNode) => {
          invariances.noBalance(account, jsonCommand.get("amount").asDouble(0.0), jsonCommand.get("type").asText())
        }
      )

    val result: KTable[String, Double] = groupValueByAccount(branches(0))
    result.toStream.to("atm.account.result")

    branches(1).to("atm.account.refused")
    val streams = new KafkaStreams(builder.build(), props)

    streams.setStateListener((newState: KafkaStreams.State, oldState: KafkaStreams.State) => {
      if (newState == KafkaStreams.State.RUNNING && oldState == KafkaStreams.State.REBALANCING) {
        balanceByAccountStore = streams.store("accountStore", QueryableStoreTypes.keyValueStore[String, Double]())
        invariances = new AccountService(balanceByAccountStore)
      }
    })
    streams.start()
    sys.ShutdownHookThread {
      streams.close(Duration.ofSeconds(10))
    }
  }

  def groupValueByAccount(accountStream: KStream[String, JsonNode]): KTable[String, Double] = accountStream.mapValues((jsonNode: JsonNode) => {
    val amount = jsonNode.get("amount").asDouble(0.0)
    val transType = jsonNode.get("type").asText()
    transType match {
      case "Debit" => amount * -1
      case _ => amount
    }
  }).groupByKey()
    .aggregate(() => 0.0, (_: String, balance: Double, oldBalance: Double) => oldBalance + balance,
      Materialized.as[String, Double, KeyValueStore[Bytes, Array[Byte]]]("accountStore").withKeySerde(Serdes.String).withValueSerde(org.apache.kafka.streams.scala.serialization.Serdes.doubleSerde))
}
