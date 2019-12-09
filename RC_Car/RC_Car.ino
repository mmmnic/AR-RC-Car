/* ------------- INCLUDE ------------- */
#include "RC_Car.h"


void setup() {
  long Time_Wifi;
  
  pinMode(AIN2, OUTPUT);
  pinMode(PWMA, OUTPUT);
  pinMode(AIN1, OUTPUT);
  pinMode(BIN1, OUTPUT);
  pinMode(BIN2, OUTPUT);
  pinMode(PWMB, OUTPUT);
  pinMode(BT_RX, OUTPUT);
  pinMode(BT_TX, INPUT);

  // Serial setup
  // Open serial for debugging
  Serial.begin(9600);
  
  /* ----- START BLUETOOTH ----- */
  bluetooth.begin(9600);
  // Wait for bluetooth
  while (!bluetooth);
  Serial.println("Config bluetooth done");
  
  /* ----- START WIFI ----- */
  Serial.println("Connecting Wifi");
  Serial.print("Connecting to: ");
  Serial.println(ssid);
  Time_Wifi=millis();
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
    if(millis()-Time_Wifi>30000){
//      ESP.restart();
      break;
    }
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  /* ----- START SOCKET ----- */
  Socket.on("DistanceDown",listenDistance);
  Socket.on("AngleDown",listenAngle);
  Socket.on("DirectionDown",listenDirection);
  Socket.begin(Host_Socket, Port_Socket, "/socket.io/?transport=websocket");

  
  // Setup motor direction
  setDirection(1,1);

  Serial.println("----------------- ALL DONE -----------------");
}

void loop() {
  Socket.loop();
  if (isReceived == 1)
  {
    isReceived = 0;
    for (int i = 0; i< amount; i++)
    {
       Serial.print("---------- ");
       Serial.print(i);
       Serial.println(" ----------");
       Serial.print("Angle: ");
       Serial.println(parsed_Angle[i]);
       Serial.print("Direction: ");
       Serial.println(parsed_Direction[i]);
       Serial.print("Distance: ");
       Serial.println(parsed_Distance[i]);
       turn(parsed_Angle[i], parsed_Direction[i]);
       runStraight(parsed_Distance[i]);
       parsed_Angle[i] = 0;
       parsed_Direction[i] = 0;
       parsed_Distance[i] = 0;
       Socket.loop();
       if (isReceived == 1)
          break;
    }
  }
}

int *parseStringToInt(char *data)
{
  int *arrayInt = new int[10];
  int count = 0;
  String tempStringInt = "";
  for (int i = 0; i < strlen(data); i++)
  {
    if (data[i] == ';')
      break;
    if (data[i] == ',')
    {
      arrayInt[count] = tempStringInt.toInt();
      count++;
      tempStringInt = "";
    }
    else
    {
      tempStringInt += data[i];
    }
    
    // if ; is end string
    if (data[i] == ';')
      break;
  }
  return arrayInt;
}

float *parseStringToFloat(char *data)
{
  float *arrayFloat = new float[10];
  int count = 0;
  String tempStringInt = "";
  for (int i = 0; i < strlen(data); i++)
  {
    if (data[i] == ',')
    {
      arrayFloat[count] = tempStringInt.toFloat();
      count++;
      tempStringInt = "";
    }
    else
    {
      tempStringInt += data[i];
    }
    // if ; is end string
    if (data[i] == ';')
      break;
  }
  amount = count;
  return arrayFloat;
}


void turn(float TurnAngle, int Direction)
{
  /* NOTE
   * Set 1 motor on and delay 600ms = turn 90 degree
   * 1 degree ~ 6.6ms
   * Direction = 1 is left, 0 is right
   */
  if (Direction == 1)
  {
     setMotor(0,255);
     delay(TimePerAg*TurnAngle);
  }
  else
  {
    setMotor(250,0);
    delay(TimePerAg*TurnAngle);
  }
  setMotor(0,0);
  delay(100);
}

void runStraight(float distance)
{
  // convet to centimeter
  distance *= 100;
  // start motor
  setMotor(248, 255);
  delay(distance/Speed);
  setMotor(0,0);
  delay(100);
}

void setMotor(int MTA, int MTB)
{
  analogWrite(PWMA, MTA);
  analogWrite(PWMB, MTB);
}
// set direction for motor, 0 is backward, !0 is forward
void setDirection(int MT1, int MT2)
{
  if (MT1 == 1)
  {
    digitalWrite(AIN1, HIGH);
    digitalWrite(AIN2, LOW);
  }
  else
  {
    digitalWrite(AIN1, LOW);
    digitalWrite(AIN2, HIGH);
  }
  if (MT2 == 1)
  {
    digitalWrite(BIN1, HIGH);
    digitalWrite(BIN2, LOW);
  }
  else
  {
    digitalWrite(BIN1, LOW);
    digitalWrite(BIN2, HIGH);
  }
}

void listenDistance(const char * payload, size_t length){
  if(!String(payload)==NULL){
    char tempString[] = "";
    strcpy(tempString, payload);
    parsed_Distance = parseStringToFloat(tempString);
  }
}

void listenAngle(const char * payload, size_t length){
  if(!String(payload)==NULL){
    char tempString[] = "";
    strcpy(tempString, payload);
    parsed_Angle = parseStringToInt(tempString);
    
  }
}

void listenDirection(const char * payload, size_t length){
  if(!String(payload)==NULL){
    char tempString[] = "";
    strcpy(tempString, payload);
    parsed_Direction = parseStringToInt(tempString);
    isReceived = 1;
  }
}
