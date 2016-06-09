package com.samsung.multiscreen.msf20.photoshare;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.samsung.multiscreen.Client;
import com.samsung.multiscreen.Error;
import com.samsung.multiscreen.Result;
import com.samsung.multiscreen.Service;
import com.samsung.multiscreen.msf20.sdk.ServiceWrapper;
import com.samsung.multiscreen.util.RunUtil;


public class MainActivity extends ListActivity {
    private static final String TAG = new Object() {}.getClass().getEnclosingClass().getName();

    private App app;
    private PhotoShareWebApplicationHelper photoShare;
    
    private Menu refreshMenu;
    private static ProgressBar progressBar;
    private static CountDownTimer countDownTimer;

    private ServiceListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: " + TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        app = App.getInstance();
        photoShare = app.getPhotoShare();

        progressBar = (ProgressBar)findViewById(R.id.initial_loading);
        showProgress();

        adapter = new ServiceListAdapter(this, R.layout.listview_item);
        setListAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: " + TAG);
        refreshMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.action_refresh);
        app.setMenuReady(true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu: " + TAG);
        if (app.isMenuReady() && (adapter.getCount() == 0) && 
                (photoShare != null) && 
                !photoShare.isRunning()) {
            if (refreshMenu != null) {
                MenuItem item = refreshMenu.findItem(R.id.action_refresh);
                if (item.getActionView() == null) {
                    item.setActionView(R.layout.refresh);
                }
                if (item.isEnabled()) {
                    item.setEnabled(false);
                }
            }
            validateServices(new Result<Boolean>() {

                @Override
                public void onError(Error error) {
                }

                @Override
                public void onSuccess(Boolean result) {
                    countDownTimer = photoShare.startDiscovery(searchListener);
                }
            });
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: " + TAG);
        
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            item.setEnabled(false);
            item.setActionView(R.layout.refresh);

            validateServices(new Result<Boolean>() {

                @Override
                public void onError(Error error) {
                }

                @Override
                public void onSuccess(Boolean result) {
                    countDownTimer = photoShare.startDiscovery(searchListener);
                }
            });

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        try {
            showProgress();
            
            ServiceWrapper wrapper = (ServiceWrapper)getListAdapter().getItem(position);
            final Service service = wrapper.getService();

            photoShare.connectAndLaunch(service, 
                    new Result<Client>() {

                        @Override
                        public void onSuccess(Client client) {
                            Log.d(TAG, "Channel onSuccess(): " + client.toString());
                        }

                        @Override
                        public void onError(Error error) {
                            hideProgress();
                            showMessage(app.getConfig().getString(R.string.photoshare_channel_err));
                        }
                    }, 
                    new ChannelListener() {
                        
                        private boolean launched = false;
                        
                        @Override
                        public void onClientDisconnect(Client client) {
                            Log.d(TAG, "Channel onClientDisconnect(): " + client.toString());
                        }
                        
                        @Override
                        public void onClientConnect(Client client) {
                            Log.d(TAG, "Channel onClientConnect(): " + client.toString());
                            
                            if (client.isHost()) {
                                startGridView();
                            }
                        }

                        @Override
                        public void onDisconnect(Client client) {
                            Log.d(TAG, "Channel onDisconnect(): " + client.toString());
                        }

                        @Override
                        public void onConnect(Client client) {
                            Log.d(TAG, "Channel onConnect(): " + client.toString());
                        }

                        @Override
                        public void onReady() {
                            Log.d(TAG, "Channel onReady()");
                            startGridView();
                        }
                        
                        private void startGridView() {
                            hideProgress();

                            if (!launched) {
                                launched = true;
                                Intent intent = new Intent(getApplicationContext(), GridViewActivity.class);
                                intent.putParcelableArrayListExtra(ImageInfoUtils.IMAGE_INFOS_KEY, ImageInfoUtils.getImageInfos(getApplicationContext()));
                                startActivity(intent);
                            }
                        }
                    }
                );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: " + TAG);
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: " + TAG);
        super.onResume();
        Log.d(TAG, "onResume menu ready? " + app.isMenuReady());
        if (app.isMenuReady() && (adapter.getCount() == 0) && 
                (photoShare != null) && 
                !photoShare.isRunning()) {
            if (refreshMenu != null) {
                MenuItem item = refreshMenu.findItem(R.id.action_refresh);
                if (item.getActionView() == null) {
                    item.setActionView(R.layout.refresh);
                }
                if (item.isEnabled()) {
                    item.setEnabled(false);
                }
            }

            validateServices(new Result<Boolean>() {

                @Override
                public void onError(Error error) {
                }

                @Override
                public void onSuccess(Boolean result) {
                    countDownTimer = photoShare.startDiscovery(searchListener);
                }
            });
        } else if (refreshMenu != null) {
            MenuItem item = refreshMenu.findItem(R.id.action_refresh);
            item.setActionView(null);
            item.setEnabled(true);
        }
        
        // Reset the channel
        photoShare.resetChannel();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: " + TAG);
        super.onPause();
        completeScan();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: " + TAG);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: " + TAG);
        if ((photoShare != null) && 
                photoShare.isRunning()) {
            photoShare.stopDiscovery();
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged: " + TAG);
        super.onConfigurationChanged(newConfig);
    }

    private SearchListener searchListener = new SearchListener() {
        
        @Override
        public void onStop() {
            Log.d(TAG, "Search onStop()");
            countDownTimer.cancel();
            completeScan();
        }
        
        @Override
        public void onStart() {
            Log.d(TAG, "Search onStart()");
        }

        @Override
        public void onFound(final Service service) {
            Log.d(TAG, "Search onAdded() " + service.toString());

            // Add service to a visual list where your user can select.
            // For display, we recommend that you show: service.getName()
            RunUtil.runOnUI(new Runnable() {

                @Override
                public void run() {
                    ServiceWrapper wrapper = new ServiceWrapper(service);
                    if (!adapter.contains(wrapper)) {
                        adapter.add(wrapper);
                    } else {
                        adapter.replace(wrapper);
                    }
                    hideProgress();
                }
            });
        }

        @Override
        public void onLost(final Service service) {
            Log.d(TAG, "Search onLost() " + service.toString());

            // Remove this service from the display list
            RunUtil.runOnUI(new Runnable() {

                @Override
                public void run() {
                    ServiceWrapper wrapper = new ServiceWrapper(service);
                    adapter.remove(wrapper);
                }
            });
        }
    };
    
    private void completeScan() {

        RunUtil.runOnUI(new Runnable() {

            @Override
            public void run() {
                if (refreshMenu != null) {
                    MenuItem item = refreshMenu.findItem(R.id.action_refresh);
                    if (item.getActionView() != null) {
                        item.setActionView(null);
                    }
                    if (!item.isEnabled()) {
                        item.setEnabled(true);
                    }
                }
                hideProgress();
            }
        });
    }

    private void showProgress() {
        int visibility = progressBar.getVisibility();
        if ((visibility == ProgressBar.INVISIBLE) || 
                (visibility == ProgressBar.GONE)) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
    }
    
    private void hideProgress() {
        if (progressBar.getVisibility() == ProgressBar.VISIBLE) {
            progressBar.setVisibility(ProgressBar.GONE);
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, 
                message, 
                Toast.LENGTH_LONG).show();
    }
    
    private class ValidateHelper {
        final int numServices;
        private int numReturned = 0;
        
        ValidateHelper(int numServices) {
            this.numServices = numServices;
        }
        
        boolean hasAllReturned() {
            return (numReturned >= numServices);
        }
        
        void hasReturned() {
            numReturned++;
        }
    }
    
    private void validateServices(final Result<Boolean> callback) {
        
        final int numServices = adapter.getCount();
        
        if (numServices > 0) {
            final ValidateHelper helper = new ValidateHelper(numServices);
            for (int i = 0; i < numServices; i++) {
                final ServiceWrapper wrapper = adapter.getItem(i);
                Service service = wrapper.getService();
                Service.getByURI(service.getUri(), 2000, new Result<Service>() {

                    @Override
                    public void onSuccess(Service service) {
                        Log.d(TAG, "validateServices onSuccess(): " + service.toString());
                        // We can contact the service, so keep it in the master 
                        // list.
                        helper.hasReturned();
                        if (helper.hasAllReturned()) {
                            callback.onSuccess(true);
                        }
                    }

                    @Override
                    public void onError(Error error) {
                        Log.d(TAG, "validateServices onError() unable to contact service: " + error.toString());
                        helper.hasReturned();
                        adapter.remove(wrapper);
                        if (helper.hasAllReturned()) {
                            callback.onSuccess(true);
                        }
                    }
                });
            }
        } else {
            callback.onSuccess(true);
        }
    }
}
