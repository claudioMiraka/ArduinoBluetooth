int blue_out = 2;
int red_out = 13;
int motor_forward_pin = 7;
int motor_backwards_pin = 12;
char myChar;

bool collision = false;

const int trigPin = 9;
const int echoPin = 10;

long duration;
int distance;

int debug = 0;

String readString;
void setup() {
  pinMode(red_out, OUTPUT);
  pinMode(blue_out, OUTPUT);
  pinMode(trigPin, OUTPUT);
  pinMode(motor_forward_pin, OUTPUT);
  pinMode(motor_backwards_pin, OUTPUT);
  pinMode(echoPin, INPUT);
  Serial.begin(9600);
}

void loop() {
  while (Serial.available()) {
    delay(2);
    char c = Serial.read();
    readString += c;
  }

  if (readString.length() > 0) {
    Serial.print(readString);
    if (readString == "CONNECTED") {
      digitalWrite(blue_out, HIGH);
    }
    if (readString == "DISCONNECTED") {
      digitalWrite(blue_out, LOW);
    }
    if (!collision) {
      if (readString == "FORWARD") {
        forward();
      }
      if (readString == "BACKWARD") {
        backward();
      }
    }
    if (readString == "STOP") {
      stopMachine();
    }
    readString = "";
  }

  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  duration = pulseIn(echoPin, HIGH);
  distance = duration * 0.017;
  if (distance <= 5) {
    collision = true;
    backward();
    Serial.write("1");
    delay(5);
    digitalWrite(red_out, HIGH);
  }
  if (distance > 7) {
    if (collision) {
      stopMachine();
    }
    Serial.write("0");
    collision = false;
    digitalWrite(red_out, LOW);
  }
  if (debug == 1) {
    Serial.println(distance);
  }
}

void forward() {
  digitalWrite(motor_forward_pin, HIGH);
  digitalWrite(motor_backwards_pin, LOW);
}
void backward() {
  digitalWrite(motor_backwards_pin, HIGH);
  digitalWrite(motor_forward_pin, LOW);
}
void stopMachine() {
  digitalWrite(motor_backwards_pin, LOW);
  digitalWrite(motor_forward_pin, LOW);
}
