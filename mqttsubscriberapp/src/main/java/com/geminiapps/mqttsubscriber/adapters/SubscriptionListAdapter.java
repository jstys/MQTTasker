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
import com.geminiapps.mqttsubscriber.databinding.ListitemSubscriptionBinding;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.models.MqttSubscriptionModel;
import com.geminiapps.mqttsubscriber.viewmodels.ConnectionProfileListItemViewModel;
import com.geminiapps.mqttsubscriber.viewmodels.SubscriptionListItemViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jim.stys on 1/5/17.
 */

public class SubscriptionListAdapter extends ArrayAdapter {
    private ObservableArrayList<MqttSubscriptionModel> mSubscriptionList;
    private Context mContext;

    public SubscriptionListAdapter(Context context, ArrayList<MqttSubscriptionModel> subscriptions) {
        super(context, 0, subscriptions);

        mContext = context;
        mSubscriptionList = (ObservableArrayList<MqttSubscriptionModel>)subscriptions;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListitemSubscriptionBinding binding = DataBindingUtil.inflate(inflater, R.layout.listitem_subscription, parent, false);

        binding.setSubscriptionModel(mSubscriptionList.get(position));
        binding.setViewModel(new SubscriptionListItemViewModel(mContext));
        return binding.getRoot();
    }
}
