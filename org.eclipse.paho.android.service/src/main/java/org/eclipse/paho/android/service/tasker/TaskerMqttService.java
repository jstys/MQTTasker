package org.eclipse.paho.android.service.tasker;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.eclipse.paho.android.service.MqttConnection;
import org.eclipse.paho.android.service.MqttConnectionProfileRecord;
import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.android.service.MqttServiceConstants;
import org.eclipse.paho.android.service.MqttServiceHandler;
import org.eclipse.paho.android.service.MqttSubscriptionRecord;
import org.eclipse.paho.android.service.ParcelableMqttMessage;
import org.eclipse.paho.android.service.Status;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Iterator;
import java.util.List;

/**
 * Created by jim.stys on 10/3/16.
 */

public class TaskerMqttService extends MqttService {
    private static final String TAG = "TaskerMqttService";
    private static final int SERVICE_NOTIF_ID = 101;

    private MqttServiceHandler mqttServiceHandler;
    private Messenger mqttServiceMessanger;
    private boolean serviceStarted;

    @Override
    public void onCreate() {
        super.onCreate();

        this.mqttServiceHandler = new MqttServiceHandler(this);
        this.mqttServiceMessanger = new Messenger(mqttServiceHandler);
        this.serviceStarted = false;
    }

    @Override
    public void onDestroy() {
        if(this.mqttServiceHandler != null)
        {
            this.mqttServiceHandler = null;
        }

        super.onDestroy();
    }

    private void persistState(Status status, Bundle data)
    {
        String action = data.getString(MqttServiceConstants.CALLBACK_ACTION, "");
        String clientId = data.getString(MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT, "");
        if(action.equals(MqttServiceConstants.CONNECT_ACTION)){
            if(status == Status.OK){
                setClientConnectedState(MqttConnectionProfileRecord.findOne(clientId), true);
            }
        }
        else if(action.equals(MqttServiceConstants.DISCONNECT_ACTION)){
            if(status == Status.OK){
                setClientConnectedState(MqttConnectionProfileRecord.findOne(clientId), false);
            }
        }
    }

    @Override
    public void callbackToActivity(String clientHandle, Status status,
                                   Bundle dataBundle) {
        // Don't call traceDebug, as it will try to callbackToActivity leading
        // to recursicallbackToActivityon.
        Intent callbackIntent = new Intent(
                MqttServiceConstants.CALLBACK_TO_ACTIVITY);
        if (clientHandle != null) {
            callbackIntent.putExtra(
                    MqttServiceConstants.CALLBACK_CLIENT_HANDLE, clientHandle);
        }
        callbackIntent.putExtra(MqttServiceConstants.CALLBACK_STATUS, status);
        if (dataBundle != null) {
            callbackIntent.putExtras(dataBundle);
        }
        persistState(status, dataBundle);
        sendBroadcast(callbackIntent);

        Log.d(TAG, "Sent broadcast back to activity");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.mqttServiceMessanger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = (intent == null) ? TaskerMqttConstants.START_SERVICE_ACTION : intent.getAction();
        String topicFilter;
        String profileName = null;
        List<MqttConnectionProfileRecord> profileList = null;
        Bundle data = intent.getExtras() == null ? new Bundle() : intent.getExtras();
        Bundle resultBundle = null;

        switch(action){
            case TaskerMqttConstants.START_SERVICE_ACTION:
                resultBundle = new Bundle();
                resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION, TaskerMqttConstants.START_SERVICE_ACTION);

                if(!this.serviceStarted) {
                    Log.d(TAG, "Starting the TaskerMqttService");

                    resetAllClientsConnectedState();

                    // Build required notification for foreground service
                    Notification notification = new NotificationCompat.Builder(this)
                            .setContentTitle("MQTT Subscriber Service")
                            .setTicker("MQTT Subscriber Service").build();
                    startForeground(SERVICE_NOTIF_ID,
                            notification);

                    registerBroadcastReceivers();
                    this.serviceStarted = true;
                    this.callbackToActivity(null, Status.OK, resultBundle);
                }
                else{
                    this.callbackToActivity(null, Status.ERROR, resultBundle);
                }
                break;
            case TaskerMqttConstants.CONNECT_ACTION:
                profileName = data.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA, null);
                profileList = MqttConnectionProfileRecord.find(MqttConnectionProfileRecord.class, "client_id = ?", profileName);

