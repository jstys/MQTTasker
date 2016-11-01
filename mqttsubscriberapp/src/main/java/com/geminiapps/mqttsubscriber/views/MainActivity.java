package com.geminiapps.mqttsubscriber.views;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.geminiapps.mqttsubscriber.R;
import com.geminiapps.mqttsubscriber.databinding.ActivityMainBinding;
import com.geminiapps.mqttsubscriber.viewmodels.MainViewModel;

public class MainActivity extends AppCompatActivity {

    public AddEditProfileFragment.IConnectionProfileAddedListener profileAddedListener;

    private MainViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        vm = new MainViewModel(this);
        profileAddedListener = vm;
        binding.setViewModel(vm);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        vm.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return this.vm.onMenuClick(item.getItemId());
    }
}
