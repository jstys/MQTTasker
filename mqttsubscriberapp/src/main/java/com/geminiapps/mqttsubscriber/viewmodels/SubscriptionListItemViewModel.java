package com.geminiapps.mqttsubscriber.viewmodels;

import android.content.Context;

import com.geminiapps.mqttsubscriber.broadcast.MqttServiceSender;
import com.geminiapps.mqttsubscriber.models.MqttSubscriptionModel;


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
