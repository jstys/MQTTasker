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
    public MqttConnectionProfileRecord profile;
    public int qos;

    public MqttSubscriptionRecord(){}

    public MqttSubscriptionRecord(String topic, MqttConnectionProfileRecord record, int qos)
    {
        this.topic = topic;
        this.profile = record;
        this.qos = qos;
    }

    public MqttSubscriptionRecord(String topic, String clientId, int qos)
    {
        this.topic = topic;
        List<MqttConnectionProfileRecord> dbRecords = MqttConnectionProfileRecord.find(MqttConnectionProfileRecord.class, "clientId = ?", clientId);
        this.profile = dbRecords.get(0);
        this.qos = qos;
    }
}
