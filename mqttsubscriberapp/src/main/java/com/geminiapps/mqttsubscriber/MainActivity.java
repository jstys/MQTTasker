package com.geminiapps.mqttsubscriber;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.android.service.TaskerMqttConstants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener, ServiceConnection {
    @BindView(R.id.stopbutton)  Button stopButton;
    @BindView(R.id.startbutton) Button startButton;
    @BindView(R.id.testConnectButton) Button connectButton;
    @BindView(R.id.testDisconnectButton) Button disconnectButton;
    @BindView(R.id.testSubscribeButton) Button subscribeButton;
    @BindView(R.id.testUnsubscribeButton) Button unsubscribeButton;
    @BindView(R.id.floatingActionButton) FloatingActionButton addButton;

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
        connectButton.setOnClickListener(this);
        disconnectButton.setOnClickListener(this);
        subscribeButton.setOnClickListener(this);
        unsubscribeButton.setOnClickListener(this);
        addButton.setOnClickListener(this);
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
        boolean sendMessage = false;
        Bundle data = null;
        switch(v.getId()){

            case R.id.startbutton:
                Intent startIntent = new Intent(this, MqttService.class);
                startIntent.setAction(TaskerMqttConstants.START_SERVICE_ACTION);
                startService(startIntent);
                bindService(startIntent, this, 0);
                break;
            case R.id.stopbutton:
                Intent stopIntent = new Intent(this, MqttService.class);
                stopIntent.setAction(TaskerMqttConstants.STOP_SERVICE_ACTION);
                stopService(stopIntent);
                break;
            case R.id.testConnectButton:
                if(this.serviceRegistered)
                {
                    Message serviceMessage = new Message();
                    data = new Bundle();
                    data.putString(TaskerMqttConstants.ACTION_EXTRA, TaskerMqttConstants.CONNECT_ACTION);
                    serviceMessage.setData(data);
                    try {
                        this.subscriberService.send(serviceMessage);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.testDisconnectButton:
                if(this.serviceRegistered)
                {
                    sendMessage = true;
                    data = new Bundle();
                    data.putString(TaskerMqttConstants.ACTION_EXTRA, TaskerMqttConstants.DISCONNECT_ACTION);
                }
                break;
            case R.id.testSubscribeButton:
                if(this.serviceRegistered)
                {
                    sendMessage = true;
                    data = new Bundle();
                    data.putString(TaskerMqttConstants.ACTION_EXTRA, TaskerMqttConstants.SUBSCRIBE_ACTION);
                }
                break;
            case R.id.testUnsubscribeButton:
                if(this.serviceRegistered)
                {
                    sendMessage = true;
                    data = new Bundle();
                    data.putString(TaskerMqttConstants.ACTION_EXTRA, TaskerMqttConstants.UNSUBSCRIBE_ACTION);
                }
                break;
            case R.id.floatingActionButton:
                android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                AddEditProfileFragment dialog = new AddEditProfileFragment();
                dialog.show(fm, "add_edit_profile_fragment");
                break;
            default:
                break;
        }

        if(sendMessage && data != null)
        {
            Message serviceMessage = new Message();
            serviceMessage.setData(data);
            try {
                this.subscriberService.send(serviceMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
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
