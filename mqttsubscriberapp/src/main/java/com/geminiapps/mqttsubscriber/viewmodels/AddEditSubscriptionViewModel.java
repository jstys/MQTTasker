package com.geminiapps.mqttsubscriber.viewmodels;

import android.app.Dialog;

import com.geminiapps.mqttsubscriber.models.MqttSubscriptionModel;
import com.geminiapps.mqttsubscriber.views.AddEditSubscriptionFragment;

/**
 * Created by jim.stys on 1/5/17.
 */

public class AddEditSubscriptionViewModel {
    private Dialog dialog;
    private AddEditSubscriptionFragment.ISubscriptionAddedListener subscriptionAddedListener;

    public AddEditSubscriptionViewModel(Dialog dialog, AddEditSubscriptionFragment.ISubscriptionAddedListener subscriptionAddedListener)
    {
        this.dialog = dialog;
        this.subscriptionAddedListener = subscriptionAddedListener;
    }

    public void saveSubscription(MqttSubscriptionModel model) {
        if (model != null && !model.getTopic().isEmpty()) {
            model.save();
            this.subscriptionAddedListener.onSubscriptionAdded(model);
            this.dialog.dismiss();
        }
    }

    public void cancelSubscription() {
        this.dialog.dismiss();
    }
}
