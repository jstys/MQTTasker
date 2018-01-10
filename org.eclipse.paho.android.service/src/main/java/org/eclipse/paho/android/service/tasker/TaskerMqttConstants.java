package org.eclipse.paho.android.service.tasker;


public class TaskerMqttConstants {
    // Actions
    public static final String START_SERVICE_ACTION = "startService";
    public static final String STOP_SERVICE_ACTION = "stopService";
    public static final String QUERY_SERVICE_RUNNING_ACTION = "queryService";
    public static final String CONNECT_ACTION = "connect";
    public static final String DISCONNECT_ACTION = "disconnect";
    public static final String PUBLISH_ACTION = "publish";
    public static final String SUBSCRIBE_ACTION = "subscribe";
    public static final String UNSUBSCRIBE_ACTION = "unsubscribe";
    public static final String QUERY_TOPIC_ACTION = "queryTopic";
    public static final String QUERY_PROFILE_CONNECTED_ACTION = "queryProfileConnected";
    public static final String PROFILE_CREATED_ACTION = "profileCreated";
    public static final String PROFILE_DELETED_ACTION = "profileDeleted";
    public static final String SUBSCRIPTION_CREATED_ACTION = "subscriptionCreated";
    public static final String SUBSCRIPTION_DELETED_ACTION = "subscriptionDeleted";
    public static final String OPEN_APP_INTENT = "com.geminiapps.mqttsubscriber.OPEN_APP";

    // Other bundle data extras
    public static final String ACTION_EXTRA = "action";
    public static final String PROFILE_NAME_EXTRA = "profileName";
    public static final String TOPIC_EXTRA = "topic";
    public static final String QOS_EXTRA = "qos";
    public static final String TOPIC_FILTER_EXTRA = "topicFilter";
    public static final String MESSAGE_EXTRA = "message";
    public static final String RECONNECT_EXTRA = "reconnect";
    public static final String CLEAN_SESSION_EXTRA = "cleanSession";
    public static final String DUPLICATE_EXTRA = "isDuplicate";
    public static final String RETAINED_EXTRA = "isRetained";
    public static final String TASKER_TOPIC_FILTER = "taskerTopicFilter";
    public static final String TASKER_PROFILE_NAME = "taskerProfileName";
    public static final String TASKER_SAVED_SETTING_INTENT = "settingIntent";

    // Hard-coded client id
    public static final String TASKER_CLIENT_ID = "tasker_client";
}
