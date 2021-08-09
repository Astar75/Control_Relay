#pragma once

#include <Preferences.h>
#include <string>

class DataStore
{
private:

    const char *PREF_NAME = "esp32";
    const char *KEY_PASS = "key_pass";
    const char *KEY_DEVICE_NAME = "key_device_name";

    const char *DEFAULT_DEVICE_NAME = "BlueRelay";
    const char *DEFAULT_PASSWORD = "111111";  

    Preferences pref;
    
public:
    DataStore() {}
    ~DataStore() {}

    std::string getPassword(); 
    void savePassword(std::string password);

    std::string getDeviceName(); 
    void saveDeviceName(std::string deviceName);

    void resetData();  
};