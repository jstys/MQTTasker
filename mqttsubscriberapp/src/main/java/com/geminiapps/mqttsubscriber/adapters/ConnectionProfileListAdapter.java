package com.geminiapps.mqttsubscriber.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.RecyclerviewConnectionProfilesBinding;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.viewmodels.AddEditProfileViewModel;
import com.geminiapps.mqttsubscriber.viewmodels.ConnectionListViewModel;

/**
 * Created by jim.stys on 10/2/16.
 */

public class ConnectionProfileListAdapter extends RecyclerView.Adapter {
    private ObservableArrayList<MqttConnectionProfileModel> connectionProfiles;

    public ConnectionProfileListAdapter(ObservableArrayList<MqttConnectionProfileModel> profiles){
        this.connectionProfiles = profiles;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_connection_profiles, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder myViewHolder = (ViewHolder)holder;
        final MqttConnectionProfileModel model = connectionProfiles.get(position);
        myViewHolder.binder.setProfileModel(model);
        myViewHolder.binder.setViewModel(new ConnectionListViewModel());
        myViewHolder.binder.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return this.connectionProfiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public RecyclerviewConnectionProfilesBinding binder;

        public ViewHolder(View v) {
            super(v);
            v.setClickable(true);
            binder = DataBindingUtil.bind(v);
        }
    }

}
