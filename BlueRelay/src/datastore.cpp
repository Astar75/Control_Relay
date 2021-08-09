#include "datastore.h"

std::string DataStore::getPassword() 
{
    std::string password; 
    pref.begin(PREF_NAME, false);
    password = pref.getString(KEY_PASS, "0000").c_str(); 
    pref.end(); 
    return password;
} 

void DataStore::savePassword(std::string password) 
{
    pref.begin(PREF_NAME, false);
    pref.putString(KEY_PASS, password.c_str()); 
    pref.end(); 
}


std::string DataStore::getDeviceName() 
{
    std::string deviceName; 
    pref.begin(PREF_NAME, false);
    deviceName = pref.getString(KEY_DEVICE_NAME, "ESPRelay").c_str(); 
    pref.end(); 
    return deviceName;
}

void DataStore::saveDeviceName(std::string deviceName) 
{
    pref.begin(PREF_NAME, false);
    pref.putString(KEY_DEVICE_NAME, deviceName.c_str()); 
    pref.end(); 
}

void DataStore::resetData() 
{
    pref.begin(PREF_NAME, false);
    pref.putString(KEY_DEVICE_NAME, DEFAULT_DEVICE_NAME);
    pref.putString(KEY_PASS, DEFAULT_PASSWORD); 
    pref.end(); 
}