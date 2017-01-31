package com.geminiapps.mqttsubscriber.views;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.ActivityTaskerMessageEventBinding;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.models.MqttSubscriptionModel;
import com.geminiapps.mqttsubscriber.tasker.TaskerBroadcastReceiver;
import com.geminiapps.mqttsubscriber.viewmodels.TaskerViewModel;

import org.eclipse.paho.android.service.tasker.TaskerMqttConstants;

import java.util.ArrayList;
import java.util.List;

public class TaskerMessageEventActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String ANY_SUBSCRIBED_TOPIC = "<Any>";

    private TaskerViewModel mViewModel;
    private ActivityTaskerMessageEventBinding mBinding;
    private List<String> mProfileNames;
    private List<String> mTopicNames;
    ArrayAdapter<String> mSubscriptionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent taskerIntent = getIntent();
        Bundle taskerExtras = taskerIntent.getBundleExtra(TaskerBroadcastReceiver.TASKER_DATA_BUNDLE);
        String selectedProfile = null;
        String selectedTopic = ANY_SUBSCRIBED_TOPIC;

        mProfileNames = new ArrayList<>();
        mTopicNames = new ArrayList<>();
        mViewModel = new TaskerViewModel(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tasker_message_event);
        mBinding.setViewModel(mViewModel);

        if(taskerExtras != null) {
            selectedProfile = taskerExtras.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA);
            selectedTopic = taskerExtras.getString(TaskerMqttConstants.TOPIC_FILTER_EXTRA, ANY_SUBSCRIBED_TOPIC);
        }

        mBinding.profileSpinner.setOnItemSelectedListener(this);

        loadSpinnerValues(selectedProfile, selectedTopic);
    }

    @Override
    public void finish() {
        this.mViewModel.saveMessageEventSettings();

        super.finish();
    }

    private void loadSpinnerValues(String selectedProfile, String selectedSubscription){
        List<MqttConnectionProfileModel> profiles = MqttConnectionProfileModel.findAll();
        int selectedProfileIndex = -1;
        int selectedSubscriptionIndex = 0;

        mTopicNames.add(ANY_SUBSCRIBED_TOPIC);

        for(int i = 0; i < profiles.size(); i++){
            MqttConnectionProfileModel profile = profiles.get(i);
            mProfileNames.add(profile.getProfileName());
            if(profile.getProfileName().equals(selectedProfile)){
                selectedProfileIndex = i;
            }
        }
        if(selectedProfileIndex < 0){
            selectedProfileIndex = 0;
        }

        // TODO: improve the view to be non-generic
        ArrayAdapter<String> profileAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mProfileNames);
        mSubscriptionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTopicNames);

        profileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSubscriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //TODO: fix issues with no profiles
        String profileInUse = selectedProfile == null ? mProfileNames.get(0) : selectedProfile;
        loadSubscriptionsForProfile(profileInUse, selectedSubscription);

        mBinding.profileSpinner.setAdapter(profileAdapter);
        mBinding.subscriptionSpinner.setAdapter(mSubscriptionAdapter);

        mBinding.profileSpinner.setSelection(selectedProfileIndex);
    }

    private void loadSubscriptionsForProfile(String profileName, String selectedSubscription){
        List<MqttSubscriptionModel> subscriptions = MqttSubscriptionModel.findAllForProfile(profileName);
        int selectedSubscriptionIndex = 0;

        mTopicNames.clear();

        mTopicNames.add(ANY_SUBSCRIBED_TOPIC);
        for(int i = 0; i < subscriptions.size(); i++){
            MqttSubscriptionModel subscription = subscriptions.get(i);
            mTopicNames.add(subscription.getTopic());
            if(selectedSubscription.equals(subscription.getTopic())){
                selectedSubscriptionIndex = i+1;
            }
        }

        mBinding.subscriptionSpinner.setSelection(selectedSubscriptionIndex);
        mSubscriptionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        loadSubscriptionsForProfile(mProfileNames.get(position), ANY_SUBSCRIBED_TOPIC);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public String getSelectedProfile(){
        return (String)mBinding.profileSpinner.getSelectedItem();
    }

    public String getSelectedSubscription(){
        return (String)mBinding.subscriptionSpinner.getSelectedItem();
    }
}
