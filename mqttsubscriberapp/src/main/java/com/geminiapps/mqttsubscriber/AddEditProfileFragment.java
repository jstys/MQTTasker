package com.geminiapps.mqttsubscriber;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.eclipse.paho.android.service.MqttConnectionProfile;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jim.stys on 9/29/16.
 */

public class AddEditProfileFragment extends DialogFragment {
    @BindView(R.id.profileNameTextview) EditText profileNameTextview;
    @BindView(R.id.brokerUriTextview) EditText brokerUriTextview;
    @BindView(R.id.usernameTextview) EditText usernameTextview;
    @BindView(R.id.passwordTextview) EditText passwordTextview;
    @BindView(R.id.cleanSessionCheckbox) CheckBox cleanSessionCheckbox;
    @BindView(R.id.autoReconnectCheckbox) CheckBox autoReconnectCheckbox;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get bundled data to determine if we're adding or editing
        Bundle profile = getArguments();

        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        DialogInterface.OnShowListener onShowListener = new ConnectionDialogShowListener();

        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", null);
        builder.setView(R.layout.dialog_add_edit_profile);
        View view = View.inflate(context, R.layout.dialog_add_edit_profile, null);

        ButterKnife.bind(this, view);

        if(profile == null) // Add new profile
        {
            builder.setTitle("Add Connection Profile");
        }
        else // Edit existing profile
        {
            builder.setTitle("Edit Connection Profile");
            profileNameTextview.setEnabled(false);

            profileNameTextview.setText(profile.getString("profile"));
            brokerUriTextview.setText(profile.getString("brokerUri", ""));
            usernameTextview.setText(profile.getString("username", ""));
            passwordTextview.setText(profile.getString("password", ""));
            cleanSessionCheckbox.setChecked(profile.getBoolean("cleanSession", false));
            autoReconnectCheckbox.setChecked(profile.getBoolean("autoReconnect", false));
        }

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(onShowListener);
        return dialog;
    }

    private void saveConnectionProfile()
    {
        String profileName = profileNameTextview.getText().toString();
        String brokerUri = brokerUriTextview.getText().toString();
        String username = usernameTextview.getText().toString();
        String password = passwordTextview.getText().toString();
        boolean cleanSession = cleanSessionCheckbox.isChecked();
        boolean autoReconnect = autoReconnectCheckbox.isChecked();

        if(!profileName.isEmpty() && !brokerUri.isEmpty()) {
            MqttConnectionProfile profile = new MqttConnectionProfile(profileName, brokerUri, username, password, autoReconnect, cleanSession);
            long profileId = profile.save();

            if(profileId >= 0)
            {
                getDialog().dismiss();
            }
        }
    }

    private class ConnectionDialogShowListener implements DialogInterface.OnShowListener{

        @Override
        public void onShow(DialogInterface dialog) {
            Button okButton = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    saveConnectionProfile();
                }
            });
        }
    }
}
