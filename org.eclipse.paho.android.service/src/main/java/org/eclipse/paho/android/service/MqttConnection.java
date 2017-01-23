/*******************************************************************************
 * Copyright (c) 1999, 2016 IBM Corp.
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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.paho.android.service.MessageStore.StoredMessage;
import org.eclipse.paho.android.service.tasker.TaskerMqttConstants;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/**
 * <p>
 * MqttConnection holds a MqttAsyncClient {host,port,mClientId} instance to perform
 * MQTT operations to MQTT broker.
 * </p>
 * <p>
 * Most of the major API here is intended to implement the most general forms of
 * the methods in IMqttAsyncClient, with slight adjustments for the Android
 * environment<br>
 * These adjustments usually consist of adding two parameters to each method :-
 * <ul>
 * <li>invocationContext - a string passed from the application to identify the
 * context of the operation (mainly included for support of the javascript API
 * implementation)</li>
 * <li>activityToken - a string passed from the Activity to relate back to a
 * callback method or other context-specific data</li>
 * </ul>
 * </p>
 * <p>
 * Operations are very much asynchronous, so success and failure are notified by
 * packing the relevant data into Intent objects which are broadcast back to the
 * Activity via the MqttService.callbackToActivity() method.
 * </p>
 */
public class MqttConnection implements MqttCallbackExtended {

	private static final String TAG = "MqttConnection";
	private static final String NOT_CONNECTED = "not connected";

	// fields for the connection definition
	private String mProfileName;
	private String mClientId;
	private String mServerURI;
	private MqttClientPersistence mPersistence = null;
	private MqttConnectOptions mConnectOptions;
	private MqttService mService = null;
	private volatile boolean isConnecting = false;
	private volatile boolean disconnected = true;

	// Saved sent messages and their corresponding Topics, activityTokens and
	// invocationContexts, so we can handle "deliveryComplete" callbacks
	// from the mqttClient
	private Map<IMqttDeliveryToken, String /* Topic */> savedTopics = new HashMap<IMqttDeliveryToken, String>();
	private Map<IMqttDeliveryToken, MqttMessage> savedSentMessages = new HashMap<IMqttDeliveryToken, MqttMessage>();
	private Map<IMqttDeliveryToken, String> savedActivityTokens = new HashMap<IMqttDeliveryToken, String>();
	private Map<IMqttDeliveryToken, String> savedInvocationContexts = new HashMap<IMqttDeliveryToken, String>();

	private WakeLock wakelock = null;
	private String wakeLockTag = null;

	private DisconnectedBufferOptions bufferOpts = null;

	public String getServerURI() {
		return mServerURI;
	}
	public void setServerURI(String mServerURI) {
		this.mServerURI = mServerURI;
	}

	public String getClientId() {
		return mClientId;
	}
	public void setClientId(String mClientId) {
		this.mClientId = mClientId;
	}


	public MqttConnectOptions getConnectOptions() {
		return mConnectOptions;
	}

	public void setConnectOptions(MqttConnectOptions mConnectOptions) {
		this.mConnectOptions = mConnectOptions;
	}

	// our client object - instantiated on connect
	private MqttAsyncClient myClient = null;


	/**
	 * Constructor - create an MqttConnection to communicate with MQTT server
	 * 
	 * @param service
	 *            our "parent" mService - we make callbacks to it
	 * @param serverURI
	 *            the URI of the MQTT server to which we will connect
	 * @param clientId
	 *            the name by which we will identify ourselves to the MQTT
	 *            server
	 * @param persistence
	 *            the mPersistence class to use to store in-flight message. If
	 *            null then the default mPersistence mechanism is used
	 * @param profileName
	 *            the "handle" by which the activity will identify us
	 */
	public MqttConnection(MqttService service, String serverURI, String clientId,
			MqttClientPersistence persistence, String profileName) {
		this.mServerURI = serverURI.toString();
		this.mService = service;
		this.mClientId = clientId;
		this.mPersistence = persistence;
		this.mProfileName = profileName;

		StringBuffer buff = new StringBuffer(this.getClass().getCanonicalName());
		buff.append(" ");
		buff.append(clientId);
		buff.append(" ");
		buff.append("on host ");
		buff.append(serverURI);
		wakeLockTag = buff.toString();
	}

