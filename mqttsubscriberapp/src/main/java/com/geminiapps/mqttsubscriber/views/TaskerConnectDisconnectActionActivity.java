package com.geminiapps.mqttsubscriber.views;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.ActivityTaskerConnectionActionBinding;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.tasker.TaskerBroadcastReceiver;
import com.geminiapps.mqttsubscriber.viewmodels.TaskerViewModel;

import org.eclipse.paho.android.service.tasker.TaskerMqttConstants;
import org.eclipse.paho.android.service.tasker.TaskerPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TaskerConnectDisconnectActionActivity extends AppCompatActivity {

    private ActivityTaskerConnectionActionBinding binding;
    private TaskerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean autoReconnect = true;
        boolean cleanSession = true;
        String action = "";
        String profile = "";
        Intent taskerIntent = getIntent();
        Bundle taskerExtras = taskerIntent.getBundleExtra(TaskerBroadcastReceiver.TASKER_DATA_BUNDLE);

        this.viewModel = new TaskerViewModel(this);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_tasker_connection_action);
        this.binding.setViewModel(this.viewModel);

        if(taskerExtras != null) {
            action = taskerExtras.getString(TaskerMqttConstants.ACTION_EXTRA, "");
            profile = taskerExtras.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA, "");
            autoReconnect = taskerExtras.getBoolean(TaskerMqttConstants.RECONNECT_EXTRA, true);
            cleanSession = taskerExtras.getBoolean(TaskerMqttConstants.CLEAN_SESSION_EXTRA, true);
            switch (action) {
                case TaskerMqttConstants.CONNECT_ACTION:
                    this.binding.connectRadioButton.setChecked(true);
                    break;
                case TaskerMqttConstants.DISCONNECT_ACTION:
                    this.binding.disconnectRadioButton.setChecked(true);
                    break;
            }
        }
        else{
            this.binding.connectRadioButton.setChecked(true);
        }
        this.binding.autoReconnectCheckbox.setChecked(autoReconnect);
        this.binding.cleanSessionCheckbox.setChecked(cleanSession);

        this.loadSpinnerValues(profile);
    }

    @Override
    public void finish() {
        this.viewModel.saveConnectionActionSettings();

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
        this.binding.spinner.setAdapter(adapter);

        if(selectedIndex >= 0)
        {
            this.binding.spinner.setSelection(selectedIndex);
        }
    }

    public String getSelectedProfileName(){
        return (String)this.binding.spinner.getSelectedItem();
    }

    public boolean isConnectAction(){
        return this.binding.connectRadioButton.isChecked();
    }

    public boolean isAutoReconnectEnabled(){
        return this.binding.autoReconnectCheckbox.isChecked();
    }

    public boolean isCleanSessionEnabled(){
        return this.binding.cleanSessionCheckbox.isChecked();
    }
}
