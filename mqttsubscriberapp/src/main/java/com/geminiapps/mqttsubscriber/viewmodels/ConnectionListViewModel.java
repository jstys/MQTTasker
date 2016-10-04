package com.geminiapps.mqttsubscriber.viewmodels;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;

/**
 * Created by jim.stys on 10/1/16.
 */

public class ConnectionListViewModel {
    private RecyclerView recyclerView;

    public boolean profileLongClicked(MqttConnectionProfileModel model)
    {
        Log.d("RecyclerView", "Long clicked row " + model.getProfileName());
        return true;
    }

    public void profileClicked(MqttConnectionProfileModel model)
    {
        Log.d("RecyclerView", "clicked row " + model.getProfileName());
    }
}
