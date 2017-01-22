package org.eclipse.paho.android.service;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

import java.util.List;

/**
 * Created by jim.stys on 9/28/16.
 */

public class MqttConnectionProfileRecord extends SugarRecord{
    private static IConnectionProfileRecordListener dbListener;

    @Unique
    public String profileName;
    public String clientId;
    public String serverURI;
    public String username;
    public String password;
    public boolean autoReconnect;
    public boolean cleanSession;
    public boolean connected;

    public static void setConnectionProfileRecordListener(IConnectionProfileRecordListener listener){
        dbListener = listener;
    }

    public MqttConnectionProfileRecord(){}

    public MqttConnectionProfileRecord(String profileName, String clientId, String serverURI, String username, String password, boolean cleanSession, boolean autoReconnect)
    {
        this.profileName = profileName;
        this.clientId = clientId;
        this.serverURI = serverURI;
        this.username = username;
        this.password = password;
        this.autoReconnect = autoReconnect;
        this.cleanSession = cleanSession;
        this.connected = false;
    }

    public long updateFromModel(String clientId, String serverURI, String username, String password){
        this.clientId = clientId;
        this.serverURI = serverURI;
        this.username = username;
        this.password = password;
        return save();
    }

    public static MqttConnectionProfileRecord findOne(String profileName){
        List<MqttConnectionProfileRecord> records = MqttConnectionProfileRecord.find(MqttConnectionProfileRecord.class, "profile_name = ?", profileName);
        return records.size() == 1 ? records.get(0) : null;
    }

    public List<MqttSubscriptionRecord> getSubscriptions()
    {
        return MqttSubscriptionRecord.find(MqttSubscriptionRecord.class, "profile = ?", Long.toString(getId()));
    }

    @Override
    public long save() {
        boolean update = (getId() != null);
        long id = super.save();

        if(update && dbListener != null){
            dbListener.onConnectionProfileUpdated(this);
        }
        else if(id >= 0 && dbListener != null){
            dbListener.onConnectionProfileCreated(this);
        }

        return id;
    }

    @Override
    public boolean delete() {
        if(dbListener != null){
            dbListener.onConnectionProfileDeleted(this);
        }

        return super.delete();
    }

    public interface IConnectionProfileRecordListener{
        void onConnectionProfileCreated(MqttConnectionProfileRecord record);
        void onConnectionProfileUpdated(MqttConnectionProfileRecord record);
        void onConnectionProfileDeleted(MqttConnectionProfileRecord record);
    }
}
