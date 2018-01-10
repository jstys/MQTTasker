package com.geminiapps.mqttsubscriber.views;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.ActivityTaskerPublishActionBinding;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.tasker.TaskerBroadcastReceiver;
import com.geminiapps.mqttsubscriber.viewmodels.TaskerViewModel;

import org.eclipse.paho.android.service.tasker.TaskerMqttConstants;

import java.util.ArrayList;
import java.util.List;

public class TaskerPublishActionActivity extends AppCompatActivity {

    private ActivityTaskerPublishActionBinding binding;
    private TaskerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String profile = "";
        int qos = 0;
        boolean retained = false;
        String message = "";
        String topic = "";

        Intent taskerIntent = getIntent();
        Bundle taskerExtras = taskerIntent.getBundleExtra(TaskerBroadcastReceiver.TASKER_DATA_BUNDLE);

        this.viewModel = new TaskerViewModel(this);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_tasker_publish_action);
        this.binding.setViewModel(this.viewModel);

        if(taskerExtras != null) {
            profile = taskerExtras.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA, "");
            qos = taskerExtras.getInt(TaskerMqttConstants.QOS_EXTRA, 0);
            retained = taskerExtras.getBoolean(TaskerMqttConstants.RETAINED_EXTRA, false);
            message = taskerExtras.getString(TaskerMqttConstants.MESSAGE_EXTRA, "");
            topic = taskerExtras.getString(TaskerMqttConstants.TOPIC_EXTRA, "");
        }

        this.binding.retainedCheckbox.setChecked(retained);
        this.binding.messageText.setText(message);
        this.binding.topicEdittext.setText(topic);
        this.loadSpinnerValues(profile, qos);
    }

    private void loadSpinnerValues(String selectedProfile, int selectedQos){
        List<MqttConnectionProfileModel> models = MqttConnectionProfileModel.findAll();
        List<String> profileNames = new ArrayList<String>();
        int selectedIndex = -1;

        for(int i = 0; i < models.size(); i++){
            profileNames.add(models.get(i).getProfileName());
            if(!selectedProfile.equals("") && models.get(i).getProfileName().equals(selectedProfile)){
                selectedIndex = i;
            }
        }

        // TODO: improve the view to be non-generic
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, profileNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.binding.profileSpinner.setAdapter(adapter);

        if(selectedIndex >= 0)
        {
            this.binding.profileSpinner.setSelection(selectedIndex);
        }

        ArrayAdapter<Integer> qosAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, new Integer[]{0, 1, 2});
        qosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.binding.qosSpinner.setAdapter(qosAdapter);
        this.binding.qosSpinner.setSelection(selectedQos);
    }

    @Override
    public void finish() {
        this.viewModel.savePublishActionSettings();

        super.finish();
    }

    public String getSelectedProfileName(){
        return (String)this.binding.profileSpinner.getSelectedItem();
    }

    public boolean isRetained(){
        return this.binding.retainedCheckbox.isChecked();
    }

    public int getQos() { return this.binding.qosSpinner.getSelectedItemPosition(); }

    public String getMessage(){ return this.binding.messageText.getText().toString(); }

    public String getTopic(){ return this.binding.topicEdittext.getText().toString(); }

}
