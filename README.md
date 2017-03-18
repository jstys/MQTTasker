# Tasker Mqtt Subscriber Plugin
This app is divided into 2 parts: the first is a standalone app that will be present in a launcher's app drawer that allows the user to manage MQTT connection profiles as well as perform actions with a broker and view the status of topics.  The second part is the tasker plugin which the user interacts with via different Android Activities spawned by tasker when the user is creating an action or event.

## Standalone App
* Start / Stop the long running background service
* Create / Edit / Delete MQTT Connection profiles 
* Create / Edit / Delete subscriptions per each saved connection profile 

(Note: Subscriptions will always automatically be resubscribed to when a connection is re-established)


## Tasker Plugin
### Action Plugins
* Start / Stop the long-running foreground service
* Connect / Disconnect from MQTT Broker Profiles

### Event Plugins
* Connection Established / Lost to a specific broker
* New message for a subscribed topic
