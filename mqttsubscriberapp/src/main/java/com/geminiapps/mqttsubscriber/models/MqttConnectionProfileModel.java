package com.geminiapps.mqttsubscriber.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.databinding.library.baseAdapters.BR;

import org.eclipse.paho.android.service.MqttConnectionProfileRecord;
import org.eclipse.paho.android.service.TaskerMqttConstants;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jim.stys on 10/1/16.
 */

public class MqttConnectionProfileModel extends BaseObservable implements Parcelable {
    private String profileName;
    private String brokerUri;
    private String username;
    private String password;
    private boolean cleanSession;
    private boolean autoReconnect;
    private boolean isConnected;

    public MqttConnectionProfileModel()
    {
        this.profileName = "";
        this.brokerUri = "";
        this.username = "";
        this.password = "";
        this.cleanSession = false;
        this.autoReconnect = false;
    }

    public MqttConnectionProfileModel(String profileName, String brokerUri, String username, String password, boolean cleanSession, boolean autoReconnect) {
        this.profileName = profileName;
        this.brokerUri = brokerUri;
        this.username = username;
        this.password = password;
        this.cleanSession = cleanSession;
        this.autoReconnect = autoReconnect;
        this.isConnected = false;
    }

    @Bindable
    public String getProfileName() {
        return profileName;
    }

    @Bindable
    public String getBrokerUri() {
        return brokerUri;
    }

    @Bindable
    public String getUsername() {
        return username;
    }

    @Bindable
    public String getPassword() {
        return password;
    }

    @Bindable
    public boolean getCleanSession() {
        return cleanSession;
    }

    @Bindable
    public boolean getAutoReconnect() {
        return autoReconnect;
    }

    @Bindable
    public boolean getIsConnected() {
        return isConnected;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
        notifyPropertyChanged(BR.profileName);
    }

    public void setBrokerUri(String brokerUri) {
        this.brokerUri = brokerUri;
        notifyPropertyChanged(BR.brokerUri);
    }

    public void setUsername(String username) {
        this.username = username;
        notifyPropertyChanged(BR.username);
    }

    public void setPassword(String password) {
        this.password = password;
        notifyPropertyChanged(BR.password);
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
        notifyPropertyChanged(BR.cleanSession);
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        notifyPropertyChanged(BR.autoReconnect);
    }

    public void setIsConnected(boolean connected) {
        this.isConnected = connected;
        notifyPropertyChanged(BR.isConnected);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.profileName);
        dest.writeString(this.brokerUri);
        dest.writeString(this.username);
        dest.writeString(this.password);
        dest.writeValue(this.cleanSession);
        dest.writeValue(this.autoReconnect);
        dest.writeValue(this.isConnected);
    }

    public long save() {
        MqttConnectionProfileRecord dbRecord = new MqttConnectionProfileRecord(this.profileName, this.brokerUri, this.username, this.password, this.cleanSession, this.autoReconnect);
        return dbRecord.save();
    }

    public static MqttConnectionProfileModel find(String profileName) {
        List<MqttConnectionProfileRecord> dbRecords = MqttConnectionProfileRecord.find(MqttConnectionProfileRecord.class, "clientId = ?", profileName);
        if (dbRecords.size() == 1) {
            MqttConnectionProfileRecord dbRecord = dbRecords.get(0);
            return new MqttConnectionProfileModel(dbRecord.clientID, dbRecord.serverURI, dbRecord.username, dbRecord.password, dbRecord.cleanSession, dbRecord.autoReconnect);
        }
        return null;
    }

    public static List<MqttConnectionProfileModel> findAll()
    {
        List<MqttConnectionProfileModel> models = new ArrayList<MqttConnectionProfileModel>();
        Iterator<MqttConnectionProfileRecord> iter = MqttConnectionProfileRecord.findAll(MqttConnectionProfileRecord.class);
        while(iter.hasNext())
        {
            MqttConnectionProfileRecord dbRecord = iter.next();
            models.add(new MqttConnectionProfileModel(dbRecord.clientID, dbRecord.serverURI, dbRecord.username, dbRecord.password, dbRecord.cleanSession, dbRecord.autoReconnect));
        }

        return models;
    }
}
