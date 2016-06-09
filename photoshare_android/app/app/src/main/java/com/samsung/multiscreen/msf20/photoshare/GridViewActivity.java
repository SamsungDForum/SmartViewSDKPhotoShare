package com.samsung.multiscreen.msf20.photoshare;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.samsung.multiscreen.Channel;
import com.samsung.multiscreen.Channel.OnClientDisconnectListener;
import com.samsung.multiscreen.Channel.OnDisconnectListener;
import com.samsung.multiscreen.Client;
import com.samsung.multiscreen.Service;

import java.util.ArrayList;

public class GridViewActivity extends Activity {

    private static final String TAG = GridViewActivity.class.getName();
    private static final int VIEW_FULLSCREEN_IMAGE = 1;
    
    private App app;
    
    private GridView gridView;
    private GridViewArrayAdapter gridViewArrayAdapter;
    private Cursor imageCursor;

    private int firstVisiblePos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: " + TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        app = App.getInstance();
        gridView = (GridView)findViewById(R.id.gridView);

        gridViewArrayAdapter = new GridViewArrayAdapter(this, R.layout.gridview_item);
        Intent intent = getIntent();
        ArrayList<ImageInfo> imageInfos = intent.getParcelableArrayListExtra(ImageInfoUtils.IMAGE_INFOS_KEY);
        gridViewArrayAdapter.addAll(imageInfos);

        gridView.setAdapter(gridViewArrayAdapter);
        gridView.setOnItemClickListener(onItemClickListener);

        if (savedInstanceState != null) {
            firstVisiblePos = savedInstanceState.getInt(Constants.FIRST_POS);
        }
        
        getActionBar().setTitle(Constants.CONNECTING_MSG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private OnItemClickListener onItemClickListener = new OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
            intent.putExtra(ImageActivity.CUR_POS, position);
            intent.putParcelableArrayListExtra(ImageInfoUtils.IMAGE_INFOS_KEY, ImageInfoUtils.getImageInfos(getApplicationContext()));
            startActivityForResult(intent, VIEW_FULLSCREEN_IMAGE);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIEW_FULLSCREEN_IMAGE) {
            if (resultCode == RESULT_OK) {
                firstVisiblePos = data.getIntExtra(ImageActivity.CUR_POS, 0);
            }
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

        final PhotoShareWebApplicationHelper photoShare = app.getPhotoShare();
        Service service = photoShare.getService();
        final Channel channel = photoShare.getApplication();
        boolean isConnected = channel.isConnected();
        if ((service != null) && isConnected) {
            updateTitle(isConnected, service.getName());

            // Listen for the onConnectionLost and disconnect events
            channel.setOnDisconnectListener(new OnDisconnectListener() {
                
                @Override
                public void onDisconnect(Client client) {
                    Log.d(TAG, "Channel.onDisconnect() client: " + client.toString());
                    channel.setOnDisconnectListener(null);
                    updateTitle(false, null);
                    
                }
            });
            channel.setOnClientDisconnectListener(new OnClientDisconnectListener() {
                
                @Override
                public void onClientDisconnect(Client client) {
                    Log.d(TAG, "Channel.onClientDisconnect() client: " + client.toString());
                    if (client.isHost()) {
                        channel.setOnClientDisconnectListener(null);
                        updateTitle(false, null);
                    }
                }
            });
        } else {
            updateTitle(false, null);
        }
        
        if (firstVisiblePos > 0) {
            gridView.setSelection(firstVisiblePos);
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: " + TAG);
        super.onPause();
        firstVisiblePos = gridView.getFirstVisiblePosition();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.FIRST_POS, gridView.getFirstVisiblePosition());
    }
    
    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: " + TAG);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: " + TAG);
        if (imageCursor != null) {
            imageCursor.close();
        }
        imageCursor = null;
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void updateTitle(boolean success, String fn) {
        if (success) {
            getActionBar().setTitle(Constants.CONNECTED_PREFIX + fn);
        } else {
            getActionBar().setTitle(Constants.DISCONNECTED_MSG);
        }
    }
}
