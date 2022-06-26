#include <ezButton.h>

// Sonar Placements (Ids)
// 1  |  2
// 3  |  4

// defines pins numbers
const int trigPin_TopLeft = 3;
const int trigPin_TopRight = 5;
const int trigPin_BottomLeft = 6;
// const inst trigPin_BottomRight = ...
const int echoPin_TopLeft = 2;
const int echoPin_TopRight = 4;
const int echoPin_BottomLeft = 7;
// const int echoPin_BottomRight = ...

const int maxDistance = 400;
long previousDistance = 0;

unsigned long previousSensorMillis = 0;
const long sensorInterval = 500;
bool sensorState = false;

ezButton button(8);
bool activeState = false;

void setup() {  
  pinMode(trigPin_TopLeft, OUTPUT);
  //pinMode(trigPin_TopRight, OUTPUT);
  //pinMode(trigPin_BottomLeft, OUTPUT);
  //pinMode(trigPin_BottomRight, OUTPUT);
  
  pinMode(echoPin_TopLeft, INPUT);
  //pinMode(echoPin_TopRight, INPUT); 
  //pinMode(echoPin_BottomLeft, INPUT);
  //pinMode(echoPin_BottomRight, INPUT);

  pinMode(11, OUTPUT);

  button.setDebounceTime(100);
  
  Serial.begin(9600);
  Serial.println("Press button to begin");
}

void loop() {
  updateState();  
  if(activeState && sensorState){
    writeDistance('1', trigPin_TopLeft, echoPin_TopLeft);
  //writeDistance('2', trigPin_TopRight, echoPin_TopRight);
  //writeDistance('3', trigPin_BottomLeft, echoPin_BottomLeft);
  // writeDistance('4', trigPin_BottomRight, echoPin_BottomRight);
  }
}

void updateState(){
  unsigned long currentMillis = millis();
  if (currentMillis - previousSensorMillis >= sensorInterval) {
    previousSensorMillis = currentMillis;
    sensorState = true;
  }else {
    sensorState = false;
  }
  
  button.loop();
  if (button.isPressed()) {
    activeState = !activeState;
    if(activeState){
      Serial.println("Changed state to ON");
    }else{
      Serial.println("Changed state to OFF");
      digitalWrite(11, LOW);
    }
  }
}

void writeDistance(char sonarId, int trigPin, int echoPin) {
  // trigger sensor
  digitalWrite(trigPin, LOW);
  delayMicroseconds(5);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  // read duration and calculate distance
  // distance = (traveltime/2) x speed of sound
  long distance = pulseIn(echoPin, HIGH) * 0.0343 / 2;

  // sensor max distance is around 400 cm so above that is error
  if (distance > maxDistance || distance < 1){
    Serial.println("Error reading distance.");
    return;
  }

  // if distance is the same as last reading return else update and continue
  if (distance == previousDistance){
    return;
  }
  previousDistance = distance;

  char printValue[200];
  sprintf(printValue, "Sonar %c read distance %ld", sonarId, distance);
  Serial.println(printValue);

  // light up led depending on distance
  // intensity = -0.6375 * distance + 255
  analogWrite(11, -0.6375 * distance + 255);
}
