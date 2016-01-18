package com.samsung.multiscreen.msf20.photoshare;

import lombok.Getter;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;

import com.samsung.multiscreen.Application;
import com.samsung.multiscreen.Channel;
import com.samsung.multiscreen.Client;
import com.samsung.multiscreen.Error;
import com.samsung.multiscreen.Result;
import com.samsung.multiscreen.Search;
import com.samsung.multiscreen.Service;

public class PhotoShareWebApplicationHelper {
    private static final String TAG = new Object() {}.getClass().getEnclosingClass().getName();

    private static PhotoShareWebApplicationHelper instance;
    
    private final App app;

    private static Search search;

    @Getter
    private Service service = null;
    
    @Getter
    private Application application = null;
    
    private PhotoShareWebApplicationHelper(App app) {
        this.app = app;
        search = Service.search(app);
    }
    
    public static synchronized PhotoShareWebApplicationHelper getInstance(App app) {
        if (instance == null) {
            instance = new PhotoShareWebApplicationHelper(app);
        }
        return instance;
    }
    
    public CountDownTimer startDiscovery(SearchListener searchListener) {
        if (searchListener != null) {
            search.setOnStartListener(searchListener);
            search.setOnStopListener(searchListener);
            search.setOnServiceFoundListener(searchListener);
            search.setOnServiceLostListener(searchListener);
        }
        search.start();

        return startTimer(app.getResources().getInteger(R.integer.max_discovery_wait));
    }
    
    public void stopDiscovery() {
        if (search != null) {
            search.stop();
        }
    }

    public void connectAndLaunch(Service service, 
            Result<Client> callback, 
            ChannelListener channelListener) {
        Log.d(TAG, "launch() is called");
        this.service = service;
        
        Config config = app.getConfig();
        Uri uri = config.getPhotoShareUri();
        String channelId = config.getPhotoShareChannel();
        application = service.createApplication(uri, channelId);
        application.setConnectionTimeout(5000);
        
        setChannelListener(channelListener);
        
        // Debug
//        application.setDebug(app.getConfig().isDebug());

        application.connect(callback);
    }
    
    public void setChannelListener(ChannelListener channelListener) {
        if ((application != null) && (channelListener != null)) {
            application.setOnConnectListener(channelListener);
            application.setOnDisconnectListener(channelListener);
            application.setOnClientConnectListener(channelListener);
            application.setOnClientDisconnectListener(channelListener);
            application.setOnReadyListener(channelListener);
            application.setOnErrorListener(channelListener);
        }
    }

    public void resetChannel() {
        if ((application != null) && application.isConnected()) {
            application.disconnect(
                new Result<Client>() {
                    @Override
                    public void onSuccess(Client client) {
                    }

                    @Override
                    public void onError(Error error) {
                    }
                }
            );
        }
    }

    public boolean isRunning() {
        return ((search != null) && search.isSearching());
    }
    
    private CountDownTimer startTimer(long millis) {
        return new CountDownTimer(millis, 250) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "Timer finished. Call completeScan()");
                cancel();
                stopDiscovery();
            }
        }.start();
    }
}
