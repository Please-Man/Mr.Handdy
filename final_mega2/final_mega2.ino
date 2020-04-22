String json_str="";

#include <Servo.h> //서보 라이브러리
#define relayPin 7 // 물 모터
#define led1 47
#define led2 48
#define led3 49

static int Base_ang; // 베이스 각도
static int Joint1_ang; // 관절 1 각도
static int Joint2_ang; // 관절 2 각도
static int Joint3_ang; // 관절 3 각도
static int End_ang; // 앤드이펙터 각도

String ssa = "";
static int count = 0;
char a0;

Servo Base; // 베이스 
Servo Joint1_1; // 1번 모터 L
Servo Joint1_2; // 1번 모터 R
Servo Joint2; // 2번 모터
Servo Joint3; // 3번 모터
Servo End; // 4번모터(엔드이펙터)

#define BT_PACKET_START 0xf5
#define BT_PACKET_LEN   4
#define MTn_A    0x1         // 
#define MTn_B    0x2         // 
#define MTR_C    0x4         // 
#define MTR_CA    0x8         // 
#define MTR_2F    0x10
#define MTR_2B    0x20
#define MTR_FS    0x40
#define MTR_3F    0x100
#define MTR_3B    0x200
#define MTR_4F    0x400
#define MTR_4B    0x800
#define MTR_5F    0x1000
#define MTR_5B    0x2000
#define MTR_SS    0x4000

//- 블루투스 모듈 통신 관련 변수
unsigned char btData[BT_PACKET_LEN];
unsigned char btPtr = 0;
unsigned int btCommand;
byte buffer[1024];
int bufferPosition; 

//티칭용 변수들
static int teaching = false;
static int route[100][5]; 
static int i = 0; // 움직인 횟수(버튼 누른 횟수)
static int act_flag = false;

void setup() {
  Serial.begin(9600);
  Serial1.begin(57600);
  pinMode(13, OUTPUT);
  Serial3.begin(9600);
  pinMode(led1,OUTPUT);
  pinMode(led2,OUTPUT);
  pinMode(led3,OUTPUT);
  pinMode(30,OUTPUT);
  
  pinMode(relayPin,OUTPUT);
  delay(3000);
  Base.attach(22); // 베이스 22번핀 [갈색] {값이 증가 = 오른쪽}
  delay(1500);
  Base.write(10);
  delay(1500);
  Joint3.attach(26); // 조인트 3 26번핀 [파랑]
  delay(1500);
  Joint3.write(180);
  delay(1500);
  Joint1_1.attach(23); // 조인트 1_1 23번핀 L [각도비례] [초록]
  Joint1_2.attach(24); // 조인트 1_2 24번핀 R [180-L] [노랑]
  Joint1_1.write(130);
  Joint1_2.write(180-130);
  delay(1500);
  Joint2.attach(25); // 조인트 2 25번핀 [각도 비례] [주황]
  delay(1000);
  End.attach(27); // 엔드이펙터 27번핀 [각도 반비례] [흰색]
  delay(1500);
  initialPos();
}

boolean btReadPacket(void) {
  unsigned char rdata;
//   Serial.println("[btReadPacket] Serial1.available() ");
  while (Serial1.available()) {
    rdata = Serial1.read();
//     Serial.println("[btReadPacket] rdata  : "+ rdata);
    if (rdata == BT_PACKET_START) {
      btPtr = 0;
    }
    btData[btPtr] = rdata;
    if (++btPtr == BT_PACKET_LEN) {
      btPtr = 0;
      if (((btData[1] + btData[2]) & 0x7f) == btData[3]) {
        btCommand = btData[1] + (word(btData[2]) << 8); //
        return true;
      }
    }
  }
  return false;
}

