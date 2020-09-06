# Location Scanner
### (or MAC addresses scanner, which would be better name)    
___  
Location Scanner is a simple Android demo app, which will watch for:
* Device's location update
* Bluetooth devices around
* WIFI Networks around  
  
Scanned data will be sent to REST API(s).


### App Logic description

Application, after start will try to load list of MAC addresses to ignore (to avoid finding own smart watch or other device that can be in pocket).  
If list will be loaded successfully, it will be saved in local file, so if there would be any failure during load in future (next time when app stats), local version can be used.

Application will be watching for Bluetooth MAC addresses and WIFI Networks around. When it will collect enough items (in each separate array), it will send it using REST service.  
*Size of array(s) to collect items before sending is currently hardcoded in code.*

In case application will not find enough devices to send, sync will happen after location change "n-th" time *(where "n" is also hardcoded currently)*

Application will print found devices before syncing on screen (both lists are cleared after each successful response codes from REST API)  


*TODO*  
* *move hardcoded values to local.properties to overwrite defaults or even to be defined in UI*  

### Setup

To be able to run application, modify (or create) `local.properties` file and add variables.

Pair of variables used for REST API where app will POST found BT devices.   
`bt.storage.headers.authorization=...` - value for Authorization Header sent with request  
`bt.storage.url=...` - REST API endpoint which will expose POST method to consume devices  

Pair of variables used for REST API where app will POST found WIFI networks.         
`wifi.storage.headers.authorization=...` - value for Authorization Header sent with request  
`wifi.storage.url=...` - REST API endpoint which will expose POST method to consume networks  

Pair of variables used for REST API which app can fetch list of devices to ignore when scanning  
`ignored-list.headers.x-api-key=...` - value for x-api-key Header sent with request  
`ignored-list.url=...` - REST API endpoint which will expose GET method to read list  


### Additional (pre) requirements
Create own REST API which will expose defined endpoints and consume GeoJSON data

1. Bluetooth devices endpoint
```json
{
    "type": "FeatureCollection",
    "features": [{
        "geometry": {
            "type": "Point",
            "coordinates": [
                15.109267,
                53.4386779
            ]
        },
        "properties": {
            "name": "xx:xx:xx:xx:xx:xx",
            "rssi": -88,
            "device": {
                "address": "xx:xx:xx:xx:xx:xx"
            },
            "syncData": {
                "timestamp": 1598567007627,
                "location": {
                    "latitude": 53.4386691,
                    "longitude": 15.1092313
                }
            },
            "timestamp": 1598566704626
        },
        "type": "Feature"
    }]
}
```
Payload will expect GeoJSON format an array of features where: 
* geometry will be point with coordinates of Android device from moment where BT device was found, 
* properties object will contain information about BT device, time when was found, rssi, MAC address, name (if exists) and information about sync moment from Android device to cloud (location and time)

2. WIFI networks endpoint
```json
{
    "type": "FeatureCollection",
    "features": [
        {
            "geometry": {
                "type": "Point",
                "coordinates": [
                    15.109267,
                    53.3486779
                ]
            },
            "properties": {
                "name": "yyyyyyyy",
                "rssi": -91,
                "network": {
                    "ssid": "yyyyyyy",
                    "bssid": "xx:xx:xx:xx:xx:xx"
                },
                "syncData": {
                    "timestamp": 1598566736896,
                    "location": {
                        "latitude": 53.3486779,
                        "longitude": 15.109267
                    }
                },
                "timestamp": 1598566736878
            },
            "type": "Feature"
        }
    ]
}
```
Payload will contain GeoJSON format, where each feature in array contains:
* geometry - point with coordinates of Android device from moment where WIFI network was found, 
* properties object which contains information about WIFI network, time when was found, ssid, bssid, rssi and information about sync moment from Android device to cloud (location and time)

3. Ignored addresses API which will expose GET endpoint and return aray of addresses as response:  
```json
[
  "xx:xx:xx:xx:xx:xx",
  "yy:yy:yy:yy:yy:yy",
  "..."
]

```
