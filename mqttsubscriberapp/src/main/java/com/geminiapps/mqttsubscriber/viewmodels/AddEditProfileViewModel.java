package com.geminiapps.mqttsubscriber.viewmodels;

import android.app.Dialog;
import android.databinding.ObservableField;
import android.net.Uri;
import android.util.Patterns;
import android.widget.Toast;

import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.views.AddEditProfileFragment;

import org.abego.treelayout.internal.util.java.lang.string.StringUtil;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.output.StringBuilderWriter;

public class AddEditProfileViewModel {
    private static final String TCP_PROTOCOL = "TCP";
    private static final String WS_PROTOCOL = "Websockets";

    private Dialog dialog;
    private AddEditProfileFragment.IConnectionProfileAddedListener profileAddedListener;
    private AddEditProfileFragment mView;

    public MqttConnectionProfileModel mModel;
    public final ObservableField<String> brokerHost = new ObservableField<>();
    public final ObservableField<String> brokerPort = new ObservableField<>();

    public AddEditProfileViewModel(Dialog dialog, AddEditProfileFragment.IConnectionProfileAddedListener profileAddedListener, AddEditProfileFragment view, MqttConnectionProfileModel model)
    {
        this.dialog = dialog;
        this.profileAddedListener = profileAddedListener;
        mView = view;
        mModel = (model == null) ? new MqttConnectionProfileModel() : model;
        if(model != null){
            Uri brokerUri = Uri.parse(mModel.getBrokerUri());
            brokerHost.set(brokerUri.getHost());
            brokerPort.set(String.valueOf(brokerUri.getPort()));
        }
    }

    public void saveConnectionProfile() {
        if (mModel != null) {
            StringBuilder errorStringBuilder = new StringBuilder();
            if(validateProfile(errorStringBuilder)) {
                mModel.setBrokerUri(buildBrokerURI());
                this.profileAddedListener.onProfileAdded(mModel);
                this.dialog.dismiss();
            }
            else{
                Toast.makeText(mView.getContext(), errorStringBuilder.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void cancelConnectionProfile() {
        this.dialog.dismiss();
    }

    private String buildBrokerURI(){
        String uri = "";
        String protocol = mView.getSelectedProtocol();
        if(protocol.equals(TCP_PROTOCOL)){
            uri += "tcp://";
        }
        else if(protocol.equals(WS_PROTOCOL)){
            uri += "ws://";
        }

        uri += brokerHost.get() + ":" + brokerPort.get();

        return uri;
    }

    private boolean validateProfile(StringBuilder errorStringBuilder){
        boolean isValid = true;

        if(mModel.getProfileName().isEmpty()){
            errorStringBuilder.append("Profile name cannot be empty\n");
            isValid = false;
        }

        //Validate using the minimum required client identifier
        if(!mModel.getClientId().matches("^[A-Za-z0-9]+$")){
            errorStringBuilder.append("Client Identifier contains non-alphanumeric characters\n");
            isValid = false;
        }

        if(mModel.getClientId().length() < 1 || mModel.getClientId().length() > 23){
            errorStringBuilder.append("Client Identifier must be between 1 and 23 characters\n");
            isValid = false;
        }

        if(mModel.getUsername().length() > 65535){
            errorStringBuilder.append("Username must be less than 65535 characters\n");
            isValid = false;
        }

        if(mModel.getPassword().length() > 65535){
            errorStringBuilder.append("Password must be less than 65535 characters\n");
            isValid = false;
        }

        int port = 0;
        try{
            port = Integer.parseInt(brokerPort.get());
        }
        catch(NumberFormatException e){
            //Just catch the failed parsing
        }
        finally{
            if(port < 1 || port > 65535){
                errorStringBuilder.append("Broker port must be between 1 and 65535\n");
                isValid = false;
            }
        }

        return isValid;
    }
}
