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
import com.geminiapps.mqttsubscriber.databinding.DialogAddEditProfileBinding;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.viewmodels.AddEditProfileViewModel;

/**
 * Created by jim.stys on 9/29/16.
 */

public class AddEditProfileFragment extends DialogFragment {

    public IConnectionProfileAddedListener profileAddedListener;
    private DialogAddEditProfileBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Get bundled data to determine if we're adding or editing
        Bundle args = getArguments();
        MqttConnectionProfileModel profile = (args != null) ? (MqttConnectionProfileModel)args.getParcelable("profile") : null;

        Context context = getActivity();
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_add_edit_profile, null, false);

        // Create the view model
        AddEditProfileViewModel vm = new AddEditProfileViewModel(this.getDialog(), this.profileAddedListener, this, profile);
        mBinding.setViewModel(vm);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.protocol_values_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.protocolSpinner.setAdapter(adapter);
        mBinding.protocolSpinner.setSelection(0);

        if (profile != null) {
            getDialog().setTitle("Edit Connection Profile");
        } else {
            getDialog().setTitle("Add Connection Profile");
        }

        return mBinding.getRoot();
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

    public String getSelectedProtocol(){
        return (String)mBinding.protocolSpinner.getSelectedItem();
    }
}