package com.geminiapps.mqttsubscriber.views;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.DialogConnectToProfileWithOptionsBinding;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.viewmodels.ConnectOptionsViewModel;

public class ConnectToProfileWithOptionsFragment extends DialogFragment{

    IConnectActionListener mConnectListener;
    DialogConnectToProfileWithOptionsBinding mBinding;

    public ConnectToProfileWithOptionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        MqttConnectionProfileModel profile = (args != null) ? (MqttConnectionProfileModel)args.getParcelable("profile") : null;

        Context context = getActivity();
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_connect_to_profile_with_options, null, false);

        // Create the view model
        ConnectOptionsViewModel vm = new ConnectOptionsViewModel(this.getDialog(), mConnectListener, this);
        mBinding.setViewModel(vm);

        getDialog().setTitle("Connection Options");

        if (profile != null) {
            mBinding.autoReconnectCheckbox.setChecked(profile.getAutoReconnect());
            mBinding.cleanSessionCheckbox.setChecked(profile.getCleanSession());
            mBinding.setProfileModel(profile);
        } else {
            mBinding.autoReconnectCheckbox.setChecked(true);
            mBinding.cleanSessionCheckbox.setChecked(true);
        }

        return mBinding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ConnectionDetailActivity activity = (ConnectionDetailActivity) context;
        mConnectListener = activity.mConnectActionListener;
    }

    public interface IConnectActionListener{
        void onConnectToProfile(MqttConnectionProfileModel model);
    }

    public boolean isAutoReconnectEnabled(){
        return mBinding.autoReconnectCheckbox.isChecked();
    }

    public boolean isCleanSessionEnabled(){
        return mBinding.cleanSessionCheckbox.isChecked();
    }

}
