package com.geminiapps.mqttsubscriber.viewmodels;

import android.app.Dialog;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;

import com.geminiapps.mqttsubscriber.R;
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

    public final ObservableField<String> brokerHost = new ObservableField<>();
    public final ObservableField<String> brokerPort = new ObservableField<>();

    public AddEditProfileViewModel(Dialog dialog, AddEditProfileFragment.IConnectionProfileAddedListener profileAddedListener, AddEditProfileFragment view)
    {
        this.dialog = dialog;
        this.profileAddedListener = profileAddedListener;
        mView = view;
    }

    public void saveConnectionProfile(MqttConnectionProfileModel model) {
        if (model != null && !model.getProfileName().isEmpty() && !brokerHost.get().isEmpty()) {
            model.setBrokerUri(buildBrokerURI());
            model.save();
            this.profileAddedListener.onProfileAdded(model);
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
