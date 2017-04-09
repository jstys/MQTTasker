package com.geminiapps.mqttsubscriber.tasker;

import android.content.Context;
import android.os.Bundle;


public interface ITaskerConditionChecker {
    void checkCondition(Context context, Bundle data, int messageId);
}
