package com.geminiapps.mqttsubscriber.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.models.MqttSubscriptionModel;
import com.geminiapps.mqttsubscriber.tasker.TaskerBroadcastReceiver;
import com.geminiapps.mqttsubscriber.viewmodels.TaskerViewModel;

import java.util.ArrayList;
import java.util.List;

public class TaskerMessageEventActivity extends AppCompatActivity {

    private TaskerViewModel mViewModel;
    //private ActivityTaskerMessageEventBinding mBinding;
    private List<String> mProfileNames;
    private List<String> mTopicNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent taskerIntent = getIntent();
        Bundle taskerExtras = taskerIntent.getBundleExtra(TaskerBroadcastReceiver.TASKER_DATA_BUNDLE);

        mProfileNames = new ArrayList<>();
        mTopicNames = new ArrayList<>();
        mViewModel = new TaskerViewModel(this);
        //mBinding = DataBindingUtil.setContentView(this, R.layout.);
        //mBinding.setViewModel(mViewModel);

        if(taskerExtras != null) {

        }

        //loadSpinnerValues();
    }

    private void loadSpinnerValues(String selectedProfile, String selectedSubscription){
        List<MqttConnectionProfileModel> profiles = MqttConnectionProfileModel.findAll();
        List<MqttSubscriptionModel> subscriptions = MqttSubscriptionModel.findAll();
        int selectedProfileIndex = -1;
        int selectedSubscriptionIndex = 0;

        mTopicNames.add("<Any>");

        for(int i = 0; i < profiles.size(); i++){
            MqttConnectionProfileModel profile = profiles.get(i);
            mProfileNames.add(profile.getProfileName());
            if(profile.getProfileName().equals(selectedProfile)){
                selectedProfileIndex = i;
            }
        }

        for(int i = 0; i < subscriptions.size(); i++){
            MqttSubscriptionModel subscription = subscriptions.get(i);
            mTopicNames.add(subscription.getTopic());
            if(subscription.getProfileName().equals(selectedProfile) && subscription.getTopic().equals(selectedSubscription)){
                selectedSubscriptionIndex = i;
            }
        }

        // TODO: improve the view to be non-generic
        ArrayAdapter<String> profileAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mProfileNames);
        ArrayAdapter<String> subscriptionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());

        profileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subscriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

//        mBinding.profileSpinner.setAdapter(profileAdapter);
//        mBinding.subscriptionSpinner.setAdapter(subscriptionAdapter);
//
//        if(selectedProfileIndex >= 0)
//        {
//            this.binding.profileSpinner.setSelection(selectedProfileIndex);
//        }
//
//        if(selectedSubscriptionIndex > 0){
//            this.binding.subscriptionSpinner.setSelected(selectedSubscriptionIndex);
//        }
    }
}
