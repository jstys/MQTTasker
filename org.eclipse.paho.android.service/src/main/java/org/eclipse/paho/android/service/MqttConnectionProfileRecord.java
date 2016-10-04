package org.eclipse.paho.android.service;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

import java.util.List;

/**
 * Created by jim.stys on 9/28/16.
 */

public class MqttConnectionProfileRecord extends SugarRecord{
    @Unique
    public String clientID;
    public String serverURI;
    public String username;
    public String password;
    public boolean autoReconnect;
    public boolean cleanSession;

    public MqttConnectionProfileRecord(){}

    public MqttConnectionProfileRecord(String clientID, String serverURI, String username, String password, boolean autoReconnect, boolean cleanSession)
    {
        this.clientID = clientID;
        this.serverURI = serverURI;
        this.username = username;
        this.password = password;
        this.autoReconnect = autoReconnect;
        this.cleanSession = cleanSession;
    }

    public List<MqttSubscriptionRecord> getSubscriptions()
    {
        return MqttSubscriptionRecord.find(MqttSubscriptionRecord.class, "profile = ?", Long.toString(getId()));
    }
}
