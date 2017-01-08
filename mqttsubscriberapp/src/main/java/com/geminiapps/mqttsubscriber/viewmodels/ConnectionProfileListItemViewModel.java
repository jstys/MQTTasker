package com.geminiapps.mqttsubscriber.viewmodels;

import android.content.Context;
import android.content.Intent;

import com.geminiapps.mqttsubscriber.broadcast.MqttServiceSender;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.views.ConnectionDetailActivity;

/**
 * Created by jim.stys on 10/1/16.
 */

public class ConnectionProfileListItemViewModel {
    private Context mContext;

    public ConnectionProfileListItemViewModel(Context context){
        mContext = context;
    }

    public boolean profileLongClicked(MqttConnectionProfileModel model)
    {
        return true;
    }

    public void profileClicked(MqttConnectionProfileModel model)
    {
        Intent activityIntent = new Intent(mContext, ConnectionDetailActivity.class);
        activityIntent.putExtra("profile", model);
        mContext.startActivity(activityIntent);
    }
}
