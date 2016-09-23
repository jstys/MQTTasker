package org.eclipse.paho.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class TaskerBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_INTENT = "com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING";
    public static final String CONDITION_INTENT = "com.twofortyfouram.local.Intent.ACTION_QUERY_CONDITION";
    private ITaskerActionRunner actionRunner;
    private ITaskerConditionChecker conditionChecker;

    public TaskerBroadcastReceiver(ITaskerActionRunner runner, ITaskerConditionChecker checker) {
        this.actionRunner = runner;
        this.conditionChecker = checker;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getBundleExtra("com.twofortyfouram.locale.Intent.EXTRA_BUNDLE");

        if(ACTION_INTENT.equals(intent.getAction()))
        {
            // Tasker action triggered
            this.actionRunner.runAction(bundle);
        }
        else if(CONDITION_INTENT.equals(intent.getAction()))
        {
            // Tasker condition check triggered
            this.conditionChecker.checkCondition(bundle);
        }
        else
        {
            // Received invalid intent
        }



    }
}
