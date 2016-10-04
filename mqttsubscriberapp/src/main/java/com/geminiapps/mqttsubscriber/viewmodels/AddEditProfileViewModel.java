package com.geminiapps.mqttsubscriber.viewmodels;

import android.app.Dialog;
import android.databinding.ObservableArrayList;

import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;

/**
 * Created by jim.stys on 10/2/16.
 */

public class AddEditProfileViewModel {
    private Dialog dialog;
    private ObservableArrayList<MqttConnectionProfileModel> profileList;

    public AddEditProfileViewModel(Dialog dialog, ObservableArrayList<MqttConnectionProfileModel> profileList)
    {
        this.dialog = dialog;
        this.profileList = profileList;
    }

    public void saveConnectionProfile(MqttConnectionProfileModel model) {
        if (model != null && !model.getProfileName().isEmpty() && !model.getBrokerUri().isEmpty()) {
            long profileId = model.save();

            if (profileId >= 0) {
                this.profileList.add(model);
                this.dialog.dismiss();
            }
        }
    }

    public void cancelConnectionProfile() {
        this.dialog.dismiss();
    }
}
