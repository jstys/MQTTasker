package com.geminiapps.mqttsubscriber.views;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.ActivityConnectionDetailBinding;
import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.viewmodels.ConnectionDetailViewModel;

public class ConnectionDetailActivity extends AppCompatActivity {

    public AddEditSubscriptionFragment.ISubscriptionAddedListener mSubscriptionAddedListener;
    private ConnectionDetailViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent activityIntent = getIntent();
        MqttConnectionProfileModel model = activityIntent.getParcelableExtra("profile");

        ActivityConnectionDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_connection_detail);
        mViewModel = new ConnectionDetailViewModel(this, model);
        mSubscriptionAddedListener = mViewModel;
        binding.setViewModel(mViewModel);
        binding.setProfileModel(model);

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
}
