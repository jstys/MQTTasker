package com.geminiapps.mqttsubscriber.views;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.ActivityTaskerConnectionActionBinding;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.viewmodels.TaskerViewModel;

import java.util.ArrayList;
import java.util.List;

public class TaskerConnectDisconnectActionActivity extends AppCompatActivity {

    ActivityTaskerConnectionActionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_tasker_connection_action);
        this.loadSpinnerValues();

        binding.setViewModel(new TaskerViewModel(this));

    }

    private void loadSpinnerValues(){
        List<MqttConnectionProfileModel> models = MqttConnectionProfileModel.findAll();
        List<String> profileNames = new ArrayList<String>();

        for(MqttConnectionProfileModel model : models){
            profileNames.add(model.getProfileName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, profileNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.binding.spinner.setAdapter(adapter);
    }

    public String getSelectedProfileName(){
        return (String)this.binding.spinner.getSelectedItem();
    }
}
