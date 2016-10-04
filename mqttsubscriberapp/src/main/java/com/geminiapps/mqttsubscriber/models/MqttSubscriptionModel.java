package com.geminiapps.mqttsubscriber.models;

import android.databinding.BaseObservable;

import org.eclipse.paho.android.service.MqttSubscriptionRecord;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jim.stys on 10/1/16.
 */

public class MqttSubscriptionModel extends BaseObservable {
    private String topic;
    private String clientId;
    private int qos;
    private List<MqttMessage> messages;

    public MqttSubscriptionModel(String topic, String clientId, int qos)
    {
        this.topic = topic;
        this.clientId = clientId;
        this.qos = qos;
        this.messages = new ArrayList<MqttMessage>();
    }

    public long save()
    {
        MqttSubscriptionRecord dbRecord = new MqttSubscriptionRecord(this.topic, this.clientId, this.qos);
        return dbRecord.save();
    }

    public static MqttSubscriptionModel find(String topic, String clientId)
    {
        List<MqttSubscriptionRecord> dbRecords = MqttSubscriptionRecord.find(MqttSubscriptionRecord.class, "topic = ? and profile = ?", topic, clientId);
        if(dbRecords.size() == 1)
        {
            MqttSubscriptionRecord dbRecord = dbRecords.get(0);
            return new MqttSubscriptionModel(dbRecord.topic, dbRecord.profile.clientID, dbRecord.qos);
        }
        return null;
    }

    public static List<MqttSubscriptionModel> findAll(String clientId)
    {
        List<MqttSubscriptionModel> models = new ArrayList<>();
        List<MqttSubscriptionRecord> dbRecords = MqttSubscriptionRecord.find(MqttSubscriptionRecord.class, "profile = ?", clientId);
        for(MqttSubscriptionRecord dbRecord : dbRecords)
        {
            models.add(new MqttSubscriptionModel(dbRecord.topic, clientId, dbRecord.qos));
        }
        return models;
    }
}
