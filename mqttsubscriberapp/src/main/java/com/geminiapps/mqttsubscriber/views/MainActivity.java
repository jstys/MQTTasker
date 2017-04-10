package com.geminiapps.mqttsubscriber.views;

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
import com.geminiapps.mqttsubscriber.databinding.ActivityMainBinding;
import com.geminiapps.mqttsubscriber.viewmodels.MainViewModel;

public class MainActivity extends AppCompatActivity {

    public AddEditProfileFragment.IConnectionProfileListener profileListener;
    private MainViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        vm = new MainViewModel(this);
        profileListener = vm;
        binding.setViewModel(vm);
        registerForContextMenu(binding.connectionProfileList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        vm.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        vm.onStart();
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
                this.vm.addEditProfileConnection(info.position);
                return true;
            case R.id.delete_menu_item:
                this.vm.deleteProfileConnection(info.position);
                return true;
        }
        return false;
    }
}
