/* ------------- INCLUDE ------------- */
#include <SocketIoClient.h>
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
#define TimePerAg   6.5
#define Speed       0.032
/* ------------- GLOBAL PARA ------------- */
SoftwareSerial bluetooth(BT_RX, BT_TX);
const char* ssid     = "MQ_Network";
const char* password = "1denmuoi1";
//const char* ssid     = "TP-LINK_F608";
//const char* password = "quenpassroi";
const char* Host_Socket = "ar-rc-car.herokuapp.com"; //<-- gán link tại đây
SocketIoClient Socket;

unsigned int Port_Socket=80;
float *parsed_Distance;
int   *parsed_Angle;
int   *parsed_Direction;
int   amount = 0;
int volatile   isReceived = 0;

/* ------------- FUNCTIONS ------------- */
void listenDistance(const char * payload, size_t length);
void listenAngle(const char * payload,  size_t length);
void listenDirection(const char * payload, size_t length);
void turn(float TurnAngle);
void runStraight(int delayTime);
void setMotor(int MTA, int MTB);
void setDirection(int MT1, int MT2);
