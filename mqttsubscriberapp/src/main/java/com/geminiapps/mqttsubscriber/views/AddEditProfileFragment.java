package com.geminiapps.mqttsubscriber.views;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.geminiapps.mqttsubscriber.databinding.DialogAddEditProfileBinding;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.viewmodels.AddEditProfileViewModel;

/**
 * Created by jim.stys on 9/29/16.
 */

public class AddEditProfileFragment extends DialogFragment {

    private DialogAddEditProfileBinding binding;
    private ObservableArrayList<MqttConnectionProfileModel> profileList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Get bundled data to determine if we're adding or editing
        Bundle args = getArguments();
        this.profileList = (ObservableArrayList)args.getParcelableArrayList("profileList");
        MqttConnectionProfileModel profile = args.getParcelable("profile");

        Context context = getActivity();
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_add_edit_profile, null, false);

        // Create the view model
        AddEditProfileViewModel vm = new AddEditProfileViewModel(this.getDialog(), this.profileList);
        binding.setViewModel(vm);

        if (profile != null) {
            getDialog().setTitle("Edit Connection Profile");
            binding.setProfileModel(profile);
        } else {
            getDialog().setTitle("Add Connection Profile");
            binding.setProfileModel(new MqttConnectionProfileModel());
        }

        return binding.getRoot();
    }
}