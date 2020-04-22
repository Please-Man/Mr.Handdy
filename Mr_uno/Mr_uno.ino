#include "WiFiEsp.h"
// Emulate Serial1 on pins 6/7 if not present
#ifndef HAVE_HWSERIAL1
#endif
#include "SoftwareSerial.h"
#include <ArduinoJson.h>
#include <string.h>

SoftwareSerial Serial1(2, 3); // RX, TX wifi
SoftwareSerial Serial3(4, 5); // RX, TX 시리얼통신

char ssid[] = "wifi01";            // your network SSID (name)
char pass[] = "m25692547m";        // your network password
int status = WL_IDLE_STATUS;     // the Wifi radio's status

char server[] = "joon16.iptime.org"; // 서버 도메인 또는 ip 주소 입력

// Initialize the Ethernet client object
WiFiEspClient client;

static int goStop_flag = 1; //수신 지속변수
char idc[2] = {0};

void setup() {
  Serial.begin(9600);
  Serial3.begin(9600);
  Serial1.begin(9600);
  WiFi.init(&Serial1);
}

void printWifiStatus(){
  
  // print the SSID of the network you're attached to
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print your WiFi shield's IP address
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // print the received signal strength
  long rssi = WiFi.RSSI();
  Serial.print("Signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");
}

void getOrderData(){ // 제이슨 파싱
  
  StaticJsonBuffer<200> jsonBuffer;
  String json_str = "";

  // if there are incoming bytes available
  // from the server, read them and print them
  int headcount = 0;
  while (client.available()) {
    char c = client.read(); // 의심 포인트 1 char []로 바로 넣어줄 방법을 생각하는것도 ...
    json_str += c;// 의심 포인트 2 // String으로  char 로 받아주느것

    if(c == '\r'){
      headcount++;

      if(headcount != 7){
        json_str = "";
      }
    }
 }
 
  Serial.print("String : ");
  Serial.println(json_str);
  
  Serial.println("===========");
    Serial.print("char[] : ");
  char json[json_str.length()+1] = {0};
  json_str.toCharArray(json, json_str.length()+1); // String -> Char
  
  for(int i = 0; i<json_str.length()+1; i++){
    Serial.print(json[i]);
  }
  Serial.println();
  
  
  JsonObject& root = jsonBuffer.parseObject(json);

  // Test if parsing succeeds.
  if (!root.success()) {
    Serial.println("parseObject() failed");
    goStop_flag = 1;
    return;
  }
  
  int id = root["result"][0]["idx"];
  int eso_hot = root["result"][0]["eso_hot"];
  int ame_hot = root["result"][0]["ame_hot"];
  int ame_cold = root["result"][0]["ame_cold"];
  
  String idstr = String(id);
  idc[2] = {0};
  idstr.toCharArray(idc,idstr.length()+1);
  
  Serial.println("aaa");
  Serial.println(id);
  Serial.println(eso_hot);
  Serial.println(ame_hot);
  Serial.println(ame_cold);
  Serial.println("bbb");

  delay(50);
  //메가로 데이터 전송
  //Serial3.write(idc[0]);
  
  Serial3.write(48+9);
  delay(50);
  Serial3.write(idc[1]);
  delay(50);
  Serial3.write(eso_hot+48);
  delay(50);
  Serial3.write(ame_hot+48);
  delay(50);
  Serial3.write(ame_cold+48);
  delay(50);

}

void dataParsing(){
  
  // check for the presence of the shield
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi shield not present");
    // don't continue
    while (true);
  }

  // attempt to connect to WiFi network
  while ( status != WL_CONNECTED) {
    Serial.print("Attempting to connect to WPA SSID: ");
    Serial.println(ssid);
    // Connect to WPA/WPA2 network
    status = WiFi.begin(ssid, pass);
  }

  // you're connected now, so print out the data
  Serial.println("You're connected to the network");
  printWifiStatus();
  Serial.println();
  Serial.println("Starting connection to server...");
  // if you get a connection, report back via serial
  if (client.connect(server, 8080)) { // 포트번호 입력
    Serial.println("Connected to server");
    // Make a HTTP request
    client.println("GET /MrHanddy/Select_LatestOrder.php HTTP/1.1"); // 도메인 또는 IP 빼고 접속하고자 하는 URL입력
    client.println("Host: joon16.iptime.org"); // 도메인 또는 IP 입력
    //client.println("Connection: close");
    client.println();
  }
  
  Serial.println("[parsing_Data]");
  getOrderData();

  // if the server's disconnected, stop the client
  if (!client.connected()) {
    Serial.println();
    Serial.println("Disconnecting from server...");
    client.stop();

    // do nothing forevermore
    while (true);
  }
}

void gettingData(String idx){       // 푸시데이터 송신
  
  // check for the presence of the shield
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi shield not present");
    // don't continue
    while (true);
  }

  // attempt to connect to WiFi network
  while ( status != WL_CONNECTED) {
    Serial.print("Attempting to connect to WPA SSID: ");
    Serial.println(ssid);
    // Connect to WPA/WPA2 network
    status = WiFi.begin(ssid, pass);
  }

  // you're connected now, so print out the data
  Serial.println("You're connected to the network");
  printWifiStatus();
  Serial.println();
  Serial.println("Starting connection to server...");
  // if you get a connection, report back via serial

  if (client.connect(server, 8080)) { // 포트번호 입력
    Serial.println("Connected to server");
    Serial.println("[getting_Data]");
    // Make a HTTP request
    //String idx = "10"; // 음료 나왔을대 그 음료 대한 주인 idx값
    client.println("GET /MrHanddy/Push_Notification.php?idx="+ idx +" HTTP/1.1"); // 도메인 또는 IP 빼고 접속하고자 하는 URL입력
    client.println("Host: joon16.iptime.org"); // 도메인 또는 IP 입력
    //client.println("Connection: close");
    client.println();
  }
}

void loop() {
    String idx = "";
    dataParsing();
    delay(60000);
    idx = String(idc[0])+String(idc[1]);
    Serial.print("idx : ");
    Serial.println(idx);
    gettingData(idx);
    delay(500);
}
