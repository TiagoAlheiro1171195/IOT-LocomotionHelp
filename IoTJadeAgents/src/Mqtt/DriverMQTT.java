package Mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;;

import java.nio.charset.StandardCharsets;

public class DriverMQTT {

    final String HOST = "ssl://051628c3388941d8b16f28b1727ca6c1.s2.eu.hivemq.cloud";
    final int PORT = 8883;
    final String USER = "Usertest1";
    final String PASS = "Usertest1";
    private AgentMQTT myAgent;
    private MqttClient client;

    public DriverMQTT(AgentMQTT myAgent) {
        this.myAgent = myAgent;
        InitClient();
    }

    private void InitClient(){
        try {
            MqttClient client = new MqttClient(HOST, MqttClient.generateClientId(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USER);
            options.setPassword(PASS.toCharArray());
            client.connect(options);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connection on Agent " + myAgent.GetAgentName() + " was lost!");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    System.out.println(topic + " : " + payload);
                    myAgent.ProcessMessageArrived(topic, payload);
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("Delivery Complete");
                }
            });

            this.client = client;
        } catch (Exception ex){
            client = null;
            ex.printStackTrace();
        }
    }

    public void SubscribeTopic(String topicName, int qos)
    {
        try {
            client.subscribe(topicName, qos);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void PublishMessage(String topicName, String message)
    {
        try {
            MqttMessage mqt = new MqttMessage();
            mqt.setPayload(message.getBytes(StandardCharsets.UTF_8));

            client.publish(topicName, mqt);

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}