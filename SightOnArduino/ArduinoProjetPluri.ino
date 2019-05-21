byte led = 13;
void setup() {
  Serial.begin(9600);
  pinMode(led,OUTPUT);
  digitalWrite(led,LOW);
}

void loop() {}

void serialEvent(){
    byte reponse = Serial.read();
    if(reponse == 1)
      digitalWrite(led,HIGH);
    else if(reponse == 0)
      digitalWrite(led,LOW);
}
