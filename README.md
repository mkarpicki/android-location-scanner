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
[
  "xx:xx:xx:xx:xx:xx",
  "yy:yy:yy:yy:yy:yy",
  "..."
]

```
> This config is optional. If not provided app will not use ifnored list.  

### External requirement(s)
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
Payload will contain GeoJSON format of data with an array of features where: 
* geometry will be point with coordinates of Android device from moment where BT device was found, 
* properties object will contain information about BT device, time when was found and information about sync moment from Android device to cloud (location and time)

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
Payload will contain GeoJSON format, where each feature in array where:
* geometry will be point with coordinates of Android device from moment where WIFI network was found, 
* properties object will contain information about WIFI network, time when was found and information about sync moment from Android device to cloud (location and time)