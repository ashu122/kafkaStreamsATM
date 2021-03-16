package com.atm.account

import java.time.Duration
import java.util.Properties

import com.fasterxml.jackson.databind.JsonNode
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.{Consumed, KStream, Predicate}
import org.apache.kafka.streams.{KafkaStreams, KeyValue, StreamsBuilder, StreamsConfig}

object CommandStream{

  object TransType extends Enumeration {
    type Trans = Value
    val Read, Credit, Debit = Value
  }
  case class Command(account: String, amount: Double, transType: TransType.Trans)

  def startStream(props: Properties): Unit = {
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "command-stream")
    val builder: StreamsBuilder = new StreamsBuilder
    val branches: Array[KStream[String, JsonNode]] = builder
      .stream[String, JsonNode]("atm.command", Consumed.`with`(Serdes.String, new JsonSerdes()))
      .map((k,v) => mapByCommandType(k, v))
      .branch(buildPredicate("Credit"),
        buildPredicate("Debit"),
        buildPredicate("Read"),
      )

    branches(0).to("atm.account")
    branches(1).to("atm.account")

    val streams: KafkaStreams = new KafkaStreams(builder.build(), props)
    streams.start()

    sys.ShutdownHookThread {
      streams.close(Duration.ofSeconds(10))
    }
  }

  def mapByCommandType(key: String, value: JsonNode): KeyValue[String, JsonNode] = {
    val `type`: String = value.get("type").asText
    `type` match {
      case "Credit" | "Debit" | "Read" => {
        val account = value.get("account").asText
        new KeyValue[String, JsonNode](account, value)
      }
      case _ => new KeyValue[String, JsonNode](key, value)
    }
  }

  private def buildPredicate(`type`: String): Predicate[String, JsonNode] =
    (_: String, jsonNode: JsonNode) => {
      jsonNode.get("type").asText.equals(`type`)
    }
}