void mtrDrive() {
  static int swp=false;
  int swc;
  
  if (btCommand & MTn_A){
    int pos = Base_ang;
    if(pos<180){
      pos++;
      Base.write(pos);
    }
    else {
      pos=180;
    }  
    initialPos();
    Serial.print("Base_ang : ");
    Serial.println(Base_ang);
  }
  
  if(btCommand & MTn_B){
    int pos = Base_ang;
    if(pos>0){
      pos--;
      Base.write(pos);
    }
    else {
      pos=0;
    }  
    initialPos();
    Serial.print("Base_ang : ");
    Serial.println(Base_ang);
  }
  
  if(btCommand & MTR_C) {
    int pos = Joint1_ang;
    if(pos<180){
      pos++;
      Joint1_1.write(pos);
      Joint1_2.write(180-pos);
    }
    else {
      pos=180;
    }  
    initialPos();
    Serial.print("Joint1_ang : ");
    Serial.println(Joint1_ang);
  }

  if(btCommand & MTR_CA) {
    int pos = Joint1_ang;
    if(pos>0){
      pos--;
      Joint1_1.write(pos);
      Joint1_2.write(180-pos);
    }
    else {
      pos=180;
    }  
    initialPos();
    Serial.print("Joint1_ang : ");
    Serial.println(Joint1_ang);
  }

  if (btCommand & MTR_2F){
    int pos = Joint2_ang;
    if(pos<180){
      pos++;
      Joint2.write(pos);
    }
    else {
      pos=180;
    }  
    initialPos();
    Serial.print("Joint2_ang : ");
    Serial.println(Joint2_ang);
  }
  
  if(btCommand & MTR_2B){
    int pos = Joint2_ang;
    if(pos>0){
      pos--;
      Joint2.write(pos);
    }
    else {
      pos=0;
    }  
    initialPos();
    Serial.print("Joint2_ang : ");
    Serial.println(Joint2_ang);
  }

  if (btCommand & MTR_FS){
    int pos = Joint3_ang;
    if(pos<180){
      pos++;
      Joint3.write(pos);
    }
    else {
      pos=180;
    }  
    initialPos();
    Serial.print("Joint3_ang : ");
    Serial.println(Joint3_ang);
  }
  
  if(btCommand & MTR_3F){
    int pos = Joint3_ang;
    if(pos>0){
      pos--;
      Joint3.write(pos);
    }
    else {
      pos=0;
    }  
    initialPos();
    Serial.print("Joint3_ang : ");
    Serial.println(Joint3_ang);
  }

  if (btCommand & MTR_3B){
    int pos = End_ang;
    if(pos<180){
      pos++;
      End.write(pos);
    }
    else {
      pos=180;
    }  
    initialPos();
    Serial.print("End_ang : ");
    Serial.println(End_ang);
  }
  
  if(btCommand & MTR_4F){
    int pos = End_ang;
    if(pos>0){
      pos--;
      End.write(pos);
    }
    else {
      pos=0;
    }  
    initialPos();
    Serial.print("End_ang : ");
    Serial.println(End_ang);
  }

  if(btCommand & MTR_4B){ //티칭 시작
    swc=true;
    teaching = true;
    if(swp == false && swc == true){ //티칭 시작시
      if(teaching == true){
        route[i][0] = Base_ang;
        route[i][1] = Joint1_ang;
        route[i][2] = Joint2_ang;
        route[i][3] = Joint2_ang;
        route[i][4] = End_ang;
        i++;
        Serial.print("i : ");
        Serial.println(i);
      } 
      act_flag = false; 
    }
  }
  else {
    swc=false;
  }

  if(btCommand & MTR_5F){ //티칭 종료(저장)
    teaching = false;
  }
  
  if(btCommand & MTR_5B){ //티칭기록 삭제
    for(int k=0;k<i;k++){
      route[k][0] = 90;
      route[k][1] = 90;
      route[k][2] = 90;
      route[k][3] = 90;
      route[k][4] = 90;
    }
    i=0;
  }
  else {
    swc=false;
  }
  
  if(btCommand & MTR_SS){ //티칭한 동작 시작
    if(act_flag == false){
      teacher();
    }
    act_flag = true;
  }
  
  swp=swc;
  Serial.print(swp);
}

