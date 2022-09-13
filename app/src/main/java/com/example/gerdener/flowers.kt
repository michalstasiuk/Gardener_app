package com.example.gerdener

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

import org.eclipse.paho.client.mqttv3.*
import kotlinx.serialization.json.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@Serializable
class Flower(var hydration_level: Int)

@Serializable
class HydrationData(var flowers: Array<Flower>)

/**
 * A simple [Fragment] subclass.
 * Use the [flowers.newInstance] factory method to
 * create an instance of this fragment.
 */
class flowers : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mqttClient : MQTTClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_flowers, container, false)
    }

    fun subscribe_to_topic(topic: String){
        if (mqttClient.isConnected()) {
            mqttClient.subscribe(topic,
                1,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        val msg = "Subscribed to: $topic"
                        Log.d(this.javaClass.name, msg)

                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(this.javaClass.name, "Failed to subscribe: $topic")
                    }
                })
        } else {
            Log.d(this.javaClass.name, "Impossible to subscribe, no server connected")
        }
    }

    fun update_flowers(msg: String, view: View){
        val data = msg.split(',').toTypedArray()
        view.findViewById<TextView>(R.id.value_flower_table_1).setText(data[0])
        view.findViewById<TextView>(R.id.value_flower_table_2).setText(data[1])
        view.findViewById<TextView>(R.id.value_flower_table_3).setText(data[2])
        view.findViewById<TextView>(R.id.value_flower_table_4).setText(data[3])
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.connect_button).setOnClickListener {
            val hostIP:String = view.findViewById<EditText>(R.id.host_IP_textbox).text.toString()
            val hostPort = view.findViewById<EditText>(R.id.host_port_textbox).text.toString()



            val serverURL = "tcp://$hostIP:$hostPort"
            //Toast.makeText(requireActivity(), "Connecting to $serverURL ...", Toast.LENGTH_SHORT).show()


            mqttClient = MQTTClient(context, serverURL, MQTT_CLIENT_ID)

            mqttClient.connect( MQTT_USERNAME,
                MQTT_PWD,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(this.javaClass.name, "Connection success")

                        Toast.makeText(requireActivity(), "MQTT Connection success", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(this.javaClass.name, "Connection failure: ${exception.toString()}")

                        Toast.makeText(requireActivity(), "MQTT Connection fails: ${exception.toString()}", Toast.LENGTH_SHORT).show()

                    }
                },
                object : MqttCallback {
                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        val msg = "Receive message: ${message.toString()} from topic: $topic"
                        update_flowers(message.toString(), view)
                        Log.d(this.javaClass.name, msg)
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.d(this.javaClass.name, "Connection lost ${cause.toString()}")
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        Log.d(this.javaClass.name, "Delivery complete")
                    }
                })

//            Timer("Schedule a subsribtion to some topic", false).schedule(SUBSCRIBE_DELAY) {
//                subscribe_to_topic(MQTT_FLOWER_STAND_TOPIC)
//            }

            Handler().postDelayed({
                subscribe_to_topic(MQTT_FLOWER_STAND_TOPIC)
            }, SUBSCRIBE_DELAY.toLong())

        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment flowers.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            flowers().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}