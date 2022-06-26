#include <WiFi.h>
#include <HTTPClient.h>
const int trigPin = 15;
const int echoPin = 13;

//define sound speed in cm/uS
#define SOUND_SPEED 0.034

const char* ssid = "Vodafone-F043B2";
const char* password = "TF695f32eH";
const char* server = "http://ptsv2.com/t/mpsfn-1655142943/post";

long duration;
long distance;

void setup() {
  Serial.begin(115200); // Starts the serial communication
  Serial.setDebugOutput(true);
  Serial.println();

  WiFi.begin(ssid, password);
  Serial.println("Connecting to wifi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.print("WiFi connected");
  Serial.println(WiFi.localIP());
  
  pinMode(trigPin, OUTPUT); // Sets the trigPin as an Output
  pinMode(echoPin, INPUT); // Sets the echoPin as an Input
}

void loop() {
  // Clears the trigPin
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  // Sets the trigPin on HIGH state for 10 micro seconds
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  
  // Reads the echoPin, returns the sound wave travel time in microseconds
  duration = pulseIn(echoPin, HIGH);
  
  // Calculate the distance
  distance = duration * SOUND_SPEED/2;
  
  // Prints the distance in the Serial Monitor
  Serial.print("Distance (cm): ");
  Serial.println(distance);

  if(WiFi.status()== WL_CONNECTED){
      HTTPClient http;

      // Your Domain name with URL path or IP address with path
      http.begin(server);

      // Specify content-type header
      http.addHeader("Content-Type", "application/json");
      // Data to send with HTTP POST
      char httpRequestData[256];
      sprintf(httpRequestData, "{\"distance\":\"%ld\"}", distance);     
      Serial.print(httpRequestData); 
      // Send HTTP POST request
      int httpResponseCode = http.POST(httpRequestData);

      Serial.print("HTTP Response code: ");
      Serial.println(httpResponseCode);

      if(httpResponseCode>0){
        Serial.print("HTTP Response: ");
        Serial.println(http.getString());
      }
      
      // Free resources
      http.end();
    }
    else {
      Serial.println("WiFi Disconnected");
    }
  
  delay(5000);
}
