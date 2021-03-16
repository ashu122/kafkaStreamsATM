package com.atm.account

import com.fasterxml.jackson.databind.JsonNode
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}
import org.apache.kafka.connect.json.{JsonDeserializer, JsonSerializer}

class JsonSerdes extends Serde[JsonNode] {
  override def serializer(): Serializer[JsonNode] = new JsonSerializer
  override def deserializer(): Deserializer[JsonNode] = new JsonDeserializer
}