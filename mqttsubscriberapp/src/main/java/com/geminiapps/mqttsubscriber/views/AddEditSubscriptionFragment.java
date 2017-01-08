package com.geminiapps.mqttsubscriber.views;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.DialogAddEditSubscriptionBinding;
import com.geminiapps.mqttsubscriber.models.MqttSubscriptionModel;
import com.geminiapps.mqttsubscriber.viewmodels.AddEditSubscriptionViewModel;

/**
 * Created by jim.stys on 1/5/17.
 */

public class AddEditSubscriptionFragment extends DialogFragment {
    public AddEditSubscriptionFragment.ISubscriptionAddedListener mSubscriptionAddedListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Get bundled data to determine if we're adding or editing
        Bundle args = getArguments();
        String clientId = args.getString("profile");
        MqttSubscriptionModel subscription = args.getParcelable("subscription");

        Context context = getActivity();
        DialogAddEditSubscriptionBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_add_edit_subscription, null, false);

        // Create the view model
        AddEditSubscriptionViewModel vm = new AddEditSubscriptionViewModel(this.getDialog(), mSubscriptionAddedListener);
        binding.setViewModel(vm);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.qos_values_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.qosSpinner.setAdapter(adapter);

        if (subscription != null) {
            getDialog().setTitle("Edit Subscription");
            binding.setSubscriptionModel(subscription);
        } else {
            getDialog().setTitle("Add Subscription");
            binding.setSubscriptionModel(new MqttSubscriptionModel(clientId));
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ConnectionDetailActivity activity = (ConnectionDetailActivity)context;
        mSubscriptionAddedListener = activity.mSubscriptionAddedListener;
    }

    public interface ISubscriptionAddedListener{
        public void onSubscriptionAdded(MqttSubscriptionModel model);
    }
}
