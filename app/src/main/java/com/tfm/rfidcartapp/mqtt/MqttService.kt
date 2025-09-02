package com.tfm.rfidcartapp.mqtt

import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

class MqttService(
    private val externalScope: CoroutineScope
) {
    private val TAG = "MqttService"

    private val client: Mqtt3AsyncClient = MqttClient.builder()
        .useMqttVersion3()
        .serverHost("broker.hivemq.com")
        .serverPort(1883)
        .identifier("AndroidClient-" + System.currentTimeMillis())
        .buildAsync()

    private val _incoming = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incoming: SharedFlow<String> = _incoming

    // Conecta al broker MQTT y se suscribe al tópico indicado, emitiendo los mensajes recibidos en _incoming
    fun connectAndSubscribe(topic: String = "r2000/tags") {
        client.connect()
            .whenComplete { _, err ->
                if (err != null) {
                    Log.e(TAG, "MQTT connect failed", err)
                } else {
                    Log.i(TAG, "MQTT connected, subscribing to $topic")
                    client.subscribeWith()
                        .topicFilter(topic)
                        .qos(MqttQos.AT_LEAST_ONCE)
                        .callback { publish ->
                            val payload = publish.payloadAsBytes.toString(StandardCharsets.UTF_8)
                            externalScope.launch(Dispatchers.Default) {
                                _incoming.emit(payload)
                            }
                        }
                        .send()
                        .whenComplete { _, subErr ->
                            if (subErr != null) Log.e(TAG, "MQTT subscribe failed", subErr)
                        }
                }
            }
    }

    // Desconecta el cliente MQTT y cancela la coroutine scope asociada
    fun disconnect() {
        try {
            client.disconnect()
        } catch (_: Throwable) { /* na */ }
        externalScope.cancel() // si se creó scope dedicada para este servicio
    }
}