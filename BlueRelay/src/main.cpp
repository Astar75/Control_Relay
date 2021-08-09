#include <Arduino.h>
#include "datastore.h"
#include "bluetooth.h"

#define PIN_RELAY_CONTROL 2
#define PIN_BUTTON_FACTORY_RESET 15

DataStore *store;
Bluetooth *bluetooth;

bool buttonStateFlag = false; 
uint32_t buttonPressTimer = 0;

void bluetoothCallback(uint8_t command, std::string data); 
void factoryReset(); 

void setup()
{
  Serial.begin(115200);
  Serial.println("Start...");

  store = new DataStore();
  uint32_t password = atoi(store->getPassword().c_str());
  std::string deviceName = store->getDeviceName();

  bluetooth = new Bluetooth(deviceName);
  bluetooth->setEncryptPassword(password);
  bluetooth->setCallback(bluetoothCallback);
  bluetooth->start();

  pinMode(PIN_RELAY_CONTROL, OUTPUT);
  pinMode(PIN_BUTTON_FACTORY_RESET, INPUT); 
}

void loop()
{
  bool buttonState = digitalRead(PIN_BUTTON_FACTORY_RESET);
  if (buttonState && !buttonStateFlag && millis() - buttonPressTimer > 100) {
    buttonStateFlag = true;
    buttonPressTimer = millis();
    factoryReset();
  }
}

void bluetoothCallback(uint8_t command, std::string data) 
{
  switch(command) {
    case Bluetooth::Commands::CMD_CHANGE_DEVICE_NAME: 
      Serial.println("Change device name");
      if (data.size() > 0) {
        store->saveDeviceName(data);
        delay(1000); 
        ESP.restart();
      }
      break;
    case Bluetooth::Commands::CMD_CHANGE_PASSWORD:
      Serial.println("Change password");
      if (data.size() > 0) {
        store->savePassword(data);
        delay(1000); 
        ESP.restart();
      } 
      break;
    case Bluetooth::Commands::CMD_FACTORY_RESET:
      factoryReset();
      break;
    case Bluetooth::Commands::CMD_RELAY_CLOSE:
      Serial.println("Выключаем реле");   
      digitalWrite(2, LOW); 
      break;
    case Bluetooth::Commands::CMD_RELAY_OPEN:
      Serial.println("Включаем реле");   
      digitalWrite(2, HIGH);
      break;
  } 
}

void factoryReset() 
{
  Serial.println("Factory reset");
  store->resetData(); 
  ESP.restart();
}