void teacher(){ //티칭동작
  for(int k=0;k<i;k++){
    Base.write(route[k][0]);
    Joint1_1.write(route[k][1]);
    Joint1_2.write(180-route[k][1]);
    Joint2.write(route[k][2]);
    Joint3.write(route[k][3]);
    End.write(route[k][4]);
    delay(1000);
  }
}
static char pre_a0 = 'a';

void uno(){
  int flag = false;
  if (Serial3.available()) {
    char c = Serial3.read(); // 의심 포인트 1 char []로 바로 넣어줄 방법을 생각하는것도 ...
    json_str += c;        
  }
  String s = json_str;
  char a3 = s.charAt(s.length()-1); //에소
  char a2 = s.charAt(s.length()-2); //아메핫
  char a1 = s.charAt(s.length()-3);
  char c_a0 = s.charAt(s.length()-4); //아메핫
  a0 = s.charAt(s.length()-5);
 
  if(c_a0 != pre_a0){
    flag = true;
  }
  if(a0 == '9' & flag == true){
    Serial.print(a1);
    Serial.print(a2);
    Serial.print(a3);
    ssa += a1;
    ssa += a2;
    ssa += a3;
    if (count == 0 && ssa != "") {
      initialPos();
      cupHolder();
      delay(1000);
      cupGrap();
      delay(1000);
      coffeeMachine();
      delay(1000);
      coffee();
      delay(1000);
      coffeeBack();
      delay(1000);
      firstPos();
      delay(1000);
      finalPos();  
      ssa = "";    
    }
  if(count == 2 && ssa != "") {
      initialPos();
      cupHolder();
      delay(1000);
      cupGrap();
      delay(1000);
      coffeeMachine();
      delay(1000);
      coffee();
      delay(1000);
      coffeeBack();
      delay(1000);
      thirdPos();
      delay(1000);
      finalPos();
      ssa = "";
      count=0;
    } 
  }
  
  pre_a0 = c_a0;
}

// -------------------------------------------------------------------------

void baseSpeed(int prevAng, int currAng) { //베이스 모터 속도조절 [prvAng 이전 각도 currAng 움직일 각도]
  if(prevAng < currAng) { 
    for(int i = prevAng; i <= currAng; i++) {
      Base.write(i);
      delay(50); //값이 커질수록 느려짐
    }
  }

  else {
    for(int i = prevAng; i >= currAng; i--) {
      Base.write(i);
      delay(50);
    }
  }
  Base_ang = Base.read();
}

void joint1Speed(int prevAng, int currAng) { //1번 관절 속도조절
  if(prevAng < currAng) {
    for(int i = prevAng; i <= currAng; i++) {
      Joint1_1.write(i);
      Joint1_2.write(180-i);
      delay(40);
    }
  }

  else {
    for(int i = prevAng; i >= currAng; i--) {
      Joint1_1.write(i);
      Joint1_2.write(180-i);
      delay(40);
    }
  }
  Joint1_ang = Joint1_1.read();
}

void joint2Speed(int prevAng, int currAng) { //2번 관절 속도조절
  if(prevAng < currAng) {
    for(int i = prevAng; i <= currAng; i++) {
      Joint2.write(i);
      delay(40);
    }
  }

  else {
    for(int i = prevAng; i >= currAng; i--) {
      Joint2.write(i);
      delay(40);
    }
  }
  Joint2_ang = Joint2.read();
}

void joint3Speed(int prevAng, int currAng) { // 3번관절 속도조절
  if(prevAng < currAng) {
    for(int i = prevAng; i <= currAng; i++) {
      Joint3.write(i);
      delay(40);
    }
  }

  else {
    for(int i = prevAng; i >= currAng; i--) {
      Joint3.write(i);
      delay(40);
    }
  }
  Joint3_ang = Joint3.read();
}

void endSpeed(int prevAng, int currAng) { //엔드이펙터 속도조절
  if(prevAng < currAng) {
    for(int i = prevAng; i <= currAng; i++) {
      End.write(i);
      delay(30);
    }
  }

  else {
    for(int i = prevAng; i >= currAng; i--) {
      End.write(i);
      delay(50);
    }
  }
  End_ang = End.read();
}

