package org.eclipse.paho.android.service;

import com.orm.SugarRecord;

import java.util.List;

public class MqttSubscriptionRecord extends SugarRecord {
    private static ISubscriptionRecordListener dbListener;

    public String topic;
    public String profileName;
    public int qos;

    public static void setSubscriptionRecordListener(ISubscriptionRecordListener listener){
        dbListener = listener;
    }

    public MqttSubscriptionRecord(){}

    public MqttSubscriptionRecord(String topic, String profileName, int qos)
    {
        this.topic = topic;
        List<MqttConnectionProfileRecord> dbRecords = MqttConnectionProfileRecord.find(MqttConnectionProfileRecord.class, "profile_name = ?", profileName);
        this.profileName = dbRecords.get(0).profileName;
        this.qos = qos;
    }

    public long updateFromModel(int qos){
        this.qos = qos;
        return save();
    }

    public static MqttSubscriptionRecord findOne(String profileName, String topic){
        List<MqttSubscriptionRecord> records = MqttSubscriptionRecord.find(MqttSubscriptionRecord.class, "profile_name = ? and topic = ?", profileName, topic);
        return records.size() == 1 ? records.get(0) : null;
    }

    @Override
    public long save() {
        boolean update = (getId() != null);
        long id = super.save();

        if(update && dbListener != null){
            dbListener.onSubscriptionUpdated(this);
        }
        else if(id >= 0 && dbListener != null){
            dbListener.onSubscriptionCreated(this);
        }

        return id;
    }

    @Override
    public boolean delete() {
        if(dbListener != null){
            dbListener.onSubscriptionDeleted(this);
        }

        return super.delete();
    }

    public interface ISubscriptionRecordListener{
        void onSubscriptionCreated(MqttSubscriptionRecord record);
        void onSubscriptionUpdated(MqttSubscriptionRecord record);
        void onSubscriptionDeleted(MqttSubscriptionRecord record);
    }
}
