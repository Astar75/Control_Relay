#include "bluetooth.h"

void Bluetooth::setEncryptPassword(uint32_t password) 
{
    this->encryptPass = password;
    Serial.println(encryptPass);
} 

void Bluetooth::setCallback(void(*fptr) (uint8_t command, std::string data)) 
{
    this->callback = fptr;
}

void Bluetooth::start() 
{
    btStart(); 
    BLEDevice::init(deviceName);
    BLEDevice::setEncryptionLevel(ESP_BLE_SEC_ENCRYPT);
    BLEDevice::setSecurityCallbacks(new SecurityCallback(*this));
    BLEServer *server = BLEDevice::createServer(); 
    server->setCallbacks(new ServerCallback(*this));
    BLEService *service = server->createService(SERVICE_UUID); 
    characteristicRequest = service->createCharacteristic(CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_WRITE);
    characteristicRequest->setAccessPermissions(ESP_GATT_PERM_READ_ENCRYPTED | ESP_GATT_PERM_WRITE_ENCRYPTED);
    characteristicRequest->addDescriptor(new BLE2902());
    characteristicRequest->setCallbacks(new CharacteristicCallback(*this));
    characteristicResponse = service->createCharacteristic(CHARACTERISTIC_RESPONSE, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_NOTIFY);
    characteristicResponse->addDescriptor(new BLE2902()); 
    characteristicResponse->setCallbacks(new CharacteristicCallback(*this));  

    service->start(); 

    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->setScanResponse(true);
    pAdvertising->setMinPreferred(0x06);
    pAdvertising->setMinPreferred(0x12);
    BLEDevice::startAdvertising();

    setupSecurity();

    ready = true;
}

void Bluetooth::setupSecurity() {
    esp_ble_auth_req_t auth_req = ESP_LE_AUTH_REQ_SC_MITM_BOND;
    esp_ble_io_cap_t iocap = ESP_IO_CAP_OUT;          
    uint8_t key_size = 16;     
    uint8_t init_key = ESP_BLE_ENC_KEY_MASK | ESP_BLE_ID_KEY_MASK;
    uint8_t rsp_key = ESP_BLE_ENC_KEY_MASK | ESP_BLE_ID_KEY_MASK;
    uint32_t passkey = encryptPass; 
    uint8_t auth_option = ESP_BLE_ONLY_ACCEPT_SPECIFIED_AUTH_DISABLE;
    esp_ble_gap_set_security_param(ESP_BLE_SM_SET_STATIC_PASSKEY, &passkey, sizeof(uint32_t));
    esp_ble_gap_set_security_param(ESP_BLE_SM_AUTHEN_REQ_MODE, &auth_req, sizeof(uint8_t));
    esp_ble_gap_set_security_param(ESP_BLE_SM_IOCAP_MODE, &iocap, sizeof(uint8_t));
    esp_ble_gap_set_security_param(ESP_BLE_SM_MAX_KEY_SIZE, &key_size, sizeof(uint8_t));
    esp_ble_gap_set_security_param(ESP_BLE_SM_ONLY_ACCEPT_SPECIFIED_SEC_AUTH, &auth_option, sizeof(uint8_t));
    esp_ble_gap_set_security_param(ESP_BLE_SM_SET_INIT_KEY, &init_key, sizeof(uint8_t));
    esp_ble_gap_set_security_param(ESP_BLE_SM_SET_RSP_KEY, &rsp_key, sizeof(uint8_t));
}

void Bluetooth::stop() 
{
    ready = false; 
    if (btStarted()) btStop(); 
}

bool Bluetooth::isConnected() { return connected; }

bool Bluetooth::isReady() { return ready; }
