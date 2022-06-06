package com.example.gerdener

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.eclipse.paho.client.mqttv3.*

class MainActivity : AppCompatActivity() {
    private lateinit var mqttClient : MQTTClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val connectButton = findViewById<Button>(R.id.connect_button)

        connectButton.setOnClickListener{
            val hostIP:String = findViewById<EditText>(R.id.host_IP_textbox).text.toString()
            val hostPort = findViewById<EditText>(R.id.host_port_textbox).text.toString()


            val serverURL = "tcp://$hostIP:$hostPort"
            Toast.makeText(this, "Connecting to $serverURL ...", Toast.LENGTH_SHORT).show()


            mqttClient = MQTTClient(this, serverURL, MQTT_CLIENT_ID)

            mqttClient.connect( MQTT_USERNAME,
                MQTT_PWD,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(this.javaClass.name, "Connection success")

                        //Toast.makeText(this, "MQTT Connection success", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(this.javaClass.name, "Connection failure: ${exception.toString()}")

                        //Toast.makeText(this, "MQTT Connection fails: ${exception.toString()}", Toast.LENGTH_SHORT).show()

                    }
                },
                object : MqttCallback {
                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        val msg = "Receive message: ${message.toString()} from topic: $topic"
                        Log.d(this.javaClass.name, msg)

                        //Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.d(this.javaClass.name, "Connection lost ${cause.toString()}")
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        Log.d(this.javaClass.name, "Delivery complete")
                    }
                })
        }
    }


}