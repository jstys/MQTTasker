package com.geminiapps.mqttsubscriber.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.ListitemConnectionProfileBinding;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.viewmodels.ConnectionProfileListItemViewModel;

import java.util.ArrayList;

/**
 * Created by jim.stys on 10/2/16.
 */

public class ConnectionProfileListAdapter extends ArrayAdapter {
    private ObservableArrayList<MqttConnectionProfileModel> connectionProfiles;
    private Context context;

    public ConnectionProfileListAdapter(Context context, ArrayList connectionProfiles)
    {
        super(context, 0, connectionProfiles);
        this.context = context;
        this.connectionProfiles = (ObservableArrayList<MqttConnectionProfileModel>)connectionProfiles;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListitemConnectionProfileBinding binding = DataBindingUtil.inflate(inflater, R.layout.listitem_connection_profile, parent, false);

        binding.setProfileModel(this.connectionProfiles.get(position));
        binding.setViewModel(new ConnectionProfileListItemViewModel(this.context));
        return binding.getRoot();
    }
}
