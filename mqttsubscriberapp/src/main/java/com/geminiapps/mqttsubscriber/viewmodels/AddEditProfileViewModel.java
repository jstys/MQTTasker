package com.geminiapps.mqttsubscriber.viewmodels;

import android.app.Dialog;
import android.databinding.ObservableArrayList;

import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.views.AddEditProfileFragment;

/**
 * Created by jim.stys on 10/2/16.
 */

public class AddEditProfileViewModel {
    private Dialog dialog;
    private AddEditProfileFragment.IConnectionProfileAddedListener profileAddedListener;

    public AddEditProfileViewModel(Dialog dialog, AddEditProfileFragment.IConnectionProfileAddedListener profileAddedListener)
    {
        this.dialog = dialog;
        this.profileAddedListener = profileAddedListener;
    }

    public void saveConnectionProfile(MqttConnectionProfileModel model) {
        if (model != null && !model.getProfileName().isEmpty() && !model.getBrokerUri().isEmpty()) {
            model.save();
            this.profileAddedListener.onProfileAdded(model);
            this.dialog.dismiss();
        }
    }

    public void cancelConnectionProfile() {
        this.dialog.dismiss();
    }
}
