package com.geminiapps.mqttsubscriber.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.databinding.library.baseAdapters.BR;

import org.eclipse.paho.android.service.MqttSubscriptionRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jim.stys on 10/1/16.
 */

public class MqttSubscriptionModel extends BaseObservable implements Parcelable {
    private String topic;
    private String profileName;
    private int qos;

    public MqttSubscriptionModel(String profileName){
        this.topic = "";
        this.profileName = profileName;
        this.qos = 0;
    }

    public MqttSubscriptionModel(String topic, String profileName, int qos)
    {
        this.topic = topic;
        this.profileName = profileName;
        this.qos = qos;
    }

    public MqttSubscriptionModel(Parcel source) {
        this.topic = source.readString();
        this.profileName = source.readString();
        this.qos = source.readInt();
    }

    @Bindable
    public String getTopic(){
        return this.topic;
    }

    public String getProfileName(){
        return this.profileName;
    }

    @Bindable
    public int getQos(){
        return this.qos;
    }

    public void setTopic(String topic){
        this.topic = topic;
        notifyPropertyChanged(BR.topic);
    }

    public void setQos(int qos){
        this.qos = qos;
        notifyPropertyChanged(BR.qos);
    }

    public long save()
    {
        MqttSubscriptionRecord dbRecord = new MqttSubscriptionRecord(this.topic, this.profileName, this.qos);
        return dbRecord.save();
    }

    public long update(){
        MqttSubscriptionRecord dbRecord = MqttSubscriptionRecord.findOne(this.profileName, this.topic);
        if(dbRecord != null){
            return dbRecord.updateFromModel(this.qos);
        }
        return -1;
    }

    public boolean delete() {
        MqttSubscriptionRecord dbRecord = MqttSubscriptionRecord.findOne(this.profileName, this.topic);
        if(dbRecord != null){
            dbRecord.delete();
            return true;
        }
        return false;
    }

    public static MqttSubscriptionModel find(String topic, String profileName)
    {
        List<MqttSubscriptionRecord> dbRecords = MqttSubscriptionRecord.find(MqttSubscriptionRecord.class, "topic = ? and profile_name = ?", topic, profileName);
        if(dbRecords.size() == 1)
        {
            MqttSubscriptionRecord dbRecord = dbRecords.get(0);
            return new MqttSubscriptionModel(dbRecord.topic, dbRecord.profileName, dbRecord.qos);
        }
        return null;
    }

    public static List<MqttSubscriptionModel> findAllForProfile(String profileName)
    {
        List<MqttSubscriptionModel> models = new ArrayList<>();
        List<MqttSubscriptionRecord> dbRecords = MqttSubscriptionRecord.find(MqttSubscriptionRecord.class, "profile_name = ?", profileName);
        for(MqttSubscriptionRecord dbRecord : dbRecords)
        {
            models.add(new MqttSubscriptionModel(dbRecord.topic, profileName, dbRecord.qos));
        }
        return models;
    }

    public static List<MqttSubscriptionModel> findAll(){
        List<MqttSubscriptionModel> models = new ArrayList<>();
        Iterator<MqttSubscriptionRecord> dbRecords = MqttSubscriptionRecord.findAll(MqttSubscriptionRecord.class);
        while(dbRecords.hasNext())
        {
            MqttSubscriptionRecord dbRecord = dbRecords.next();
            models.add(new MqttSubscriptionModel(dbRecord.topic, dbRecord.profileName, dbRecord.qos));
        }
        return models;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.topic);
        dest.writeString(this.profileName);
        dest.writeInt(this.qos);
    }

    public static final Parcelable.Creator<MqttSubscriptionModel> CREATOR = new Parcelable.Creator<MqttSubscriptionModel>(){

        @Override
        public MqttSubscriptionModel createFromParcel(Parcel source) {
            return new MqttSubscriptionModel(source);
        }

        @Override
        public MqttSubscriptionModel[] newArray(int size) {
            return new MqttSubscriptionModel[0];
        }
    };
}
