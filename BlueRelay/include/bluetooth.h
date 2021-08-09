#pragma once

#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

class Bluetooth
{
    class CharacteristicCallback : public BLECharacteristicCallbacks
    {
        Bluetooth &bluetooth;

        const std::string CHANGE_PASS = "chp";
        const std::string CHANGE_NAME = "chn";
        const std::string FACTORY_RESET = "fr";
        const std::string RELAY_OPEN = "ro";
        const std::string RELAY_CLOSE = "rc";
        const std::string RELAY_CHECK = "get_state";

        void sendResponse(std::string message)
        {
            this->bluetooth.characteristicResponse->setValue(message);
            this->bluetooth.characteristicResponse->notify();
        }

        void onWrite(BLECharacteristic *chr)
        {
            std::string data = chr->getValue();

            if (data.rfind("p=", 0) == 0)
            {
                size_t delim = data.find(',');
                uint32_t pass = atoi(data.substr(2, delim - 2).c_str());

                if (pass == this->bluetooth.encryptPass)
                {
                    sendResponse("correct_pass");

                    if (data.find(CHANGE_NAME, delim) != std::string::npos)
                    {
                        size_t pos = delim + CHANGE_NAME.size() + 2;
                        std::string name = data.substr(pos);
                        this->bluetooth.callback(Commands::CMD_CHANGE_DEVICE_NAME, name);
                        this->sendResponse("suc_ch_n");
                    }
                    else if (data.find(CHANGE_PASS, delim) != std::string::npos)
                    {
                        size_t pos = delim + CHANGE_PASS.size() + 2;
                        std::string password = data.substr(pos);
                        this->bluetooth.callback(Commands::CMD_CHANGE_PASSWORD, password);
                        this->sendResponse("suc_ch_pass");
                    }
                    else if (data.find(FACTORY_RESET, delim) != std::string::npos)
                    {
                        this->bluetooth.callback(Commands::CMD_FACTORY_RESET, "");
                        this->sendResponse("fc_reset");
                    }
                    else if (data.find(RELAY_CLOSE, delim) != std::string::npos)
                    {
                        this->bluetooth.callback(Commands::CMD_RELAY_CLOSE, "");
                        this->sendResponse("rel_close");
                    }
                    else if (data.find(RELAY_OPEN, delim) != std::string::npos)
                    {
                        this->bluetooth.callback(Commands::CMD_RELAY_OPEN, "");
                        this->sendResponse("rel_open");
                    }
                    else if (data.find(RELAY_CHECK, delim) != std::string::npos)
                    {
                        if (digitalRead(2) == HIGH)
                            sendResponse("open");
                        else
                            sendResponse("close");
                    }
                    else
                    {
                        Serial.println("Команда не опознана");
                        this->sendResponse("err_cmd_error");
                    }
                }
                else
                {
                    Serial.println("Пароль не верный!");
                    this->sendResponse("err_pass");
                }
            }
        }

    public:
        CharacteristicCallback(Bluetooth &ble) : bluetooth(ble){};
    };

    class ServerCallback : public BLEServerCallbacks
    {
        Bluetooth &bluetooth;

        void onConnect(BLEServer *server)
        {
            this->bluetooth.connected = true;
        }

        void onDisonnect(BLEServer *server)
        {
            this->bluetooth.connected = false;
        }

    public:
        ServerCallback(Bluetooth &ble) : bluetooth(ble){};
    };

    class SecurityCallback : public BLESecurityCallbacks
    {

        Bluetooth &bluetooth;

        uint32_t onPassKeyRequest()
        {
            return 000000;
        }

        void onPassKeyNotify(uint32_t pass_key) {}

        bool onConfirmPIN(uint32_t pass_key)
        {
            delay(5000);
            return true;
        }

        bool onSecurityRequest()
        {
            return true;
        }

        void onAuthenticationComplete(esp_ble_auth_cmpl_t cmpl)
        {
            if (cmpl.success)
            {
                Serial.println("Security: Authentification success!");
            }
            else
            {
                Serial.println("Security: Authentification failed!");
            }
            BLEDevice::startAdvertising();
        }

    public:
        SecurityCallback(Bluetooth &ble) : bluetooth(ble){};
    };

private:
    const char *SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    const char *CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    const char *CHARACTERISTIC_RESPONSE = "0543e43b-ca02-46e2-a3a6-eef13baee160";

    std::string deviceName;
    uint32_t encryptPass = 111111;

    ServerCallback *serverCallback;
    CharacteristicCallback *characteristicCallback;

    bool connected = false;
    bool ready = false;

    BLECharacteristic *characteristicRequest;
    BLECharacteristic *characteristicResponse;

    void setupSecurity();
    void (*callback)(uint8_t command, std::string data);

public:
    enum Commands : uint8_t
    {
        CMD_CHANGE_PASSWORD = 1,
        CMD_CHANGE_DEVICE_NAME = 2,
        CMD_FACTORY_RESET = 3,
        CMD_RELAY_CLOSE = 4,
        CMD_RELAY_OPEN = 5
    };

    Bluetooth(std::string deviceName)
    {
        this->deviceName = deviceName;
    }
    ~Bluetooth() {}

    void setEncryptPassword(uint32_t password);
    void setCallback(void (*fptr)(uint8_t command, std::string data));
    void start();
    void stop();
    bool isConnected();
    bool isReady();

    std::string getCharacteristicRequestUUID() { return CHARACTERISTIC_UUID; }
    std::string getCharacteristicResponseUUID() { return CHARACTERISTIC_RESPONSE; }
};
