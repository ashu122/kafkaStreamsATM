package com.atm.account

import java.util.Properties

import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.common.serialization.Serdes

object Boot extends App {
  val props: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, classOf[Serdes.StringSerde])
    p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, classOf[JsonSerdes])
    p
  }
  CommandStream.startStream(props)
  AccountStream.startStreams(props)
}
