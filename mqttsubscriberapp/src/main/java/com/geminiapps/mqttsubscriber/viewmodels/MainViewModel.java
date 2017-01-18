package com.geminiapps.mqttsubscriber.viewmodels;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.adapters.ConnectionProfileListAdapter;
import com.geminiapps.mqttsubscriber.broadcast.MqttServiceListener;
import com.geminiapps.mqttsubscriber.broadcast.MqttServiceReceiver;
import com.geminiapps.mqttsubscriber.broadcast.MqttServiceSender;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.views.AddEditProfileFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jim.stys on 10/1/16.
 */

public class MainViewModel extends MqttServiceListener implements AddEditProfileFragment.IConnectionProfileAddedListener {
    private Context viewContext;
    private MqttServiceReceiver receiver;
    private MqttServiceSender sender;
    private boolean serviceRunning;
    private ObservableArrayList<MqttConnectionProfileModel> connectionProfiles;
    private Map<String, Integer> connectionProfileNames;

    public MainViewModel(Context context)
    {
        this.viewContext = context;
        this.connectionProfiles = new ObservableArrayList<>();
        this.connectionProfileNames = new HashMap<>();
        this.receiver = new MqttServiceReceiver(this, this.viewContext);
        this.sender = new MqttServiceSender(this.viewContext);
        this.serviceRunning = false;

        this.receiver.register();
        this.sender.checkService();
        for(MqttConnectionProfileModel model : MqttConnectionProfileModel.findAll())
        {
            addOrUpdateConnectionProfile(model);
        }
    }

    public void onDestroy(){
        this.receiver.unregister();
    }

    public void onStart(){

    }

    public ObservableArrayList<MqttConnectionProfileModel> getConnectionProfiles()
    {
        return this.connectionProfiles;
    }

    public void addEditProfileConnection(int index)
    {
        FragmentManager fm = ((AppCompatActivity)viewContext).getSupportFragmentManager();
        AddEditProfileFragment dialog = new AddEditProfileFragment();

        if(index >= 0 && index < this.connectionProfiles.size())
        {
            Bundle profile = new Bundle();
            profile.putParcelable("profile", (Parcelable)connectionProfiles.get(index));
            dialog.setArguments(profile);
        }
        dialog.show(fm, "add_edit_profile_fragment");
    }

    public void deleteProfileConnection(int index)
    {
        MqttConnectionProfileModel model = connectionProfiles.get(index);
        if(model.delete()) {
            connectionProfiles.remove(index);
            connectionProfileNames.remove(model.getProfileName());
            for(String profileName : connectionProfileNames.keySet()){
                int curIndex = connectionProfileNames.get(profileName);
                if(curIndex > index){
                    connectionProfileNames.put(profileName, curIndex - 1);
                }
            }
        }
    }

    @BindingAdapter("app:connectionItems")
    public  static void bindList(ListView view, ObservableArrayList<MqttConnectionProfileModel> list) {
        view.setAdapter(new ConnectionProfileListAdapter(view.getContext(), list));
    }

    @Override
    public void onProfileAdded(MqttConnectionProfileModel model) {
        boolean newProfile = addOrUpdateConnectionProfile(model);
        long id = newProfile ? model.save() : model.update();
    }

    @Override
    public void onQueryServiceRunningResponse(boolean running) {
        Toast.makeText(this.viewContext, "Service running = " + running, Toast.LENGTH_SHORT).show();
        this.serviceRunning = running;
    }

    @Override
    protected void onStartServiceResponse(boolean success) {
        Toast.makeText(this.viewContext, "Service started", Toast.LENGTH_SHORT).show();
        this.serviceRunning = true;
    }

    @Override
    protected void onStopServiceResponse(boolean success) {
        Toast.makeText(this.viewContext, "Service stopped", Toast.LENGTH_SHORT).show();
        this.serviceRunning = false;
        for(MqttConnectionProfileModel connectionProfile : connectionProfiles){
            onClientDisconnectResponse(connectionProfile.getProfileName(), connectionProfile.getClientId(), true);
        }
    }

    @Override
    protected void onClientConnectResponse(String profileName, String clientId, boolean success, String error) {
        MqttConnectionProfileModel model = getModel(profileName);
        if(model != null) {
            model.setIsConnecting(false);
        }
        if(success){
            Toast.makeText(this.viewContext, profileName + " connected successfully", Toast.LENGTH_SHORT).show();
            if(model != null){
                model.setIsConnected(true);
            }
        }
        else{
            Toast.makeText(this.viewContext, "Error connecting client " + profileName + ": " + error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onClientDisconnectResponse(String profileName, String clientId, boolean success) {
        MqttConnectionProfileModel model = getModel(profileName);
        if(model != null){
            model.setIsConnecting(false);
            model.setIsConnected(false);
        }
    }

    public boolean onMenuClick(int itemId){
        switch(itemId){
            case R.id.service_power_menuitem:
                if(this.serviceRunning){
                    this.sender.stopMqttService();
                }
                else{
                    this.sender.startMqttService();
                }
                return true;
            default:
                return true;
        }
    }

    private MqttConnectionProfileModel getModel(String clientId){
        for(MqttConnectionProfileModel model : this.connectionProfiles){
            if(model.getProfileName().equals(clientId)){
                return model;
            }
        }
        return null;
    }

    private boolean addOrUpdateConnectionProfile(MqttConnectionProfileModel model){
        if(!this.connectionProfileNames.containsKey(model.getProfileName())){
            this.connectionProfiles.add(model);
            this.connectionProfileNames.put(model.getProfileName(), this.connectionProfiles.size()-1);
            return true;
        }
        else{
            this.connectionProfiles.set(this.connectionProfileNames.get(model.getProfileName()), model);
            return false;
        }
    }
}
