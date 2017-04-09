package com.geminiapps.mqttsubscriber.viewmodels;

import android.app.Dialog;
import android.widget.Toast;

import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.views.ConnectToProfileWithOptionsFragment;

public class ConnectOptionsViewModel {

    private Dialog mDialog;
    private ConnectToProfileWithOptionsFragment.IConnectActionListener mConnectListener;
    private ConnectToProfileWithOptionsFragment mView;

    public ConnectOptionsViewModel(Dialog dialog, ConnectToProfileWithOptionsFragment.IConnectActionListener connectListener, ConnectToProfileWithOptionsFragment view){
        mDialog = dialog;
        mConnectListener = connectListener;
        mView = view;
    }

    public void connectWithOptions(MqttConnectionProfileModel profile){
        if(profile == null){
            Toast.makeText(mView.getContext(), "Error reading profile from database", Toast.LENGTH_SHORT).show();
            mDialog.dismiss();
            return;
        }

        profile.setAutoReconnect(mView.isAutoReconnectEnabled());
        profile.setCleanSession(mView.isCleanSessionEnabled());

        mConnectListener.onConnectToProfile(profile);
        mDialog.dismiss();
    }

    public void cancelConnect(){
        mDialog.dismiss();
    }
}
