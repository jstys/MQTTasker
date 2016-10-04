package com.geminiapps.mqttsubscriber.viewmodels;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableArrayMap;
import android.databinding.ObservableMap;
import android.os.Bundle;
import android.os.Messenger;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.geminiapps.mqttsubscriber.adapters.ConnectionProfileListAdapter;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.views.AddEditProfileFragment;

import org.eclipse.paho.android.service.MqttService;

/**
 * Created by jim.stys on 10/1/16.
 */

public class MainViewModel {
    private Context viewContext;
    private ObservableArrayList<MqttConnectionProfileModel> connectionProfiles;

    public MainViewModel(Context context)
    {
        this.viewContext = context;
        this.connectionProfiles = new ObservableArrayList<>();
        for(MqttConnectionProfileModel model : MqttConnectionProfileModel.findAll())
        {
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

        Bundle profile = new Bundle();
        profile.putParcelableArrayList("profileList", this.connectionProfiles);
        if(index >= 0 && index < this.connectionProfiles.size())
        {
            profile.putParcelable("profile", (Parcelable)connectionProfiles.get(index));
        }
        dialog.setArguments(profile);
        dialog.show(fm, "add_edit_profile_fragment");
    }

    @BindingAdapter("bind:items")
    public  static void bindList(RecyclerView view, ObservableArrayList<MqttConnectionProfileModel> list) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        view.setLayoutManager(layoutManager);
        view.setAdapter(new ConnectionProfileListAdapter(list));
    }
}