                if (profileList.size() == 1) {
                    MqttConnectOptions options = new MqttConnectOptions();
                    MqttConnectionProfileRecord profile = profileList.get(0);
                    options.setServerURIs(new String[]{profile.serverURI});
                    if (profile.autoReconnect) {
                        options.setAutomaticReconnect(true);
                    }
                    if (profile.cleanSession) {
                        options.setCleanSession(true);
                    }
                    if (profile.username != null && !profile.username.trim().equals("")) {
                        options.setUserName(profile.username);
                    }
                    if (profile.password != null && !profile.password.trim().equals("")) {
                        options.setPassword(profile.password.toCharArray());
                    }

                    try {
                        connect(getClient(profile.serverURI, profileName), options, profileName, null);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case TaskerMqttConstants.DISCONNECT_ACTION:
                profileName = data.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA, null);
                profileList = MqttConnectionProfileRecord.find(MqttConnectionProfileRecord.class, "client_id = ?", profileName);

                if (profileList.size() == 1) {
                    MqttConnectionProfileRecord record = profileList.get(0);
                    disconnect(getClient(record.serverURI, profileName), profileName, null);
                }
                break;
            case TaskerMqttConstants.SUBSCRIBE_ACTION:
                topicFilter = data.getString(TaskerMqttConstants.TOPIC_FILTER_EXTRA, null);
                int qos = data.getInt(TaskerMqttConstants.QOS_EXTRA, 0);

                if(topicFilter != null) {
                    IMqttMessageListener messageListener = new MqttMessageListener(this, topicFilter);
                    subscribe(TaskerMqttConstants.TASKER_CLIENT_ID, topicFilter, qos, null, null, messageListener);
                }
                break;
            case TaskerMqttConstants.UNSUBSCRIBE_ACTION:
                topicFilter = data.getString(TaskerMqttConstants.TOPIC_FILTER_EXTRA, null);
                if(topicFilter != null) {
                    unsubscribe(TaskerMqttConstants.TASKER_CLIENT_ID, topicFilter, null, null);
                }
                break;
            case TaskerMqttConstants.STOP_SERVICE_ACTION:
                this.serviceStarted = false;
                resultBundle = new Bundle();
                resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION, TaskerMqttConstants.STOP_SERVICE_ACTION);
                if(this.serviceStarted) {
                    this.callbackToActivity(null, Status.OK, resultBundle);
                }
                else{
                    this.callbackToActivity(null, Status.ERROR, resultBundle);
                }

                stopForeground(true);
                stopSelf();
                break;
            case TaskerMqttConstants.QUERY_SERVICE_RUNNING_ACTION:
                resultBundle = new Bundle();
                resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION, TaskerMqttConstants.QUERY_SERVICE_RUNNING_ACTION);
                if(this.serviceStarted) {
                    this.callbackToActivity(null, Status.OK, resultBundle);
                }
                else{
                    this.callbackToActivity(null, Status.ERROR, resultBundle);
                }
                break;
            default:
                break;
        }

        return START_STICKY;
    }

    /**
     * General-purpose IMqttActionListener for the Client context
     * <p>
     * Simply handles the basic success/failure cases for operations which don't
     * return results
     *
     */
    private class MqttMessageListener implements IMqttMessageListener {
        private String topicFilter;
        private Context context;

        public MqttMessageListener(Context context, String topicFilter)
        {
            this.topicFilter = topicFilter;
            this.context = context;
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Bundle data = new Bundle();
            data.putString(TaskerMqttConstants.TOPIC_EXTRA, topic);
            data.putString(TaskerMqttConstants.TOPIC_FILTER_EXTRA, this.topicFilter);
            data.putParcelable(TaskerMqttConstants.MESSAGE_EXTRA, new ParcelableMqttMessage(message));
            TaskerEventTrigger.triggerEvent(context, data);
        }
    }

    public String getClient(String brokerUri, String clientId)
    {
        if(!connections.containsKey(clientId))
        {
            MqttConnection client = new MqttConnection(this, brokerUri,
                    clientId);
            connections.put(clientId, client);
        }
        return clientId;
    }

    @Override
    public void unsubscribe(String clientHandle, String topic, String invocationContext, String activityToken) {
        super.unsubscribe(clientHandle, topic, invocationContext, activityToken);

        MqttSubscriptionRecord dbRecord = new MqttSubscriptionRecord(topic, clientHandle, 0);
        dbRecord.delete();
    }

    @Override
    public void disconnect(String clientHandle, String invocationContext, String activityToken) {
        MqttConnection client = getConnection(clientHandle);
        client.disconnect(invocationContext, activityToken);
        connections.remove(clientHandle);
    }

    public void subscribe(String clientHandle, String topicFilter, int qos, String invocationContext, String activityToken, IMqttMessageListener messageListener){
        MqttConnection client = getConnection(clientHandle);
        client.subscribe(topicFilter, qos, invocationContext, activityToken, messageListener);

        MqttSubscriptionRecord dbRecord = new MqttSubscriptionRecord(topicFilter, clientHandle, qos);
        dbRecord.save();
    }

    private void setClientConnectedState(MqttConnectionProfileRecord record, boolean connected){
        if(record != null){
            record.connected = connected;
            record.save();
        }
    }

    private void resetAllClientsConnectedState(){
        Iterator<MqttConnectionProfileRecord> iter = MqttSubscriptionRecord.findAll(MqttConnectionProfileRecord.class);
        while(iter.hasNext()){
            setClientConnectedState(iter.next(), false);
        }
    }
}
