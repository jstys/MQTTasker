package org.eclipse.paho.android.service;

/**
 * Created by jim.stys on 9/25/16.
 */

public class TaskerMqttConstants {
    // Actions
    public static final String START_SERVICE_ACTION = "startService";
    public static final String STOP_SERVICE_ACTION = "stopService";
    public static final String CONNECT_ACTION = "connect";
    public static final String DISCONNECT_ACTION = "disconnect";
    public static final String SUBSCRIBE_ACTION = "subscribe";
    public static final String UNSUBSCRIBE_ACTION = "unsubscribe";

    // Other bundle data extras
    public static final String ACTION_EXTRA = "action";
    public static final String PROFILE_NAME_EXTRA = "profileName";
    public static final String TOPIC_EXTRA = "topic";
    public static final String QOS_EXTRA = "qos";
    public static final String TOPIC_FILTER_EXTRA = "topicFilter";
    public static final String MESSAGE_EXTRA = "message";

    // Hard-coded client id
    public static final String TASKER_CLIENT_ID = "tasker_client";
}
