package com.geminiapps.mqttsubscriber.views;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.DialogAddEditProfileBinding;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.viewmodels.AddEditProfileViewModel;

/**
 * Created by jim.stys on 9/29/16.
 */

public class AddEditProfileFragment extends DialogFragment {

    public IConnectionProfileAddedListener profileAddedListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Get bundled data to determine if we're adding or editing
        Bundle args = getArguments();
        MqttConnectionProfileModel profile = (args != null) ? (MqttConnectionProfileModel)args.getParcelable("profile") : null;

        Context context = getActivity();
        DialogAddEditProfileBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_add_edit_profile, null, false);

        // Create the view model
        AddEditProfileViewModel vm = new AddEditProfileViewModel(this.getDialog(), this.profileAddedListener);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        MainActivity activity = (MainActivity)context;
        this.profileAddedListener = activity.profileAddedListener;
    }

    public interface IConnectionProfileAddedListener{
        public void onProfileAdded(MqttConnectionProfileModel model);
    }
}