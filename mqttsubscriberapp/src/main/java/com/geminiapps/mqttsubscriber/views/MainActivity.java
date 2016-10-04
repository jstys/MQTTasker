package com.geminiapps.mqttsubscriber.views;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.ActivityMainBinding;
import com.geminiapps.mqttsubscriber.viewmodels.MainViewModel;

import org.eclipse.paho.android.service.TaskerMqttConstants;
import org.eclipse.paho.android.service.TaskerMqttService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setViewModel(new MainViewModel(this));

        //TODO: implement a button for start/stop intent
        Intent startIntent = new Intent(this, TaskerMqttService.class);
        startIntent.setAction(TaskerMqttConstants.START_SERVICE_ACTION);
        startService(startIntent);
    }
}