void initialPos() { //초기위치 값 각도지정
  Base_ang = Base.read();
  Joint1_ang = Joint1_1.read();
  Joint2_ang = Joint2.read();
  Joint3_ang = Joint3.read();
  End_ang = End.read();
}

void finalPos() { //커피배달 후 마지막 대기위치
  endSpeed(End_ang,50);
  delay(500);
  joint3Speed(Joint3_ang,50);
  delay(500);
  joint1Speed(Joint1_ang,130);
  delay(500);
  joint2Speed(Joint2_ang,95);
  delay(500);
  baseSpeed(Base_ang,10);
}

void cupHolder() { // 컵홀더 위치
  endSpeed(End_ang,50);
  delay(500);
  joint3Speed(Joint3_ang,150);
  delay(500);
  baseSpeed(Base_ang, 39);
  delay(500);
  joint2Speed(Joint2_ang, 75);
  delay(500);
  joint1Speed(Joint1_ang, 100);
  delay(500);
  joint2Speed(Joint2_ang,90);
  delay(500);
  joint3Speed(Joint3_ang,140);
  delay(500);
  endSpeed(End_ang, 115);
}

void cupGrap() { // 컵빼기
  joint3Speed(Joint3_ang,125);
  delay(500);
  joint3Speed(Joint3_ang,155);
  delay(500);
  joint3Speed(Joint3_ang,140);
  delay(500);
  joint2Speed(Joint2_ang, 60);
  delay(500);
  joint2Speed(Joint2_ang, 80);
  delay(500);
  joint2Speed(Joint2_ang, 70);
  delay(500);
  joint1Speed(Joint1_ang, 90);
  delay(500);
  joint1Speed(Joint1_ang, 100);
  delay(500);
  joint1Speed(Joint1_ang, 90);
}

void coffeeMachine() { // 커피머신 위치
  for(int i = 1; i<= 30; i++) {
    Joint2.write(Joint2_ang-i);
    Joint1_1.write(Joint1_ang+i*1.3);
    Joint1_2.write(180-(Joint1_ang+i*1.3));
    delay(100);
  }
  Joint1_ang = Joint1_1.read();
  Joint2_ang = Joint2.read();
  joint2Speed(Joint2_ang, 62);
  delay(500);
  joint1Speed(Joint1_ang,135);
  delay(1000);
  baseSpeed(Base_ang, 83);
  delay(500);
  
  for(int i ; i <=14 ; i++) { // 커피 머신에 컵넣기
    if(i<=11) {
      Joint2.write(Joint2_ang+(i*1.8));
      Joint1_1.write(Joint1_ang-(i*2));
      Joint1_2.write(180-(Joint1_ang-(i*2)));
      delay(100);
    }
    else {
      Joint1_1.write(Joint1_ang-(i*2));
      Joint1_2.write(180-(Joint1_ang-(i*2)));
      delay(100);
    }
  }
  Joint1_ang = Joint1_1.read();
  Joint2_ang = Joint2.read();
}


void coffeeBack() { // 커피머신에서 컵 뒤로 빼기
  for(int i ; i <=24 ; i++) {
    Joint1_1.write(Joint1_ang+(i*2));
    Joint1_2.write(180-(Joint1_ang+(i*2)));
    Joint2.write(Joint2_ang-(i*1));
    if(i >= 2 &&i <= 15) {
      Joint3.write(Joint3_ang-i);
    }
    delay(100);
  }
  Joint2_ang = Joint2.read();
  Joint1_ang = Joint1_1.read();
  Joint3_ang = Joint3.read();
}

