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

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.DialogAddEditSubscriptionBinding;
import com.geminiapps.mqttsubscriber.models.MqttSubscriptionModel;
import com.geminiapps.mqttsubscriber.viewmodels.AddEditSubscriptionViewModel;

import org.eclipse.paho.android.service.MqttSubscriptionRecord;

public class AddEditSubscriptionFragment extends DialogFragment {
    public AddEditSubscriptionFragment.ISubscriptionListener mSubscriptionListener;
    private DialogAddEditSubscriptionBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Get bundled data to determine if we're adding or editing
        Bundle args = getArguments();
        String clientId = args.getString("profile");
        MqttSubscriptionModel subscription = args.getParcelable("subscription");

        Context context = getActivity();
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_add_edit_subscription, null, false);

        // Create the view model
        int mode = (subscription == null) ? AddEditSubscriptionViewModel.ADD_MODE : AddEditSubscriptionViewModel.EDIT_MODE;
        AddEditSubscriptionViewModel vm = new AddEditSubscriptionViewModel(this.getDialog(), mSubscriptionListener, this, mode);
        mBinding.setViewModel(vm);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.qos_values_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.qosSpinner.setAdapter(adapter);

        if (subscription != null) {
            getDialog().setTitle("Edit Subscription");
            mBinding.setSubscriptionModel(subscription);
            mBinding.qosSpinner.setSelection(subscription.getQos());
            mBinding.topicEdittext.setEnabled(false);
        } else {
            getDialog().setTitle("Add Subscription");
            mBinding.setSubscriptionModel(new MqttSubscriptionModel(clientId));
            mBinding.topicEdittext.setEnabled(true);
        }

        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ConnectionDetailActivity activity = (ConnectionDetailActivity)context;
        mSubscriptionListener = activity.mSubscriptionListener;
    }

    public interface ISubscriptionListener{
        boolean onSubscriptionAdded(MqttSubscriptionModel model);
        boolean onSubscriptionUpdated(MqttSubscriptionModel model);
    }

    public int getSelectedQos(){
        return Integer.parseInt((String)mBinding.qosSpinner.getSelectedItem());
    }
}
