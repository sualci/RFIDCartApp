package com.tfm.rfidcartapp.mqtt

import kotlinx.serialization.Serializable

@Serializable
data class MqttTagMessage(
    val count: Int,
    val tags: List<String>
)
