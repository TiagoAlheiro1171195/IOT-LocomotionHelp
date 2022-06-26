#include <WiFi.h>
#include <HTTPClient.h>
#include <PubSubClient.h>
  
const char* ssid = "Vodafone-F043B2";
const char* password =  "TF695f32eH";
const char* mqttServer = "test.mosquitto.org";
const int mqttPort = 1883;
const char* mqttUser = "";
const char* mqttPassword = "";
const char* mqttTopic = "/sensor/distance/1";

WiFiClient wifiClient;
PubSubClient client(wifiClient);
  
void setup() {
  
  Serial.begin(115200);
  delay(4000);   //Delay needed before calling the WiFi.begin
  
  WiFi.begin(ssid, password); 
  
  while (WiFi.status() != WL_CONNECTED) { //Check for the connection
    delay(1000);
    Serial.println("Connecting to WiFi..");
  }
  
  Serial.println("Connected to the WiFi network");

  client.setServer(mqtt_server, mqttPort);
  //client.setCallback(callback);
  
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived on topic: ");
  Serial.println(topic);
 
  String message;
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
    message += (char)payload[i];
  }
  Serial.print(". Message: ");
  Serial.println(message);
  Serial.write(payload, length);
  Serial.println();
}

void connectMqtt() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    if (client.connect("ESP32ClientPub_1")) {
      Serial.println("connected");
      //client.subscribe(mqttTopic);
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}

  
void loop() {
  if (!client.connected()) {
    connectMqtt();
  }
  client.loop();

  Serial.println("Sending data");
  if (!client.connected()) {
 connectMQTT();
 }
 client.loop();
  
  client.publish(mqttTopic, "12");
  
  delay(5000);  //Send a request every 10 seconds
  
}
