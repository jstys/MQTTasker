package com.geminiapps.mqttsubscriber.viewmodels;

import android.content.Context;
import android.content.Intent;

import com.geminiapps.mqttsubscriber.broadcast.MqttServiceSender;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.models.MqttSubscriptionModel;
import com.geminiapps.mqttsubscriber.views.ConnectionDetailActivity;

/**
 * Created by jim.stys on 1/5/17.
 */

public class SubscriptionListItemViewModel {
    private MqttServiceSender mServiceSender;
    private Context mContext;

    public SubscriptionListItemViewModel(Context context){
        mServiceSender = new MqttServiceSender(context);
        mContext = context;
    }

    public void subscriptionClicked(MqttSubscriptionModel model)
    {
        mServiceSender.subscribeTopic(model.getProfileName(), model.getTopic(), model.getQos());
    }
}
