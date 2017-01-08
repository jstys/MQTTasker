package com.geminiapps.mqttsubscriber.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.databinding.library.baseAdapters.BR;

import org.eclipse.paho.android.service.MqttConnectionProfileRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jim.stys on 10/1/16.
 */

public class MqttConnectionProfileModel extends BaseObservable implements Parcelable {
    private String profileName;
    private String clientId;
    private String brokerUri;
    private String username;
    private String password;
    private boolean cleanSession;
    private boolean autoReconnect;
    private boolean isConnected;
    private boolean isConnecting;

    public MqttConnectionProfileModel()
    {
        this.profileName = "";
        this.clientId = "";
        this.brokerUri = "";
        this.username = "";
        this.password = "";
        this.cleanSession = false;
        this.autoReconnect = false;
        this.isConnected = false;
        this.isConnecting = false;
    }

    public MqttConnectionProfileModel(Parcel in){
        this.profileName = in.readString();
        this.clientId = in.readString();
        this.brokerUri = in.readString();
        this.username = in.readString();
        this.password = in.readString();
        this.cleanSession = (Boolean)in.readValue(null);
        this.autoReconnect = (Boolean)in.readValue(null);
        this.isConnected = (Boolean)in.readValue(null);
        this.isConnecting = (Boolean)in.readValue(null);
    }

    public MqttConnectionProfileModel(String profileName, String clientId, String brokerUri, String username, String password, boolean cleanSession, boolean autoReconnect) {
        this.profileName = profileName;
        this.clientId = clientId;
        this.brokerUri = brokerUri;
        this.username = username;
        this.password = password;
        this.cleanSession = cleanSession;
        this.autoReconnect = autoReconnect;
        this.isConnected = false;
        this.isConnecting = false;
    }

    public MqttConnectionProfileModel(String profileName, String clientId, String brokerUri, String username, String password, boolean cleanSession, boolean autoReconnect, boolean isConnected) {
        this.profileName = profileName;
        this.clientId = clientId;
        this.brokerUri = brokerUri;
        this.username = username;
        this.password = password;
        this.cleanSession = cleanSession;
        this.autoReconnect = autoReconnect;
        this.isConnected = isConnected;
        this.isConnecting = false;
    }

    @Bindable
    public String getProfileName() {
        return profileName;
    }

    @Bindable
    public String getClientId() { return clientId; }

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

    @Bindable
    public boolean getIsConnecting() { return isConnecting; }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
        notifyPropertyChanged(BR.profileName);
    }

    public void setClientId(String clientId){
        this.clientId = clientId;
        notifyPropertyChanged(BR.clientId);
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

    public void setIsConnecting(boolean connecting) {
        this.isConnecting = connecting;
        notifyPropertyChanged(BR.isConnecting);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.profileName);
        dest.writeString(this.clientId);
        dest.writeString(this.brokerUri);
        dest.writeString(this.username);
        dest.writeString(this.password);
        dest.writeValue(this.cleanSession);
        dest.writeValue(this.autoReconnect);
        dest.writeValue(this.isConnected);
        dest.writeValue(this.isConnecting);
    }

    public long save() {
        MqttConnectionProfileRecord dbRecord = new MqttConnectionProfileRecord(this.profileName, this.clientId, this.brokerUri, this.username, this.password, this.cleanSession, this.autoReconnect);
        return dbRecord.save();
    }

    public static MqttConnectionProfileModel find(String profileName) {
        List<MqttConnectionProfileRecord> dbRecords = MqttConnectionProfileRecord.find(MqttConnectionProfileRecord.class, "client_id = ?", profileName);
        if (dbRecords.size() == 1) {
            MqttConnectionProfileRecord dbRecord = dbRecords.get(0);
            return new MqttConnectionProfileModel(dbRecord.profileName, dbRecord.clientId, dbRecord.serverURI, dbRecord.username, dbRecord.password, dbRecord.cleanSession, dbRecord.autoReconnect, dbRecord.connected);
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
            models.add(new MqttConnectionProfileModel(dbRecord.profileName, dbRecord.clientId, dbRecord.serverURI, dbRecord.username, dbRecord.password, dbRecord.cleanSession, dbRecord.autoReconnect, dbRecord.connected));
        }

        return models;
    }

    public static final Parcelable.Creator<MqttConnectionProfileModel> CREATOR = new Parcelable.Creator<MqttConnectionProfileModel>(){

        @Override
        public MqttConnectionProfileModel createFromParcel(Parcel source) {
            return new MqttConnectionProfileModel(source);
        }

        @Override
        public MqttConnectionProfileModel[] newArray(int size) {
            return new MqttConnectionProfileModel[0];
        }
    };
}
