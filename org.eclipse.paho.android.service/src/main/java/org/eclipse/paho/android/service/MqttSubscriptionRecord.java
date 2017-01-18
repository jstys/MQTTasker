package org.eclipse.paho.android.service;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

import java.util.List;

/**
 * Created by jim.stys on 10/1/16.
 */

public class MqttSubscriptionRecord extends SugarRecord {
    @Unique
    public String topic;
    public String profileName;
    public int qos;

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
}
