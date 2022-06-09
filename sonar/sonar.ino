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

void setup() {
  pinMode(3, OUTPUT);
  pinMode(5, OUTPUT);
  pinMode(6, OUTPUT);
  pinMode(2, INPUT);
  pinMode(4, INPUT); 
  pinMode(7, INPUT); 
  Serial.begin(9600);
}

void loop() {
  writeDistance(trigPin_TopLeft, echoPin_TopLeft, '1', 0);
  writeDistance(trigPin_TopRight, echoPin_TopRight, '2', 5);
  writeDistance(trigPin_BottomLeft, echoPin_BottomLeft, '3', 10);
  // writeDistance(trigPin_BottomRight, echoPin_BottomRight, '4', 10);
}

void writeDistance(int trigPin, int echoPin, char sonarId, int tone) {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(tone);

  digitalWrite(trigPin, HIGH);
  delayMicroseconds(2000);
  digitalWrite(trigPin, LOW);

  Serial.print("Distance Sonar " + sonarId);
  Serial.println(pulseIn(echoPin, HIGH) * 0.034 / 2);
}
