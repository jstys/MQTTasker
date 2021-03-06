package com.geminiapps.mqttsubscriber.views;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.ActivityTaskerServiceActionBinding;
import com.geminiapps.mqttsubscriber.tasker.TaskerBroadcastReceiver;
import com.geminiapps.mqttsubscriber.viewmodels.TaskerViewModel;

import org.eclipse.paho.android.service.tasker.TaskerMqttConstants;


public class TaskerStartStopServiceActivity extends AppCompatActivity {

    private ActivityTaskerServiceActionBinding binding;
    private TaskerViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String action = TaskerMqttConstants.START_SERVICE_ACTION;
        Intent taskerIntent = getIntent();
        Bundle taskerExtras = taskerIntent.getBundleExtra(TaskerBroadcastReceiver.TASKER_DATA_BUNDLE);

        this.viewModel = new TaskerViewModel(this);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_tasker_service_action);
        this.binding.setViewModel(this.viewModel);

        if(taskerExtras != null) {
            action = taskerExtras.getString(TaskerMqttConstants.ACTION_EXTRA, TaskerMqttConstants.START_SERVICE_ACTION);
        }

        switch (action) {
            case TaskerMqttConstants.START_SERVICE_ACTION:
                this.binding.startServiceButton.setChecked(true);
                break;
            case TaskerMqttConstants.STOP_SERVICE_ACTION:
                this.binding.stopServiceButton.setChecked(true);
                break;
        }
    }

    @Override
    public void finish() {
        this.viewModel.saveServiceActionSettings();

        super.finish();
    }

    public boolean isStartAction(){
        return this.binding.startServiceButton.isChecked();
    }
}
