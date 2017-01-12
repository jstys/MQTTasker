package com.geminiapps.mqttsubscriber.viewmodels;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.adapters.SubscriptionListAdapter;
import com.geminiapps.mqttsubscriber.broadcast.MqttServiceListener;
import com.geminiapps.mqttsubscriber.broadcast.MqttServiceReceiver;
import com.geminiapps.mqttsubscriber.broadcast.MqttServiceSender;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.models.MqttSubscriptionModel;
import com.geminiapps.mqttsubscriber.views.AddEditSubscriptionFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jim.stys on 1/4/17.
 */

public class ConnectionDetailViewModel extends MqttServiceListener implements AddEditSubscriptionFragment.ISubscriptionAddedListener{
    private Context mContext;
    private MqttServiceReceiver mReceiver;
    private MqttServiceSender mSender;
    private MqttConnectionProfileModel mModel;
    private ObservableArrayList<MqttSubscriptionModel> mSubscriptionList;
    private Map<String, Integer> mSubscriptionTopics;

    public final ObservableField<String> connectionState = new ObservableField<String>();

    public ConnectionDetailViewModel(Context context, MqttConnectionProfileModel model){
        mContext = context;
        mReceiver = new MqttServiceReceiver(this, mContext);
        mSender = new MqttServiceSender(mContext);
        mModel = model;
        mSubscriptionTopics = new HashMap<>();
        mSubscriptionList = new ObservableArrayList<>();
        connectionState.set(getConnectionStateText());
    }

    public boolean onMenuClick(int itemId){
        switch(itemId){
            case R.id.profile_connect_menuitem:
                return handleConnectionAction();
            default:
                return true;
        }
    }

    public void onStop(){
        mReceiver.unregister();
    }

    public void onStart(){
        mReceiver.register();
        for(MqttSubscriptionModel subscription : MqttSubscriptionModel.findAll(mModel.getProfileName())){
            addOrUpdateSubscription(subscription);
        }

    }

    public ObservableArrayList<MqttSubscriptionModel> getSubscriptions(){
        return mSubscriptionList;
    }

    @BindingAdapter("app:subscriptionItems")
    public  static void bindList(ListView view, ObservableArrayList<MqttSubscriptionModel> list) {
        view.setAdapter(new SubscriptionListAdapter(view.getContext(), list));
    }

    private String getConnectionStateText(){
        if(mModel.getIsConnecting()){
            if(mModel.getIsConnected()){
                return "Disconnecting...";
            }
            else{
                return "Connecting...";
            }
        }
        else{
            if(mModel.getIsConnected()){
                return "Connected";
            }
            else{
                return "Disconnected";
            }
        }
    }

    @Override
    protected void onClientConnectResponse(String profileName, String clientId, boolean success, String error) {
        mModel.setIsConnecting(false);
        if(success){
            mModel.setIsConnected(true);
        }
        else{
            mModel.setIsConnected(false);
        }
        connectionState.set(getConnectionStateText());
    }

    @Override
    protected void onClientDisconnectResponse(String profileName, String clientId, boolean success) {
        mModel.setIsConnecting(false);
        mModel.setIsConnected(false);

        connectionState.set(getConnectionStateText());
    }

    @Override
    protected void onClientSubscribeResponse(String profileName, String topicFilter, boolean success) {
        Toast.makeText(this.mContext, "Successfully subscribed to topic filter = " + topicFilter, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onMessageArrived(String profileName, String topicFilter, String topic, String message, int qos) {
        Toast.makeText(this.mContext, "Received message = " + message + " with qos = " + qos + " and topic = " + topic, Toast.LENGTH_SHORT).show();
    }

    private boolean handleConnectionAction(){
        if(this.mModel.getIsConnecting()){
            return false;
        }

        if(this.mModel.getIsConnected()) {
            mModel.setIsConnecting(true);
            this.mSender.disconnectFromBroker(mModel.getProfileName());
        }
        else{
            mModel.setIsConnecting(true);
            this.mSender.connectToBroker(mModel.getProfileName());
        }
        connectionState.set(getConnectionStateText());
        return true;
    }

    public void addEditSubscription(int index)
    {
        Bundle subscription = new Bundle();
        FragmentManager fm = ((AppCompatActivity)mContext).getSupportFragmentManager();
        AddEditSubscriptionFragment dialog = new AddEditSubscriptionFragment();

        if(index >= 0 && index < mSubscriptionList.size())
        {
            subscription.putParcelable("subscription", mSubscriptionList.get(index));
        }
        subscription.putString("profile", mModel.getProfileName());
        dialog.setArguments(subscription);
        dialog.show(fm, "add_edit_subscription_fragment");
    }

    @Override
    public void onSubscriptionAdded(MqttSubscriptionModel model) {
        boolean newTopic = true;

        addOrUpdateSubscription(model);

        //TODO: move this logic to the service side
        if(!newTopic){
            mSender.unsubscribeTopic(mModel.getProfileName(), model.getTopic());
        }

        mSender.subscribeTopic(mModel.getProfileName(), model.getTopic(), model.getQos());
    }

    private void addOrUpdateSubscription(MqttSubscriptionModel model){
        if (mSubscriptionTopics.containsKey(model.getTopic())) {
            mSubscriptionList.set(mSubscriptionTopics.get(model.getTopic()), model);
        }
        else {
            mSubscriptionList.add(model);
            mSubscriptionTopics.put(model.getTopic(), mSubscriptionList.size()-1);
        }
    }
}
