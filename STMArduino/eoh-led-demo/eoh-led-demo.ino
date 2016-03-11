const int ledPin = 13;

void setup() {
  // put your setup code here, to run once:
  pinMode(ledPin, OUTPUT);
  Serial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  if(Serial.available()) {
    String incoming = Serial.readString();    //Wait for the command to light the led to come in
    if(incoming == "Start") {
      for(int i = 0; i < 5; i++) { //Turn the led on and off 5 times
        digitalWrite(ledPin, HIGH);
        delay(100);
        digitalWrite(ledPin, LOW);
        delay(100);
      }
      Serial.println("Done");
    }
  }
  delay(200);
}
