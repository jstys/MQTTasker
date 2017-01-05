package com.geminiapps.mqttsubscriber.viewmodels;

import android.content.Context;
import android.databinding.Bindable;
import android.databinding.ObservableField;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.broadcast.MqttServiceListener;
import com.geminiapps.mqttsubscriber.broadcast.MqttServiceReceiver;
import com.geminiapps.mqttsubscriber.broadcast.MqttServiceSender;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;

/**
 * Created by jim.stys on 1/4/17.
 */

public class ConnectionDetailViewModel extends MqttServiceListener{
    private Context mContext;
    private MqttServiceReceiver mReceiver;
    private MqttServiceSender mSender;
    private MqttConnectionProfileModel mModel;

    public final ObservableField<String> connectionState = new ObservableField<String>();

    public ConnectionDetailViewModel(Context context, MqttConnectionProfileModel model){
        this.mContext = context;
        this.mReceiver = new MqttServiceReceiver(this, mContext);
        this.mSender = new MqttServiceSender(mContext);
        this.mModel = model;

        connectionState.set(getConnectionStateText());
    }

    public boolean onMenuClick(int itemId){
        switch(itemId){
            case R.id.profile_connect_menuitem:
                return handleConnectionAction();
            default:
                return true;
        }
    }

    public void onStop(){
        mReceiver.unregister();
    }

    public void onStart(){
        mReceiver.register();
    }

    public String getConnectionStateText(){
        if(mModel.getIsConnecting()){
            if(mModel.getIsConnected()){
                return "Disconnecting...";
            }
            else{
                return "Connecting...";
            }
        }
        else{
            if(mModel.getIsConnected()){
                return "Connected";
            }
            else{
                return "Disconnected";
            }
        }
    }

    @Override
    protected void onClientConnectResponse(String clientId, boolean success, String error) {
        mModel.setIsConnecting(false);
        if(success){
            mModel.setIsConnected(true);
        }
        else{
            mModel.setIsConnected(false);
        }
        connectionState.set(getConnectionStateText());
    }

    @Override
    protected void onClientDisconnectResponse(String clientId, boolean success) {
        mModel.setIsConnecting(false);
        if(success){
            mModel.setIsConnected(false);
        }
        else{
            mModel.setIsConnected(true);
        }
        connectionState.set(getConnectionStateText());
    }

    private boolean handleConnectionAction(){
        if(this.mModel.getIsConnecting()){
            return false;
        }

        if(this.mModel.getIsConnected()) {
            mModel.setIsConnecting(true);
            this.mSender.disconnectFromBroker(mModel.getProfileName());
        }
        else{
            mModel.setIsConnecting(true);
            this.mSender.connectToBroker(mModel.getProfileName());
        }
        connectionState.set(getConnectionStateText());
        return true;
    }
}
