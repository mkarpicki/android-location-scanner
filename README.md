### Description

Location Scanner is a simple Android demo app, which will watch for:
* Android's location update
* Bluetooth devices around
* WIFI Networks around
Scanned data will be sent to REST API(s).


### Logic
All the time scan for BT devices and collect them.  
If array is bigger than 25 (currently hardcoded value), send to REST API with last known own location

All the time scan for WIFI networks and collect them.  
If array is bigger than 25 (currently hardcoded value), send to REST API with last known own location

All the time scan for location update (currently hardcoded minTime 3000ms and min distance 5m).  
When position changed send collected BT devices and WIFI networks to REST APIs. 

*TODO*  
* *move hardcoded values to local.properties to overwrite defaults or even to be defined in UI*  
* *add REST API(s) with lists of BT devices and WIFI networks to ignore (to not find own band or headphones all the time)*    

### Setup

Modify or create local.properties file and add variables.

Pair of variables used for REST API where app will POST found BT devices.   
`bt.storage.apiKey=...` - value for x-api-key Header sent with request  
`bt.storage.host=...` - REST API endpoint which will expose POST method to consume devices  

Pair of variables used for REST API where app will POST found WIFI networks.         
`wifi.storage.apiKey=...` - value for x-api-key Header sent with request  
`wifi.storage.host=...` - REST API endpoint which will expose POST method to consume networks  

Pair of variables used for REST API which app can fetch list of devices to ignore when scanning  
`ignored-list.apiKey=...` - value for x-api-key Header sent with request  
`ignored-list.host=...` - REST API endpoint which will expose GET method to read list  

Expected `ignored-list` response:
```json
{
    "wifi": [
        {
            "bssid": "xx:xx:xx:xx:xx:xx"
        },
        {
            "bssid": "..."
        }
    ],
    "bt": [
        {
            "address": "xx:xx:xx:xx:xx:xx"
        },
        {
            "address": "..."
        }
    ]
}
```
> This config is optional. If not provided app will not use ifnored list.  

### External requirement(s)
Create own REST API which will expose defined endpoints and consume data

1. Bluetooth devices endpoint
```json
{
  "location": {
    "latitude": 53.3486734, 
    "longitude": 13.0112315
  }, 
  "devices": [{ 
    "rssi": -71,
    "location": {
      "latitude": 53.3486713, 
      "longitude": 13.011213
    },
    "timestamp": 1595456722064, 
    "device": {
      "address": "xx:xx:xx:xx:xx:xx",
      "name": "some name" 
    } 
  }] 
}
```
Payload will contain location of Android device from moment when data are sent and array of found devices, where each has information about:
* Android's location when BT device was found
* rssi
* timestamp (time when device was found)
* BT device information (MAC address and optional name if exposed)

2. WIFI networks endpoint
```json
{
  "location": {
    "latitude": 53.3486734, 
    "longitude": 13.0112315
  }, 
  "networks": [{ 
    "rssi": -71,
    "location": {
      "latitude": 53.3486713, 
      "longitude": 13.011213
    },
    "timestamp": 1595456722064, 
    "network": {
      "bssid": "xx:xx:xx:xx:xx:xx",
      "ssid": "some name" 
    } 
  }] 
}
```
Payload will contain location of Android device from moment when data are sent and array of found networks, where each has information about:
* Android's location when WIFI network was found
* rssi
* timestamp (time when network was found)
* WIFI network information (BSSID and SSID)