package com.geminiapps.mqttsubscriber.viewmodels;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.geminiapps.mqttsubscriber.adapters.ConnectionProfileListAdapter;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.views.AddEditProfileFragment;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jim.stys on 10/1/16.
 */

public class MainViewModel extends BroadcastReceiver implements AddEditProfileFragment.IConnectionProfileAddedListener {
    private Context viewContext;
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
        if(this.connectionProfileNames.contains(model.getProfileName()))
        {
            for(int i = 0; i < this.connectionProfiles.size(); i++){
                MqttConnectionProfileModel modelIter = this.connectionProfiles.get(i);
                if(modelIter.getProfileName().equals(model.getProfileName())){
                    this.connectionProfiles.set(i, model);
                    break;
                }
            }
        }
        else
        {
            this.connectionProfiles.add(model);
        }
        this.connectionProfileNames.add(model.getProfileName());
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
