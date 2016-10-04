/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.eclipse.paho.android.service;

import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;

import android.os.Message;
import android.widget.Toast;

/**
 * What the Service passes to the Activity on binding:-
 * <ul>
 * <li>a reference to the Service
 * <li>the activityToken provided when the Service was started
 * </ul>
 *
 */
public class MqttServiceHandler extends Handler {

	private TaskerMqttService mqttService;
	private String activityToken;

	public MqttServiceHandler(TaskerMqttService mqttService) {
		this.mqttService = mqttService;
	}

	/**
	 * @return a reference to the Service
	 */
	public MqttService getService() {
		return mqttService;
	}

	void setActivityToken(String activityToken) {
		this.activityToken = activityToken;
	}

	/**
	 * @return the activityToken provided when the Service was started
	 */
	public String getActivityToken() {
		return activityToken;
	}

	@Override
	public void handleMessage(Message msg) {
		Bundle data = msg.getData();
		this.mqttService.runAction(this.mqttService, data);
	}
}
