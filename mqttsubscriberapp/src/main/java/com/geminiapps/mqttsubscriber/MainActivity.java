package com.geminiapps.mqttsubscriber;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.android.service.TaskerMqttConstants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener, ServiceConnection {
    @BindView(R.id.stopbutton)  Button stopButton;
    @BindView(R.id.startbutton) Button startButton;

    private Messenger subscriberService;
    private boolean serviceRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        serviceRegistered = false;
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(this.serviceRegistered)
        {
            unbindService(this);
        }
    }

    @Override
    public void onClick(View v) {
        Log.d("MqttApp", "Button");
        switch(v.getId()){

            case R.id.startbutton:
                Log.d("MqttApp", "Start pressed");
                Intent startIntent = new Intent(this, MqttService.class);
                startIntent.setAction(TaskerMqttConstants.START_SERVICE_ACTION);
                startService(startIntent);
                bindService(startIntent, this, 0);
                break;
            case R.id.stopbutton:
                Log.d("MqttApp", "Stop pressed");
                Intent stopIntent = new Intent(this, MqttService.class);
                stopIntent.setAction(TaskerMqttConstants.STOP_SERVICE_ACTION);
                stopService(stopIntent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        this.subscriberService = new Messenger(binder);
        this.serviceRegistered = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        this.subscriberService = null;
        this.serviceRegistered = false;
    }
}
