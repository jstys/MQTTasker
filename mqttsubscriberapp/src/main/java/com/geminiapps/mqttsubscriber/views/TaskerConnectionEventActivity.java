package com.geminiapps.mqttsubscriber.views;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.ActivityTaskerConnectionEventBinding;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.tasker.TaskerBroadcastReceiver;
import com.geminiapps.mqttsubscriber.viewmodels.TaskerViewModel;

import org.eclipse.paho.android.service.tasker.TaskerMqttConstants;

import java.util.ArrayList;
import java.util.List;

public class TaskerConnectionEventActivity extends AppCompatActivity {

    private ActivityTaskerConnectionEventBinding mBinding;
    private TaskerViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent taskerIntent = getIntent();
        Bundle taskerExtras = taskerIntent.getBundleExtra(TaskerBroadcastReceiver.TASKER_DATA_BUNDLE);
        String selectedProfile = "";
        String action = TaskerMqttConstants.CONNECT_ACTION;

        mViewModel = new TaskerViewModel(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tasker_connection_event);
        mBinding.setViewModel(mViewModel);

        if(taskerExtras != null) {
            selectedProfile = taskerExtras.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA, "");
            action = taskerExtras.getString(TaskerMqttConstants.ACTION_EXTRA, "");
        }

        switch (action) {
            case TaskerMqttConstants.CONNECT_ACTION:
                mBinding.connectRadioButton.setChecked(true);
                break;
            case TaskerMqttConstants.DISCONNECT_ACTION:
                mBinding.disconnectRadioButton.setChecked(true);
                break;
        }

        loadSpinnerValues(selectedProfile);
    }

    @Override
    public void finish() {
        mViewModel.saveConnectionEventSettings();

        super.finish();
    }

    private void loadSpinnerValues(String selectedProfile){
        List<MqttConnectionProfileModel> models = MqttConnectionProfileModel.findAll();
        List<String> profileNames = new ArrayList<String>();
        int selectedIndex = -1;

        for(int i = 0; i < models.size(); i++){
            profileNames.add(models.get(i).getProfileName());
            if(!selectedProfile.equals("") && models.get(i).getProfileName().equals(selectedProfile)){
                selectedIndex = i;
            }
        }

        // TODO: improve the view to be non-generic
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, profileNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.profileSpinner.setAdapter(adapter);

        if(selectedIndex >= 0)
        {
            mBinding.profileSpinner.setSelection(selectedIndex);
        }
    }

    public String getSelectedProfileName(){
        return (String)mBinding.profileSpinner.getSelectedItem();
    }

    public boolean isConnectedEvent(){
        return mBinding.connectRadioButton.isChecked();
    }
}
