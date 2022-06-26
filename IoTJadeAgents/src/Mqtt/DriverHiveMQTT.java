package Mqtt;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

public class DriverHiveMQTT {

    final String HOST = "051628c3388941d8b16f28b1727ca6c1.s2.eu.hivemq.cloud";
    final int PORT = 8883;
    final String USER = "Usertest1";
    final String PASS = "Usertest1";
    private AgentMQTT myAgent;

    public DriverHiveMQTT(AgentMQTT myAgent) {
        this.myAgent = myAgent;
    }

    private Mqtt5BlockingClient InitClient(){
        try {
            // create an MQTT client
            final Mqtt5BlockingClient client = MqttClient.builder()
                    .useMqttVersion5()
                    .serverHost(HOST)
                    .serverPort(PORT)
                    .sslWithDefaultConfig()
                    .buildBlocking();

            // connect to HiveMQ Cloud with TLS and username/pw
            client.connectWith()
                    .simpleAuth()
                    .username(USER)
                    .password(UTF_8.encode(PASS))
                    .applySimpleAuth()
                    .send();

            // set a callback that is called when a message is received (using the async API style)
            client.toAsync().publishes(ALL, publish -> {
                String payload = UTF_8.decode(publish.getPayload().get()).toString();
                String topic = publish.getTopic().toString();
                System.out.println("Received message: " + topic + " -> " + payload);
                myAgent.ProcessMessageArrived(topic, payload);
            });

            return client;
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public void SubscribeTopic(String topicName)
    {
        this.SubscribeTopic(topicName, 0);
    }

    public void SubscribeTopic(String topicName, int qos)
    {
        Mqtt5BlockingClient client = InitClient();
        client.subscribeWith()
                .topicFilter(topicName)
                .qos(MqttQos.fromCode(qos))
                .send();
    }

    public void PublishMessage(String topicName, String message)
    {
        this.PublishMessage(topicName, message, 0, false);
    }

    public void PublishMessage(String topicName, String message, int qos)
    {
        this.PublishMessage(topicName, message, qos, false);
    }

    public void PublishMessage(String topicName, String message, int qos, boolean retain)
    {
        Mqtt5BlockingClient client = InitClient();
        client.publishWith()
                .topic(topicName)
                .payload(UTF_8.encode(message))
                .qos(MqttQos.fromCode(qos))
                .retain(retain)
                .send();
    }
}