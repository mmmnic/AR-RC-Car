/* ------------- INCLUDE ------------- */
#include <SoftwareSerial.h>

/* ------------- DEFINE ------------- */
#define AIN2        D0
#define PWMA        D1
#define AIN1        D2
#define BIN1        D3
#define BIN2        D4
#define PWMB        D5
#define BT_TX       D6
#define BT_RX       D7
#define angleDelay  23
/* ------------- GLOBAL PARA ------------- */
//SoftwareSerial bluetooth(BT_TX, BT_RX);
SoftwareSerial bluetooth(BT_RX, BT_TX);

void setup() {
  pinMode(AIN2, OUTPUT);
  pinMode(PWMA, OUTPUT);
  pinMode(AIN1, OUTPUT);
  pinMode(BIN1, OUTPUT);
  pinMode(BIN2, OUTPUT);
  pinMode(PWMB, OUTPUT);
  pinMode(BT_RX, OUTPUT);
  pinMode(BT_TX, INPUT);

  // Serial setup
  Serial.begin(9600);
  bluetooth.begin(9600);
  Serial.println("Config done");

  // Wait for bluetooth
  while (!bluetooth);
  Serial.println("Finished");

  // Setup motor direction
  setDirection(1,1);
}

void loop() {
  Serial.println(bluetooth.read()); //debug
  delay(100);
}

void turn(float TurnAngle)
{
  /* NOTE
   * Set 1 motor on and delay 500ms = turn 22.5 degree
   */
  TurnAngle /= angleDelay;
  if (TurnAngle >= 0)
  {
     setMotor(255,0);
     delay(500*TurnAngle);
  }
  else
  {
    TurnAngle = -TurnAngle;
    setMotor(0,263);
    delay(500*TurnAngle);
  }
  setMotor(0,0);
}

void runStraight(int delayTime)
{
  setMotor(255, 255);
  delay(delayTime);
  setMotor(0, 0);
}

void setMotor(int MTA, int MTB)
{
  if (MTB>8)
    MTB -= 7;
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
