package org.eclipse.paho.android.service;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by jim.stys on 9/23/16.
 */

public interface ITaskerActionRunner {
    public void runAction(Context context, Bundle data);
}