void firstPos() { // 1번 위치에 컵놓기
  baseSpeed(Base_ang, 123);
  delay(500);
  joint3Speed(Joint3_ang, 110);
  delay(500);
  joint2Speed(Joint2_ang,80);
  delay(1300);
  
  for(int i ; i <=25 ; i++) {
    if(i<= 17) {
      Joint1_1.write(Joint1_ang-(i*2));
      Joint1_2.write(180-(Joint1_ang-(i*2)));
      Joint2.write(Joint2_ang+(i*1.5));
      delay(100);
    }
    else {
      Joint1_1.write(Joint1_ang-(i*2));
      Joint1_2.write(180-(Joint1_ang-(i*2)));
      delay(100);
    }
  }
  
  Joint1_ang = Joint1_1.read();
  Joint2_ang = Joint2.read();
}

void secondPos() { // 2번위치에 컵놓기
  baseSpeed(Base_ang, 143);
  delay(500);
  joint3Speed(Joint3_ang, 110);
  delay(500);
  joint2Speed(Joint2_ang,80);
  delay(1300);
  
  for(int i ; i <=25 ; i++) {
    if(i<= 17) {
      Joint1_1.write(Joint1_ang-(i*2));
      Joint1_2.write(180-(Joint1_ang-(i*2)));
      Joint2.write(Joint2_ang+(i*1.5));
      delay(100);
    }
    else {
      Joint1_1.write(Joint1_ang-(i*2));
      Joint1_2.write(180-(Joint1_ang-(i*2)));
      delay(100);
    }
  }
  
  Joint1_ang = Joint1_1.read();
  Joint2_ang = Joint2.read();
}

void thirdPos() { // 3번위치에 컵놓기
  baseSpeed(Base_ang, 160);
  delay(500);
  joint3Speed(Joint3_ang, 110);
  delay(500);
  joint2Speed(Joint2_ang,80);
  delay(1300);
  
  for(int i ; i <=25 ; i++) {
    if(i<= 17) {
      Joint1_1.write(Joint1_ang-(i*2));
      Joint1_2.write(180-(Joint1_ang-(i*2)));
      Joint2.write(Joint2_ang+(i*1.5));
      delay(100);
    }
    else {
      Joint1_1.write(Joint1_ang-(i*2));
      Joint1_2.write(180-(Joint1_ang-(i*2)));
      delay(100);
    }
  }
  
  Joint1_ang = Joint1_1.read();
  Joint2_ang = Joint2.read();
}

void coffee() {
  digitalWrite(relayPin,HIGH);
  if(ssa == "100"){
    digitalWrite(led1,HIGH);
  }
  if(ssa == "010"){
    digitalWrite(led2,HIGH);
  }
  if(ssa == "001"){
    digitalWrite(led3,HIGH);
  }
  delay(2000);
  digitalWrite(relayPin,LOW);
  digitalWrite(led1,LOW);
  digitalWrite(led2,LOW);
  digitalWrite(led3,LOW);
}

/*void serialCon() { //시리얼모니터
  if(Serial.available()>0) {
    int choice = Serial.parseInt();
    int Joint_ang = Serial.parseInt();
         
    if(Serial.read() == '\n') {
      Serial.print(choice, "관절 :");
      Serial.println(Joint_ang);
      if(Joint_ang >= 180) {
        Joint_ang = 180;
      }
      
      if(choice == 0) {
        baseSpeed(Base_ang, Joint_ang);
      }

      else if(choice == 1) {
        joint1Speed(Joint1_ang, Joint_ang);
      }

      else if(choice == 2) {
        joint2Speed(Joint2_ang, Joint_ang);
      }
      
      else if(choice == 3) {
        joint3Speed(Joint3_ang, Joint_ang);
      }

      else if(choice == 4) {
        endSpeed(End_ang, Joint_ang);
      }
      else if(choice == 5) {
        Base.write(Joint_ang);
      }
    }
  }
}*/

//-----------------------------------------------------------------------
void loop() {
  uno();
  Serial.print("a0 : ");
  Serial.println(a0);
   Serial.print("ssa : ");
  Serial.println(ssa);
  static unsigned long t_prev = 0;
    bool isOrder = false;
    if (btReadPacket()) {
      mtrDrive();
      t_prev = millis();
      isOrder = true;
    }


  //digitalWrite(13, ((millis()/250)%2) ? HIGH : LOW); 
}
