package com.geminiapps.mqttsubscriber.viewmodels;

import android.app.Dialog;
import android.databinding.ObservableField;
import android.net.Uri;

import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.views.AddEditProfileFragment;

/**
 * Created by jim.stys on 10/2/16.
 */

public class AddEditProfileViewModel {
    private static final String TCP_PROTOCOL = "TCP";
    private static final String WS_PROTOCOL = "Websockets";

    private Dialog dialog;
    private AddEditProfileFragment.IConnectionProfileAddedListener profileAddedListener;
    private AddEditProfileFragment mView;

    public MqttConnectionProfileModel mModel;
    public final ObservableField<String> brokerHost = new ObservableField<>();
    public final ObservableField<String> brokerPort = new ObservableField<>();

    public AddEditProfileViewModel(Dialog dialog, AddEditProfileFragment.IConnectionProfileAddedListener profileAddedListener, AddEditProfileFragment view, MqttConnectionProfileModel model)
    {
        this.dialog = dialog;
        this.profileAddedListener = profileAddedListener;
        mView = view;
        mModel = (model == null) ? new MqttConnectionProfileModel() : model;
        if(model != null){
            Uri brokerUri = Uri.parse(mModel.getBrokerUri());
            brokerHost.set(brokerUri.getHost());
            brokerPort.set(String.valueOf(brokerUri.getPort()));
        }
    }

    public void saveConnectionProfile() {
        if (mModel != null && !mModel.getProfileName().isEmpty() && !brokerHost.get().isEmpty()) {
            mModel.setBrokerUri(buildBrokerURI());
            this.profileAddedListener.onProfileAdded(mModel);
            this.dialog.dismiss();
        }
    }

    public void cancelConnectionProfile() {
        this.dialog.dismiss();
    }

    private String buildBrokerURI(){
        String uri = "";
        String protocol = mView.getSelectedProtocol();
        if(protocol.equals(TCP_PROTOCOL)){
            uri += "tcp://";
        }
        else if(protocol.equals(WS_PROTOCOL)){
            uri += "ws://";
        }

        uri += brokerHost.get() + ":" + brokerPort.get();

        return uri;
    }
}