	// The major API implementation follows
	/**
	 * Connect to the server specified when we were instantiated
	 * 
	 * @param options
	 *            timeout, etc
	 * @param invocationContext
	 *            arbitrary data to be passed back to the application
	 * @param activityToken
	 *            arbitrary identifier to be passed back to the Activity
	 */
	public void connect(MqttConnectOptions options, String invocationContext,
			String activityToken) {

		if(options != null)
		{
			mConnectOptions = options;
		}

		if (mConnectOptions.isCleanSession()) { // if it's a clean session,
			// discard old data
			mService.messageStore.clearArrivedMessages(mProfileName);
		}

		mService.traceDebug(TAG, "Connecting {" + mServerURI + "} as {" + mClientId + "}");
		final Bundle resultBundle = new Bundle();
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN,
				activityToken);
		resultBundle.putString(
				MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT,
				invocationContext);
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
				MqttServiceConstants.CONNECT_ACTION);
		
				
		try {
			if (mPersistence == null) {
				// ask Android where we can put files
				File myDir = mService.getExternalFilesDir(TAG);

				if (myDir == null) {
					// No external storage, use internal storage instead.
					myDir = mService.getDir(TAG, Context.MODE_PRIVATE);
					
					if(myDir == null){
						//Shouldn't happen.
						resultBundle.putString(
								MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
								"Error! No external and internal storage available");
						resultBundle.putSerializable(
								MqttServiceConstants.CALLBACK_EXCEPTION, new MqttPersistenceException());
						mService.callbackToActivity(mProfileName, Status.ERROR,
								resultBundle);
						return;
					}
				}

				// use that to setup MQTT client mPersistence storage
				mPersistence = new MqttDefaultFilePersistence(
						myDir.getAbsolutePath());
			}
			
			IMqttActionListener listener = new MqttActionListener(
					resultBundle) {

				@Override
				public void onSuccess(IMqttToken asyncActionToken) {
					doAfterConnectSuccess(resultBundle);
					mService.traceDebug(TAG, "connect success!");
				}

				@Override
				public void onFailure(IMqttToken asyncActionToken,
						Throwable exception) {
					resultBundle.putString(
							MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
							exception.getLocalizedMessage());
					resultBundle.putSerializable(
							MqttServiceConstants.CALLBACK_EXCEPTION, exception);
					mService.traceError(TAG,
							"connect fail, call connect to reconnect.reason:"
									+ exception.getMessage());

					doAfterConnectFail(resultBundle);

				}
			};
			
			if (myClient != null) {
				if (isConnecting ) {
					mService.traceDebug(TAG,
							"myClient != null and the client is connecting. Connect return directly.");
					mService.traceDebug(TAG,"Connect return:isConnecting:"+isConnecting+".disconnected:"+isConnected());
				}else if(isConnected()){
					mService.traceDebug(TAG,"myClient != null and the client is connected and notify!");
					Exception exception = new Exception("Client is already connected");
					resultBundle.putSerializable(MqttServiceConstants.CALLBACK_EXCEPTION, exception);
					resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE, exception.getLocalizedMessage());
					mService.callbackToActivity(mProfileName, Status.ERROR, resultBundle);
				}
				else {					
					mService.traceDebug(TAG, "myClient != null and the client is not connected");
					mService.traceDebug(TAG,"Do Real connect!");
					setConnectingState(true);
					myClient.connect(mConnectOptions, invocationContext, listener);
				}
			}
			
			// if myClient is null, then create a new connection
			else {
				myClient = new MqttAsyncClient(mServerURI, mClientId,
						mPersistence, new AlarmPingSender(mService));
				myClient.setCallback(this);

				mService.traceDebug(TAG,"Do Real connect!");
				setConnectingState(true);
				myClient.connect(mConnectOptions, invocationContext, listener);
			}
		} catch (Exception e) {
			handleException(resultBundle, e);
		}
	}

	private void doAfterConnectSuccess(final Bundle resultBundle) {
		//since the device's cpu can go to sleep, acquire a wakelock and drop it later.
		acquireWakeLock();
		mService.callbackToActivity(mProfileName, Status.OK, resultBundle);
		deliverBacklog();
		setConnectingState(false);
		disconnected = false;
		releaseWakeLock();
	}

	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		Bundle resultBundle = new Bundle();
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
				MqttServiceConstants.CONNECT_EXTENDED_ACTION);
		resultBundle.putBoolean(MqttServiceConstants.CALLBACK_RECONNECT, reconnect);
		resultBundle.putString(MqttServiceConstants.CALLBACK_SERVER_URI, serverURI);
		mService.callbackToActivity(mProfileName, Status.OK, resultBundle);
	}

	private void doAfterConnectFail(final Bundle resultBundle){
		//
		acquireWakeLock();
		disconnected = true;
		setConnectingState(false);
		mService.callbackToActivity(mProfileName, Status.ERROR, resultBundle);
		releaseWakeLock();
	}
	
	private void handleException(final Bundle resultBundle, Exception e) {
		resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
				e.getLocalizedMessage());

		resultBundle.putSerializable(MqttServiceConstants.CALLBACK_EXCEPTION, e);

		mService.callbackToActivity(mProfileName, Status.ERROR, resultBundle);
	}

	/**
	 * Attempt to deliver any outstanding messages we've received but which the
	 * application hasn't acknowledged. If "cleanSession" was specified, we'll
	 * have already purged any such messages from our messageStore.
	 */
	private void deliverBacklog() {
		Iterator<StoredMessage> backlog = mService.messageStore
				.getAllArrivedMessages(mProfileName);
		while (backlog.hasNext()) {
			StoredMessage msgArrived = backlog.next();
			Bundle resultBundle = messageToBundle(msgArrived.getMessageId(),
					msgArrived.getTopic(), msgArrived.getMessage());
			resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
					MqttServiceConstants.MESSAGE_ARRIVED_ACTION);
			mService.callbackToActivity(mProfileName, Status.OK, resultBundle);
		}
	}

	/**
	 * Create a bundle containing all relevant data pertaining to a message
	 * 
	 * @param messageId
	 *            the message's identifier in the messageStore, so that a
	 *            callback can be made to remove it once delivered
	 * @param topic
	 *            the topic on which the message was delivered
	 * @param message
	 *            the message itself
	 * @return the bundle
	 */
	private Bundle messageToBundle(String messageId, String topic,
			MqttMessage message) {
		Bundle result = new Bundle();
		result.putString(MqttServiceConstants.CALLBACK_MESSAGE_ID, messageId);
		result.putString(MqttServiceConstants.CALLBACK_DESTINATION_NAME, topic);
		result.putParcelable(MqttServiceConstants.CALLBACK_MESSAGE_PARCEL,
				new ParcelableMqttMessage(message));
		return result;
	}
	
	/**
	 * Close connection from the server
	 * 
	 */
	void close() {
		mService.traceDebug(TAG, "close()");
		try {
			if (myClient != null) {
				myClient.close();
			}
		} catch (MqttException e) {
			// Pass a new bundle, let handleException stores error messages.
			handleException(new Bundle(), e);
		}
	}

	/**
	 * Disconnect from the server
	 * 
	 * @param quiesceTimeout
	 *            in milliseconds
	 * @param invocationContext
	 *            arbitrary data to be passed back to the application
	 * @param activityToken
	 *            arbitrary string to be passed back to the activity
	 */
	void disconnect(long quiesceTimeout, String invocationContext,
			String activityToken) {
		mService.traceDebug(TAG, "disconnect()");
		disconnected = true;
		final Bundle resultBundle = new Bundle();
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN,
				activityToken);
		resultBundle.putString(
				MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT,
				invocationContext);
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
				MqttServiceConstants.DISCONNECT_ACTION);
		if (isConnected()) {
			IMqttActionListener listener = new MqttActionListener(
					resultBundle);
			try {
				myClient.disconnect(quiesceTimeout, invocationContext, listener);
			} catch (Exception e) {
				handleException(resultBundle, e);
			}
		} else {
			resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
					NOT_CONNECTED);
			mService.traceError(MqttServiceConstants.DISCONNECT_ACTION,
					NOT_CONNECTED);
			mService.callbackToActivity(mProfileName, Status.ERROR, resultBundle);
		}

		if (mConnectOptions != null && mConnectOptions.isCleanSession()) {
			// assume we'll clear the stored messages at this point
			mService.messageStore.clearArrivedMessages(mProfileName);
		}

		releaseWakeLock();
	}

	/**
	 * Disconnect from the server
	 * 
	 * @param invocationContext
	 *            arbitrary data to be passed back to the application
	 * @param activityToken
	 *            arbitrary string to be passed back to the activity
	 */
	public void disconnect(String invocationContext, String activityToken) {
		mService.traceDebug(TAG, "disconnect()");
		disconnected = true;
		final Bundle resultBundle = new Bundle();
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN,
				activityToken);
		resultBundle.putString(
				MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT,
				invocationContext);
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
				MqttServiceConstants.DISCONNECT_ACTION);
		if (isConnected()) {
			IMqttActionListener listener = new MqttActionListener(
					resultBundle);
			try {
				myClient.disconnect(invocationContext, listener);
			} catch (Exception e) {
				handleException(resultBundle, e);
			}
		} else {
			resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
					NOT_CONNECTED);
			mService.traceError(MqttServiceConstants.DISCONNECT_ACTION,
					NOT_CONNECTED);
			mService.callbackToActivity(mProfileName, Status.ERROR, resultBundle);
		}

		if (mConnectOptions != null && mConnectOptions.isCleanSession()) {
			// assume we'll clear the stored messages at this point
			mService.messageStore.clearArrivedMessages(mProfileName);
		}
		releaseWakeLock();
	}

	/**
	 * @return true if we are connected to an MQTT server
	 */
	public boolean isConnected() {
		if (myClient != null)
			return myClient.isConnected();
		return false;
	}

	/**
	 * Publish a message on a topic
	 * 
	 * @param topic
	 *            the topic on which to publish - represented as a string, not
	 *            an MqttTopic object
	 * @param payload
	 *            the content of the message to publish
	 * @param qos
	 *            the quality of mService requested
	 * @param retained
	 *            whether the MQTT server should retain this message
	 * @param invocationContext
	 *            arbitrary data to be passed back to the application
	 * @param activityToken
	 *            arbitrary string to be passed back to the activity
	 * @return token for tracking the operation
	 */
	public IMqttDeliveryToken publish(String topic, byte[] payload, int qos,
			boolean retained, String invocationContext, String activityToken) {
		final Bundle resultBundle = new Bundle();
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
				MqttServiceConstants.SEND_ACTION);
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN,
				activityToken);
		resultBundle.putString(
				MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT,
				invocationContext);

		IMqttDeliveryToken sendToken = null;

		if (isConnected()) {
			IMqttActionListener listener = new MqttActionListener(
					resultBundle);
			try {
				MqttMessage message = new MqttMessage(payload);
				message.setQos(qos);
				message.setRetained(retained);
				sendToken = myClient.publish(topic, payload, qos, retained,
						invocationContext, listener);
				storeSendDetails(topic, message, sendToken, invocationContext,
						activityToken);
			} catch (Exception e) {
				handleException(resultBundle, e);
			}
		} else {
			resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
					NOT_CONNECTED);
			mService.traceError(MqttServiceConstants.SEND_ACTION, NOT_CONNECTED);
			mService.callbackToActivity(mProfileName, Status.ERROR, resultBundle);
		}

		return sendToken;
	}

	/**
	 * Publish a message on a topic
	 * 
	 * @param topic
	 *            the topic on which to publish - represented as a string, not
	 *            an MqttTopic object
	 * @param message
	 *            the message to publish
	 * @param invocationContext
	 *            arbitrary data to be passed back to the application
	 * @param activityToken
	 *            arbitrary string to be passed back to the activity
	 * @return token for tracking the operation
	 */
	public IMqttDeliveryToken publish(String topic, MqttMessage message,
			String invocationContext, String activityToken) {
		final Bundle resultBundle = new Bundle();
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
				MqttServiceConstants.SEND_ACTION);
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN,
				activityToken);
		resultBundle.putString(
				MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT,
				invocationContext);

		IMqttDeliveryToken sendToken = null;

		if (isConnected()) {
			IMqttActionListener listener = new MqttActionListener(
					resultBundle);
			try {
				sendToken = myClient.publish(topic, message, invocationContext,
						listener);
				storeSendDetails(topic, message, sendToken, invocationContext,
						activityToken);
			} catch (Exception e) {
				handleException(resultBundle, e);
			}
		} else if ((myClient !=null) && (this.bufferOpts != null) && (this.bufferOpts.isBufferEnabled())){
			// Client is not connected, but buffer is enabled, so sending message
			IMqttActionListener listener = new MqttActionListener(
					resultBundle);
			try {
				sendToken = myClient.publish(topic, message, invocationContext,
						listener);
				storeSendDetails(topic, message, sendToken, invocationContext,
						activityToken);
			} catch (Exception e) {
				handleException(resultBundle, e);
			}
		}  else {
			Log.i(TAG, "Client is not connected, so not sending message");
			resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
					NOT_CONNECTED);
			mService.traceError(MqttServiceConstants.SEND_ACTION, NOT_CONNECTED);
			mService.callbackToActivity(mProfileName, Status.ERROR, resultBundle);
		}
		return sendToken;
	}

	/**
	 * Subscribe to a topic
	 * 
	 * @param topic
	 *            a possibly wildcarded topic name
	 * @param qos
	 *            requested quality of mService for the topic
	 * @param invocationContext
	 *            arbitrary data to be passed back to the application
	 * @param activityToken
	 *            arbitrary identifier to be passed back to the Activity
	 */
	public void subscribe(final String topic, final int qos,
			String invocationContext, String activityToken) {
		mService.traceDebug(TAG, "subscribe({" + topic + "}," + qos + ",{"
				+ invocationContext + "}, {" + activityToken + "}");
		final Bundle resultBundle = new Bundle();
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
				MqttServiceConstants.SUBSCRIBE_ACTION);
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN,
				activityToken);
		resultBundle.putString(
				MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT,
				invocationContext);

		if (isConnected()) {
			IMqttActionListener listener = new MqttActionListener(
					resultBundle);
			try {
				myClient.subscribe(topic, qos, invocationContext, listener);
			} catch (Exception e) {
				handleException(resultBundle, e);
			}
		} else {
			resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
					NOT_CONNECTED);
			mService.traceError("subscribe", NOT_CONNECTED);
			mService.callbackToActivity(mProfileName, Status.ERROR, resultBundle);
		}
	}

	/**
	 * Subscribe to one or more topics
	 * 
	 * @param topic
	 *            a list of possibly wildcarded topic names
	 * @param qos
	 *            requested quality of mService for each topic
	 * @param invocationContext
	 *            arbitrary data to be passed back to the application
	 * @param activityToken
	 *            arbitrary identifier to be passed back to the Activity
	 */
	public void subscribe(final String[] topic, final int[] qos,
			String invocationContext, String activityToken) {
		mService.traceDebug(TAG, "subscribe({" + topic + "}," + qos + ",{"
				+ invocationContext + "}, {" + activityToken + "}");
		final Bundle resultBundle = new Bundle();
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
				MqttServiceConstants.SUBSCRIBE_ACTION);
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN,
				activityToken);
		resultBundle.putString(
				MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT,
				invocationContext);

		if (isConnected()) {
			IMqttActionListener listener = new MqttActionListener(
					resultBundle);
			try {
				myClient.subscribe(topic, qos, invocationContext, listener);
			} catch (Exception e) {
				handleException(resultBundle, e);
			}
		} else {
			resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
					NOT_CONNECTED);
			mService.traceError("subscribe", NOT_CONNECTED);
			mService.callbackToActivity(mProfileName, Status.ERROR, resultBundle);
		}
	}

	public void subscribe(String topicFilter, int qos, String invocationContext, String activityToken, IMqttMessageListener messageListener)
	{
		mService.traceDebug(TAG, "subscribe({" + topicFilter + "}," + qos + ",{"
				+ invocationContext + "}, {" + activityToken + "}");
		final Bundle resultBundle = new Bundle();
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION, MqttServiceConstants.SUBSCRIBE_ACTION);
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN, activityToken);
		resultBundle.putString(MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT, invocationContext);
		resultBundle.putString(TaskerMqttConstants.TOPIC_FILTER_EXTRA, topicFilter);
		if(isConnected()){
			try {
				IMqttActionListener actionListener = new MqttActionListener(resultBundle);
				myClient.subscribe(topicFilter, qos, null, actionListener, messageListener);
			} catch (Exception e){
				handleException(resultBundle, e);
			}
		} else {
			resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE, NOT_CONNECTED);
			mService.traceError("subscribe", NOT_CONNECTED);
			mService.callbackToActivity(mProfileName, Status.ERROR, resultBundle);
		}
	}

	public void subscribe(String[] topicFilters, int[] qos, String invocationContext, String activityToken, IMqttMessageListener[] messageListeners) {
		mService.traceDebug(TAG, "subscribe({" + topicFilters + "}," + qos + ",{"
				+ invocationContext + "}, {" + activityToken + "}");
		final Bundle resultBundle = new Bundle();
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION, MqttServiceConstants.SUBSCRIBE_ACTION);
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN, activityToken);
		resultBundle.putString(MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT, invocationContext);
		if(isConnected()){
			try {
				IMqttActionListener actionListener = new MqttActionListener(resultBundle);
				myClient.subscribe(topicFilters, qos, null, actionListener, messageListeners);
			} catch (Exception e){
				handleException(resultBundle, e);
			}
		} else {
			resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE, NOT_CONNECTED);
			mService.traceError("subscribe", NOT_CONNECTED);
			mService.callbackToActivity(mProfileName, Status.ERROR, resultBundle);
		}
	}

		/**
         * Unsubscribe from a topic
         *
         * @param topic
         *            a possibly wildcarded topic name
         * @param invocationContext
         *            arbitrary data to be passed back to the application
         * @param activityToken
         *            arbitrary identifier to be passed back to the Activity
         */
	void unsubscribe(final String topic, String invocationContext,
			String activityToken) {
		mService.traceDebug(TAG, "unsubscribe({" + topic + "},{"
				+ invocationContext + "}, {" + activityToken + "})");
		final Bundle resultBundle = new Bundle();
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
				MqttServiceConstants.UNSUBSCRIBE_ACTION);
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN,
				activityToken);
		resultBundle.putString(
				MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT,
				invocationContext);
		if (isConnected()) {
			IMqttActionListener listener = new MqttActionListener(
					resultBundle);
			try {
				myClient.unsubscribe(topic, invocationContext, listener);
			} catch (Exception e) {
				handleException(resultBundle, e);
			}
		} else {
			resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
					NOT_CONNECTED);

			mService.traceError("subscribe", NOT_CONNECTED);
			mService.callbackToActivity(mProfileName, Status.ERROR, resultBundle);
		}
	}

	/**
	 * Unsubscribe from one or more topics
	 * 
	 * @param topic
	 *            a list of possibly wildcarded topic names
	 * @param invocationContext
	 *            arbitrary data to be passed back to the application
	 * @param activityToken
	 *            arbitrary identifier to be passed back to the Activity
	 */
	void unsubscribe(final String[] topic, String invocationContext,
			String activityToken) {
		mService.traceDebug(TAG, "unsubscribe({" + topic + "},{"
				+ invocationContext + "}, {" + activityToken + "})");
		final Bundle resultBundle = new Bundle();
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
				MqttServiceConstants.UNSUBSCRIBE_ACTION);
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN,
				activityToken);
		resultBundle.putString(
				MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT,
				invocationContext);
		if (isConnected()) {
			IMqttActionListener listener = new MqttActionListener(
					resultBundle);
			try {
				myClient.unsubscribe(topic, invocationContext, listener);
			} catch (Exception e) {
				handleException(resultBundle, e);
			}
		} else {
			resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
					NOT_CONNECTED);

			mService.traceError("subscribe", NOT_CONNECTED);
			mService.callbackToActivity(mProfileName, Status.ERROR, resultBundle);
		}
	}

	/**
	 * Get tokens for all outstanding deliveries for a client
	 * 
	 * @return an array (possibly empty) of tokens
	 */
	public IMqttDeliveryToken[] getPendingDeliveryTokens() {
		return myClient.getPendingDeliveryTokens();
	}

	// Implement MqttCallback
	/**
	 * Callback for connectionLost
	 * 
	 * @param why
	 *            the exeception causing the break in communications
	 */
	@Override
	public void connectionLost(Throwable why) {
		mService.traceDebug(TAG, "connectionLost(" + why.getMessage() + ")");
		disconnected = true;
		try {
			disconnect(null, null);
		} catch (Exception e) {
			// ignore it - we've done our best
		}

		Bundle resultBundle = new Bundle();
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
				MqttServiceConstants.ON_CONNECTION_LOST_ACTION);
		if (why != null) {
			resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
					why.getMessage());
			if (why instanceof MqttException) {
				resultBundle.putSerializable(
						MqttServiceConstants.CALLBACK_EXCEPTION, why);
			}
			resultBundle.putString(
					MqttServiceConstants.CALLBACK_EXCEPTION_STACK,
					Log.getStackTraceString(why));
		}
		mService.callbackToActivity(mProfileName, Status.OK, resultBundle);
		// client has lost connection no need for wake lock
		releaseWakeLock();
	}

	/**
	 * Callback to indicate a message has been delivered (the exact meaning of
	 * "has been delivered" is dependent on the QOS value)
	 * 
	 * @param messageToken
	 *            the messge token provided when the message was originally sent
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken messageToken) {

		mService.traceDebug(TAG, "deliveryComplete(" + messageToken + ")");

		MqttMessage message = savedSentMessages.remove(messageToken);
		if (message != null) { // If I don't know about the message, it's
			// irrelevant
			String topic = savedTopics.remove(messageToken);
			String activityToken = savedActivityTokens.remove(messageToken);
			String invocationContext = savedInvocationContexts
					.remove(messageToken);

			Bundle resultBundle = messageToBundle(null, topic, message);
			if (activityToken != null) {
				resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
						MqttServiceConstants.SEND_ACTION);
				resultBundle.putString(
						MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN,
						activityToken);
				resultBundle.putString(
						MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT,
						invocationContext);
		
				mService.callbackToActivity(mProfileName, Status.OK,
						resultBundle);
			}
			resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
					MqttServiceConstants.MESSAGE_DELIVERED_ACTION);
			mService.callbackToActivity(mProfileName, Status.OK, resultBundle);
		}

		// this notification will have kept the connection alive but send the previously sechudled ping anyway
	}

	/**
	 * Callback when a message is received
	 * 
	 * @param topic
	 *            the topic on which the message was received
	 * @param message
	 *            the message itself
	 */
	@Override
	public void messageArrived(String topic, MqttMessage message)
			throws Exception {

		mService.traceDebug(TAG,
				"messageArrived(" + topic + ",{" + message.toString() + "})");

		String messageId = mService.messageStore.storeArrived(mProfileName,
				topic, message);
	
		Bundle resultBundle = messageToBundle(messageId, topic, message);
		resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
				MqttServiceConstants.MESSAGE_ARRIVED_ACTION);
		resultBundle.putString(MqttServiceConstants.CALLBACK_MESSAGE_ID,
				messageId);
		mService.callbackToActivity(mProfileName, Status.OK, resultBundle);
				
	}



	/**
	 * Store details of sent messages so we can handle "deliveryComplete"
	 * callbacks from the mqttClient
	 * 
	 * @param topic
	 * @param msg
	 * @param messageToken
	 * @param invocationContext
	 * @param activityToken
	 */
	private void storeSendDetails(final String topic, final MqttMessage msg,
			final IMqttDeliveryToken messageToken,
			final String invocationContext, final String activityToken) {
		savedTopics.put(messageToken, topic);
		savedSentMessages.put(messageToken, msg);
		savedActivityTokens.put(messageToken, activityToken);
		savedInvocationContexts.put(messageToken, invocationContext);
	}

	/**
	 * Acquires a partial wake lock for this client
	 */
	private void acquireWakeLock() {
		if (wakelock == null) {
			PowerManager pm = (PowerManager) mService
					.getSystemService(Service.POWER_SERVICE);
			wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					wakeLockTag);
		}
		wakelock.acquire();

	}

	/**
	 * Releases the currently held wake lock for this client
	 */
	private void releaseWakeLock() {
		if(wakelock != null && wakelock.isHeld()){
			wakelock.release();
		}
	}



	/**
	 * General-purpose IMqttActionListener for the Client context
	 * <p>
	 * Simply handles the basic success/failure cases for operations which don't
	 * return results
	 * 
	 */
	private class MqttActionListener implements IMqttActionListener {

		private final Bundle resultBundle;

		private MqttActionListener(Bundle resultBundle) {
			this.resultBundle = resultBundle;
		}

		@Override
		public void onSuccess(IMqttToken asyncActionToken) {
			mService.callbackToActivity(mProfileName, Status.OK, resultBundle);
		}

		@Override
		public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
			resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
					exception.getLocalizedMessage());

			resultBundle.putSerializable(
					MqttServiceConstants.CALLBACK_EXCEPTION, exception);

			mService.callbackToActivity(mProfileName, Status.ERROR, resultBundle);
		}
	}

	/**
	 * Receive notification that we are offline<br>
	 * if cleanSession is true, we need to regard this as a disconnection
	 */
	void offline() {
		
		if (disconnected && !mConnectOptions.isCleanSession()) {
			Exception e = new Exception("Android offline");
			connectionLost(e);
		}
	}
	
	/**
	* Reconnect<br>
	* Only appropriate if cleanSession is false and we were connected.
	* Declare as synchronized to avoid multiple calls to this method to send connect 
	* multiple times 
	*/
	synchronized void reconnect() {
		if (isConnecting) {
			mService.traceDebug(TAG, "The client is connecting. Reconnect return directly.");
			return ;
		}
		
		if(!mService.isOnline()){
			mService.traceDebug(TAG,
					"The network is not reachable. Will not do reconnect");
			return;
		}

		if (disconnected && !mConnectOptions.isCleanSession()) {
			// use the activityToke the same with action connect
			mService.traceDebug(TAG,"Do Real Reconnect!");
			final Bundle resultBundle = new Bundle();

			resultBundle.putString(
				MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT, null);
			resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
				MqttServiceConstants.CONNECT_ACTION);
			
			try {
				
				IMqttActionListener listener = new MqttActionListener(resultBundle) {
					@Override
					public void onSuccess(IMqttToken asyncActionToken) {
						// since the device's cpu can go to sleep, acquire a
						// wakelock and drop it later.
						mService.traceDebug(TAG,"Reconnect Success!");
						mService.traceDebug(TAG,"DeliverBacklog when reconnect.");
						doAfterConnectSuccess(resultBundle);
					}
					
					@Override
					public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
						resultBundle.putString(
								MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
								exception.getLocalizedMessage());
						resultBundle.putSerializable(
								MqttServiceConstants.CALLBACK_EXCEPTION,
								exception);
						mService.callbackToActivity(mProfileName, Status.ERROR,
								resultBundle);

						doAfterConnectFail(resultBundle);
						
					}
				};
				
				myClient.connect(mConnectOptions, null, listener);
				setConnectingState(true);
			} catch (MqttException e) {
				mService.traceError(TAG, "Cannot reconnect to remote server." + e.getMessage());
				setConnectingState(false);
				handleException(resultBundle, e);
			}
		}
	}
	
	/**
	 * 
	 * @param isConnecting
	 */
	synchronized void setConnectingState(boolean isConnecting){
		this.isConnecting = isConnecting;
	}

	/**
	 * Sets the DisconnectedBufferOptions for this client
	 * @param bufferOpts
	 */
	public void setBufferOpts(DisconnectedBufferOptions bufferOpts) {
		this.bufferOpts = bufferOpts;
		myClient.setBufferOpts(bufferOpts);
	}

	public int getBufferedMessageCount(){
		return myClient.getBufferedMessageCount();
	}

	public MqttMessage getBufferedMessage(int bufferIndex){
		return myClient.getBufferedMessage(bufferIndex);
	}

	public void deleteBufferedMessage(int bufferIndex){
		myClient.deleteBufferedMessage(bufferIndex);
	}
}
