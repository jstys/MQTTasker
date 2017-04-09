package org.eclipse.paho.android.service.tasker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.eclipse.paho.android.service.MqttConnection;
import org.eclipse.paho.android.service.MqttConnectionProfileRecord;
import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.android.service.MqttServiceConstants;
import org.eclipse.paho.android.service.MqttSubscriptionRecord;
import org.eclipse.paho.android.service.Status;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TaskerMqttService extends MqttService {
    private static final String TAG = "TaskerMqttService";
    private static final int SERVICE_NOTIF_ID = 1;

    private boolean mServiceStarted;
    private int mNumConnectedProfiles;
    private Map<String, Intent> mSavedTaskerActionIntents;

    @Override
    public void onCreate() {
        super.onCreate();

        mServiceStarted = false;
        mNumConnectedProfiles = 0;
        mSavedTaskerActionIntents = new HashMap<>();

        Iterator<MqttConnectionProfileRecord> iter = MqttConnectionProfileRecord.findAll(MqttConnectionProfileRecord.class);
        while(iter.hasNext()){
            MqttConnectionProfileRecord record = iter.next();
            MqttConnection connection = new MqttConnection(this, record.serverURI, record.clientId, null, record.profileName);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setServerURIs(new String[]{record.serverURI});
            if(record.username != null && !record.username.trim().equals("")) {
                options.setUserName(record.username);
            }
            if(record.password != null && !record.password.trim().equals("")) {
                options.setPassword(record.password.toCharArray());
            }
            connection.setConnectOptions(options);
            connections.put(record.profileName, connection);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = (intent == null) ? TaskerMqttConstants.START_SERVICE_ACTION : intent.getAction();
        Bundle data = (intent == null || intent.getExtras() == null) ? new Bundle() : intent.getExtras();

        switch (action) {
            case TaskerMqttConstants.START_SERVICE_ACTION:
                ProcessStartServiceAction();
                break;
            case TaskerMqttConstants.CONNECT_ACTION:
                ProcessConnectAction(data);
                break;
            case TaskerMqttConstants.DISCONNECT_ACTION:
                ProcessDisconnectAction(data);
                break;
            case TaskerMqttConstants.SUBSCRIBE_ACTION:
                ProcessSubscribeAction(data);
                break;
            case TaskerMqttConstants.UNSUBSCRIBE_ACTION:
                ProcessUnsubscribeAction(data);
                break;
            case TaskerMqttConstants.STOP_SERVICE_ACTION:
                ProcessStopServiceAction();
                break;
            case TaskerMqttConstants.QUERY_SERVICE_RUNNING_ACTION:
                ProcessQueryServiceAction();
                break;
            case TaskerMqttConstants.QUERY_PROFILE_CONNECTED_ACTION:
                ProcessQueryProfileConnectedAction(data);
                break;
            case TaskerMqttConstants.PROFILE_CREATED_ACTION:
                ProcessProfileCreatedAction(data);
                break;
            case TaskerMqttConstants.PROFILE_DELETED_ACTION:
                ProcessProfileDeletedAction(data);
                break;
            case TaskerMqttConstants.SUBSCRIPTION_CREATED_ACTION:
                ProcessSubscriptionCreatedAction(data);
                break;
            case TaskerMqttConstants.SUBSCRIPTION_DELETED_ACTION:
                ProcessSubscriptionDeletedAction(data);
                break;
            default:
                break;
        }

        return START_STICKY;
    }

    private void ProcessStartServiceAction(){
        Log.d(TAG, "Received the Start Service action");

        Bundle resultBundle = new Bundle();
        resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION, TaskerMqttConstants.START_SERVICE_ACTION);

        if(!mServiceStarted) {
            Log.d(TAG, "Starting the TaskerMqttService");

            resetAllClientsConnectedState();
            startForeground(SERVICE_NOTIF_ID,
                    getServiceNotification());

            registerBroadcastReceivers();
            mServiceStarted = true;
            this.callbackToActivity(null, Status.OK, resultBundle);
        }
        else{
            Log.d(TAG, "Returning error, service is already started");

            this.callbackToActivity(null, Status.ERROR, resultBundle);
        }
    }

    private void ProcessStopServiceAction(){
        Log.d(TAG, "Received the Stop Service action");

        mServiceStarted = false;
        Bundle resultBundle = new Bundle();
        resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION, TaskerMqttConstants.STOP_SERVICE_ACTION);
        if(mServiceStarted) {
            Log.d(TAG, "Service is being stopped");

            this.callbackToActivity(null, Status.OK, resultBundle);
        }
        else{
            Log.d(TAG, "Returning error, service was already stopped");

            this.callbackToActivity(null, Status.ERROR, resultBundle);
        }

        stopForeground(true);
        stopSelf();
    }

    private void ProcessConnectAction(Bundle dataBundle){
        if(!validateBundle(dataBundle, Arrays.asList(TaskerMqttConstants.PROFILE_NAME_EXTRA,
                                                     TaskerMqttConstants.RECONNECT_EXTRA,
                                                     TaskerMqttConstants.CLEAN_SESSION_EXTRA))){
            Log.w(TAG, "invalid bundle for connect action");
            return;
        }
        String profileName = dataBundle.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA);
        if(dataBundle.containsKey(TaskerMqttConstants.TASKER_SAVED_SETTING_INTENT)) {
            Intent taskerIntent = dataBundle.getParcelable(TaskerMqttConstants.TASKER_SAVED_SETTING_INTENT);
            mSavedTaskerActionIntents.put(profileName, taskerIntent);
        }
        if(!mServiceStarted){
            notifyActionError(profileName, TaskerMqttConstants.CONNECT_ACTION, "service is not started");
            sendTaskerSynchronousResult(profileName, false);
            return;
        }
        if(!isOnline()){
            notifyActionError(profileName, TaskerMqttConstants.CONNECT_ACTION, "no network connectivity");
            sendTaskerSynchronousResult(profileName, false);
            return;
        }
        if(!validateExistingProfile(profileName)){
            notifyActionError(profileName, TaskerMqttConstants.CONNECT_ACTION, "invalid profile name");
            sendTaskerSynchronousResult(profileName, false);
            return;
        }

        boolean autoReconnect = dataBundle.getBoolean(TaskerMqttConstants.RECONNECT_EXTRA, true);
        boolean cleanSession = dataBundle.getBoolean(TaskerMqttConstants.CLEAN_SESSION_EXTRA, true);

        Log.d(TAG, "Received the Connect action for profile " + profileName + " with autoReconnect = " + autoReconnect + " and cleanSession = " + cleanSession);

        MqttConnection profile = getConnection(profileName);
        MqttConnectOptions options = profile.getConnectOptions();

        options.setServerURIs(new String[]{profile.getServerURI()});
        options.setAutomaticReconnect(autoReconnect);
        options.setCleanSession(cleanSession);
        Log.d(TAG, "Setting connection options to autoReconnect = " + autoReconnect + " cleanSession = " + cleanSession);

        try {
            connect(profileName, null, null, null);
        } catch (MqttException e) {
            Log.e(TAG, "Unable to connect to profile " + profileName);
            e.printStackTrace();
        }
    }

    private void ProcessDisconnectAction(Bundle dataBundle){
        if(!validateBundle(dataBundle, Arrays.asList(TaskerMqttConstants.PROFILE_NAME_EXTRA))){
            Log.w(TAG, "invalid bundle for disconnect action");
            return;
        }

        String profileName = dataBundle.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA);
        if(!mServiceStarted){
            notifyActionError(profileName, TaskerMqttConstants.DISCONNECT_ACTION, "service is not started");
            return;
        }
        if(!isOnline()){
            notifyActionError(profileName, TaskerMqttConstants.DISCONNECT_ACTION, "no network connectivity");
            return;
        }
        if(!validateExistingProfile(profileName)){
            notifyActionError(profileName, TaskerMqttConstants.DISCONNECT_ACTION, "invalid profile name");
            return;
        }

        Log.d(TAG, "Received the Disconnect action for profile " + profileName);

        disconnect(profileName, null, null);
    }

    private void ProcessSubscribeAction(Bundle dataBundle){
        if(!validateBundle(dataBundle, Arrays.asList(TaskerMqttConstants.PROFILE_NAME_EXTRA,
                                                     TaskerMqttConstants.TOPIC_FILTER_EXTRA,
                                                     TaskerMqttConstants.QOS_EXTRA))){
            Log.w(TAG, "invalid bundle for subscribe action");
            return;
        }

        String topicFilter = dataBundle.getString(TaskerMqttConstants.TOPIC_FILTER_EXTRA);
        int qos = dataBundle.getInt(TaskerMqttConstants.QOS_EXTRA);
        String profileName = dataBundle.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA);
        if(!mServiceStarted){
            notifyActionError(profileName, TaskerMqttConstants.SUBSCRIBE_ACTION, "service is not started");
            return;
        }
        if(!isOnline()){
            notifyActionError(profileName, TaskerMqttConstants.SUBSCRIBE_ACTION, "no network connectivity");
            return;
        }
        if(!validateExistingProfile(profileName)){
            notifyActionError(profileName, TaskerMqttConstants.SUBSCRIBE_ACTION, "invalid profile name");
            return;
        }

        Log.d(TAG, "Subscribe action for profile " + profileName + " with topicFilter = " + topicFilter + " and qos = " + qos);

        subscribe(profileName, topicFilter, qos, null, null);
    }

    private void ProcessUnsubscribeAction(Bundle dataBundle){
        if(!validateBundle(dataBundle, Arrays.asList(TaskerMqttConstants.PROFILE_NAME_EXTRA,
                                                     TaskerMqttConstants.TOPIC_FILTER_EXTRA))){
            Log.w(TAG, "invalid bundle for unsubscribe action");
            return;
        }

        String topicFilter = dataBundle.getString(TaskerMqttConstants.TOPIC_FILTER_EXTRA, null);
        String profileName = dataBundle.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA, null);
        if(!mServiceStarted){
            notifyActionError(profileName, TaskerMqttConstants.UNSUBSCRIBE_ACTION, "service is not started");
            return;
        }
        if(!isOnline()){
            notifyActionError(profileName, TaskerMqttConstants.UNSUBSCRIBE_ACTION, "no network connectivity");
            return;
        }
        if(!validateExistingProfile(profileName)){
            notifyActionError(profileName, TaskerMqttConstants.UNSUBSCRIBE_ACTION, "invalid profile name");
            return;
        }

        Log.d(TAG, "Unsubscribe action for profile = " + profileName + " and topicFilter = " + topicFilter);

        unsubscribe(profileName, topicFilter, null, null);
    }

    private void ProcessQueryServiceAction(){
        Log.d(TAG, "Query Service Action received");

        Bundle resultBundle = new Bundle();
        resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION, TaskerMqttConstants.QUERY_SERVICE_RUNNING_ACTION);
        if(this.mServiceStarted) {
            this.callbackToActivity(null, Status.OK, resultBundle);
        }
        else{
            this.callbackToActivity(null, Status.ERROR, resultBundle);
        }
    }

    private void ProcessSubscriptionCreatedAction(Bundle dataBundle){
        if(!validateBundle(dataBundle, Arrays.asList(TaskerMqttConstants.PROFILE_NAME_EXTRA,
                                                     TaskerMqttConstants.TOPIC_FILTER_EXTRA,
                                                     TaskerMqttConstants.QOS_EXTRA))){
            Log.w(TAG, "invalid bundle for subscription created action");
            return;
        }

        String profileName = dataBundle.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA);
        if(!validateExistingProfile(profileName)){
            return;
        }

        String topic = dataBundle.getString(TaskerMqttConstants.TOPIC_FILTER_EXTRA);
        int qos = dataBundle.getInt(TaskerMqttConstants.QOS_EXTRA);


        subscribe(profileName, topic, qos, null, null);
    }

    private void ProcessSubscriptionDeletedAction(Bundle dataBundle){
        if(!validateBundle(dataBundle, Arrays.asList(TaskerMqttConstants.PROFILE_NAME_EXTRA,
                                                     TaskerMqttConstants.TOPIC_FILTER_EXTRA))){
            Log.w(TAG, "invalid bundle for subscription deleted action");
            return;
        }

        String profileName = dataBundle.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA);
        if(!validateExistingProfile(profileName)){
            return;
        }

        String topic = dataBundle.getString(TaskerMqttConstants.TOPIC_FILTER_EXTRA);

        unsubscribe(profileName, topic, null, null);
    }

    private void ProcessQueryProfileConnectedAction(Bundle dataBundle){
        Bundle resultBundle = new Bundle();
        String profileName = dataBundle.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA);

        if(!mServiceStarted){
            notifyActionError(profileName, TaskerMqttConstants.QUERY_PROFILE_CONNECTED_ACTION, "service is not started");
            return;
        }

        if(!dataBundle.containsKey(TaskerMqttConstants.PROFILE_NAME_EXTRA)){
            for(String connectionKey : connections.keySet()){
                MqttConnection connection = connections.get(connectionKey);
                resultBundle.putBoolean(connectionKey, connection.isConnected());
            }
        }
        else if(profileName != null && validateExistingProfile(profileName)){
            resultBundle.putBoolean(profileName, getConnection(profileName).isConnected());
        }
        else{
            Log.w(TAG, "invalid profile specified = " + profileName);
            notifyActionError(profileName, TaskerMqttConstants.QUERY_PROFILE_CONNECTED_ACTION, "invalid profile name");
            return;
        }

        resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION, TaskerMqttConstants.QUERY_PROFILE_CONNECTED_ACTION);
        this.callbackToActivity(profileName, Status.OK, resultBundle);
    }

    private void ProcessProfileCreatedAction(Bundle dataBundle){
        if(!validateBundle(dataBundle, Arrays.asList(TaskerMqttConstants.PROFILE_NAME_EXTRA))){
            Log.w(TAG, "invalid bundle for profile created action");
            return;
        }

        String profileName = dataBundle.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA);

        Log.d(TAG, "Profile created callback for profile name = " + profileName);

        MqttConnectionProfileRecord createdRecord = MqttConnectionProfileRecord.findOne(profileName);
        if(createdRecord != null) {
            MqttConnection connection = new MqttConnection(this, createdRecord.serverURI, createdRecord.clientId, null, createdRecord.profileName);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setServerURIs(new String[]{createdRecord.serverURI});
            if(createdRecord.username != null && !createdRecord.username.trim().equals("")) {
                options.setUserName(createdRecord.username);
            }
            if(createdRecord.password != null && !createdRecord.password.trim().equals("")) {
                options.setPassword(createdRecord.password.toCharArray());
            }
            connection.setConnectOptions(options);
            connections.put(profileName, connection);
        }
        else{
            Log.w(TAG, "No database record found for profile = " + profileName);
        }
    }

    private void ProcessProfileDeletedAction(Bundle dataBundle){
        if(!validateBundle(dataBundle, Arrays.asList(TaskerMqttConstants.PROFILE_NAME_EXTRA))){
            Log.w(TAG, "invalid bundle for profile deleted action");
            return;
        }

        String profileName = dataBundle.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA);
        if(!validateExistingProfile(profileName)){
            return;
        }

        Log.d(TAG, "Profile deleted callback for profile name = " + profileName);

        disconnect(profileName, null, null);
        connections.remove(profileName);

        MqttSubscriptionRecord.deleteAll(MqttSubscriptionRecord.class, "profile_name = ?", profileName);
    }

    @Override
    public void disconnect(String clientHandle, String invocationContext, String activityToken) {
        if(mServiceStarted) {
            MqttConnection client = getConnection(clientHandle);
            client.disconnect(invocationContext, activityToken);
        }
    }

    @Override
    public void unsubscribe(String clientHandle, String topic, String invocationContext, String activityToken) {
        if(mServiceStarted) {
            super.unsubscribe(clientHandle, topic, invocationContext, activityToken);
        }
    }

    public void subscribe(String profileName, String topicFilter, int qos, String invocationContext, String activityToken){
        if(mServiceStarted) {
            MqttConnection client = getConnection(profileName);
            IMqttMessageListener messageListener = new MqttMessageListener(this, topicFilter, profileName);
            client.subscribe(topicFilter, qos, invocationContext, activityToken, messageListener);
        }
    }

    @Override
    public void callbackToActivity(String clientHandle, Status status,
                                   Bundle dataBundle) {
        dumpCallbackBundle(dataBundle);

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

        callbackToService(clientHandle, status, dataBundle);

        sendBroadcast(callbackIntent);
    }

    private void callbackToService(String profileName, Status status, Bundle dataBundle){
        String action = dataBundle.getString(MqttServiceConstants.CALLBACK_ACTION, "");
        boolean reconnect = dataBundle.getBoolean(MqttServiceConstants.CALLBACK_RECONNECT, false);
        boolean success = (status == Status.OK);
        switch(action){
            case MqttServiceConstants.CONNECT_EXTENDED_ACTION:
                if(reconnect){
                    onConnectCallback(profileName, success);
                }
                break;
            case TaskerMqttConstants.CONNECT_ACTION:
                onConnectCallback(profileName, success);
                break;
            case MqttServiceConstants.ON_CONNECTION_LOST_ACTION:
            case TaskerMqttConstants.DISCONNECT_ACTION:
                updateClientState(profileName, false);
                sendTaskerConnectionEvent(profileName, false);
                break;
            case TaskerMqttConstants.STOP_SERVICE_ACTION:
                onStopService(success);
                break;
        }
    }

    private void onConnectCallback(String profileName, boolean success){
        sendTaskerSynchronousResult(profileName, success);

        if(success && connections.containsKey(profileName)){
            updateClientState(profileName, true);
            sendTaskerConnectionEvent(profileName, true);
            List<MqttSubscriptionRecord> subscriptions = MqttSubscriptionRecord.find(MqttSubscriptionRecord.class, "profile_name = ?", profileName);
            for(MqttSubscriptionRecord record : subscriptions){
                subscribe(profileName, record.topic, record.qos, null, null);
            }
        }
    }

    private void onStopService(boolean success){
        if(success) {
            resetAllClientsConnectedState();
        }
    }

    private void resetAllClientsConnectedState(){
        Iterator<MqttConnectionProfileRecord> iter = MqttSubscriptionRecord.findAll(MqttConnectionProfileRecord.class);
        while(iter.hasNext()){
            updateClientState(iter.next().profileName, false);
        }
    }

    private Notification getServiceNotification(){
        Intent appIntent = new Intent(TaskerMqttConstants.OPEN_APP_INTENT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), SERVICE_NOTIF_ID, appIntent, 0);
        String notificationText = mNumConnectedProfiles + " Connected Profile";
        notificationText = (mNumConnectedProfiles != 1) ? notificationText + "s" : notificationText;

        return  new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("MQTT Subscriber Service")
                .setContentText(notificationText)
                .setContentIntent(pendingIntent)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setTicker("MQTT Subscriber Service")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(){
        if(mServiceStarted) {
            Notification serviceNotification = getServiceNotification();
            NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.notify(SERVICE_NOTIF_ID, serviceNotification);
        }
    }

    private void updateClientState(String profileName, boolean connected)
    {
        if(connected){
            mNumConnectedProfiles++;
            updateNotification();
        }
        else if(mNumConnectedProfiles > 0){
            mNumConnectedProfiles--;
            updateNotification();
        }
    }

    private void dumpCallbackBundle(Bundle dataBundle){
        if (dataBundle != null) {
            for (String key : dataBundle.keySet()) {
                Object value = dataBundle.get(key);
                if(value != null) {
                    Log.d(TAG, String.format("%s %s (%s)", key,
                            value.toString(), value.getClass().getName()));
                }
            }
        }
    }

    private boolean validateExistingProfile(String profileName){
        if(!connections.containsKey(profileName)) {
            Log.w(TAG, "Bundle contains an unknown profile name = " + profileName);
            return false;
        }
        return true;
    }

    private boolean validateBundle(Bundle bundle, List<String> requiredKeys){
        if(bundle == null){
            Log.w(TAG, "Bundle was null!");
            return false;
        }

        for(String requiredKey : requiredKeys){
            if(!bundle.keySet().contains(requiredKey)){
                Log.w(TAG, "Bundle missing required key " + requiredKey);
                return false;
            }
        }

        return true;
    }

    private void sendTaskerSynchronousResult(String profileName, boolean success){
        if(mSavedTaskerActionIntents.containsKey(profileName)) {
            int resultCode = success ? TaskerPlugin.Setting.RESULT_CODE_OK : TaskerPlugin.Setting.RESULT_CODE_FAILED;
            TaskerPlugin.Setting.signalFinish(this, mSavedTaskerActionIntents.get(profileName), resultCode, null);
            mSavedTaskerActionIntents.remove(profileName);
        }
    }

    private void sendTaskerConnectionEvent(String profileName, boolean isConnected){
        Bundle data = new Bundle();
        String action = isConnected ? TaskerMqttConstants.CONNECT_ACTION : TaskerMqttConstants.DISCONNECT_ACTION;
        data.putString(MqttServiceConstants.CALLBACK_ACTION, action);
        data.putString(TaskerMqttConstants.PROFILE_NAME_EXTRA, profileName);
        TaskerEventTrigger.triggerEvent(this, data, action);
    }

    private void notifyActionError(String profileName, String action, String error){
        Bundle resultBundle = new Bundle();
        MqttException exception = new MqttException(MqttException.REASON_CODE_CLIENT_EXCEPTION);
        resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE, error);
        resultBundle.putSerializable(MqttServiceConstants.CALLBACK_EXCEPTION, exception);
        resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION, action);
        this.callbackToActivity(profileName, Status.ERROR, resultBundle);
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
        private String profileName;

        MqttMessageListener(Context context, String topicFilter, String profileName)
        {
            this.topicFilter = topicFilter;
            this.context = context;
            this.profileName = profileName;
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.d(TAG, "Received message: " + message.toString() + " qos = " + message.getQos() + " topic = " + topic);

            Bundle data = new Bundle();
            data.putString(MqttServiceConstants.CALLBACK_ACTION, MqttServiceConstants.MESSAGE_ARRIVED_ACTION);
            data.putString(TaskerMqttConstants.PROFILE_NAME_EXTRA, this.profileName);
            data.putString(TaskerMqttConstants.TOPIC_EXTRA, topic);
            data.putString(TaskerMqttConstants.TOPIC_FILTER_EXTRA, this.topicFilter);
            data.putString(TaskerMqttConstants.MESSAGE_EXTRA, message.toString());
            data.putInt(TaskerMqttConstants.QOS_EXTRA, message.getQos());
            data.putBoolean(TaskerMqttConstants.DUPLICATE_EXTRA, message.isDuplicate());
            data.putBoolean(TaskerMqttConstants.RETAINED_EXTRA, message.isRetained());
            callbackToActivity(this.profileName, Status.OK, data);
            TaskerEventTrigger.triggerEvent(context, data, MqttServiceConstants.MESSAGE_ARRIVED_ACTION);
        }
    }
}
