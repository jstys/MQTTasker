package com.geminiapps.mqttsubscriber.views;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.ActivityConnectionDetailBinding;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.viewmodels.ConnectionDetailViewModel;

public class ConnectionDetailActivity extends AppCompatActivity {

    public AddEditSubscriptionFragment.ISubscriptionAddedListener mSubscriptionAddedListener;
    public ConnectToProfileWithOptionsFragment.IConnectActionListener mConnectActionListener;
    private ConnectionDetailViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent activityIntent = getIntent();
        MqttConnectionProfileModel model = activityIntent.getParcelableExtra("profile");

        ActivityConnectionDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_connection_detail);
        mViewModel = new ConnectionDetailViewModel(this, model);
        mSubscriptionAddedListener = mViewModel;
        mConnectActionListener = mViewModel;
        binding.setViewModel(mViewModel);
        binding.setProfileModel(model);
        registerForContextMenu(binding.subscriptionList);
        setTitle(model.getProfileName());
    }

    @Override
    protected void onStop() {
        super.onStop();

        mViewModel.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mViewModel.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_connection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mViewModel.onMenuClick(item.getItemId());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_longclick , menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch(item.getItemId()){
            case R.id.edit_menu_item:
                mViewModel.addEditSubscription(info.position);
                return true;
            case R.id.delete_menu_item:
                mViewModel.deleteSubscription(info.position);
                return true;
        }
        return false;
    }
}
