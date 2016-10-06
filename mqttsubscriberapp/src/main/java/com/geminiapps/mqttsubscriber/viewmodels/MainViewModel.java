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

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jim.stys on 10/1/16.
 */

public class MainViewModel extends MqttServiceListener implements AddEditProfileFragment.IConnectionProfileAddedListener {
    private Context viewContext;
    private MqttServiceReceiver receiver;
    private MqttServiceSender sender;
    private boolean serviceRunning;
    public ObservableArrayList<MqttConnectionProfileModel> connectionProfiles;
    public Set<String> connectionProfileNames;

    public MainViewModel(Context context)
    {
        this.viewContext = context;
        this.connectionProfiles = new ObservableArrayList<>();
        this.connectionProfileNames = new HashSet<>();
        for(MqttConnectionProfileModel model : MqttConnectionProfileModel.findAll())
        {
            this.connectionProfileNames.add(model.getProfileName());
            this.connectionProfiles.add(model);
        }
        this.receiver = new MqttServiceReceiver(this, this.viewContext);
        this.sender = new MqttServiceSender(this.viewContext);

        this.serviceRunning = false;
        this.sender.checkService();
    }

    public void onDestroy(){
        this.receiver.unregister();
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

    @BindingAdapter("app:items")
    public  static void bindList(ListView view, ObservableArrayList<MqttConnectionProfileModel> list) {
        view.setAdapter(new ConnectionProfileListAdapter(view.getContext(), list));
    }

    @Override
    public void onProfileAdded(MqttConnectionProfileModel model) {
        if (this.connectionProfileNames.contains(model.getProfileName())) {
            for (int i = 0; i < this.connectionProfiles.size(); i++) {
                MqttConnectionProfileModel modelIter = this.connectionProfiles.get(i);
                if (modelIter.getProfileName().equals(model.getProfileName())) {
                    this.connectionProfiles.set(i, model);
                    break;
                }
            }
        } else {
            this.connectionProfiles.add(model);
        }
        this.connectionProfileNames.add(model.getProfileName());
    }

    @Override
    public void onQueryServiceRunningResponse(boolean running) {
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
}
