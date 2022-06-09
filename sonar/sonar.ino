// defines pins numbers
const int trigPin1 = 3;
const int trigPin2 = 5;
const int trigPin3 = 6;
const int echoPin1 = 2;
const int echoPin2 = 4;
const int echoPin3 = 7;

void setup() {
  pinMode(3, OUTPUT);
  pinMode(5, OUTPUT);
  pinMode(6, OUTPUT); // Sets the trigPin as an Output
  pinMode(2, INPUT); // Sets the echoPin as an Input
  pinMode(4, INPUT); 
  pinMode(7, INPUT); 
  Serial.begin(9600); // Starts the serial communication
}

void loop() {
  writeDistance(trigPin1, echoPin1, '1', 0);
  writeDistance(trigPin2, echoPin2, '2', 5);
  writeDistance(trigPin3, echoPin3, '3', 10);
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
