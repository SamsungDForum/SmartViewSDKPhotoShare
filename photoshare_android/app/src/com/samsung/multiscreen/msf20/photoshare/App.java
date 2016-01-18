package com.samsung.multiscreen.msf20.photoshare;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.samsung.multiscreen.util.RunUtil;

public class App extends Application {
    public static final String TAG = App.class.getName();

    DisplayImageOptions.Builder dispOptionsBuilder = new DisplayImageOptions.Builder()
        .cacheOnDisk(true)
        .considerExifParams(true)
        .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
        .bitmapConfig(Bitmap.Config.RGB_565)
        .resetViewBeforeLoading(true);

    private DisplayImageOptions defaultDispOptions = dispOptionsBuilder.build();

    private static App instance;
    private static Config config;
    private static WakeLock wakeLock;

    private PhotoShareWebApplicationHelper photoShare;

    private boolean menuReady = false;
    
    public static App getInstance() {
        return instance;
    }

    public App() {
        instance = this;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();
        
        RunUtil.runInBackground(new Runnable() {

            @Override
            public void run() {
                long startTime = System.nanoTime();
                Looper.prepare();
                ImageInfoUtils.getImageInfos(getApplicationContext()); 
                long endTime = System.nanoTime();
                Log.d(TAG, "getImageInfos execution in " + ((float)(endTime - startTime)/1000000f) + " ms");
            }
        });

        config = Config.newInstance(this);
//        if (config.isDebug()) {
//            wakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());
//            wakeLock.acquire();
//        }

        ImageLoader imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) {
            // Create global configuration and initialize ImageLoader with this configuration
//            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
//                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
//                .diskCacheSize(50 * 1024 * 1024)
//                .defaultDisplayImageOptions(defaultDispOptions)
//                .build();

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheSize(50 * 1024 *1024)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .defaultDisplayImageOptions(defaultDispOptions)
                .build();
            imageLoader.init(config);
        }
        
        photoShare = PhotoShareWebApplicationHelper.getInstance(this);
    }

    @Override
    protected void finalize() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }
    
    public Config getConfig() {
        return config;
    }

    public PhotoShareWebApplicationHelper getPhotoShare() {
        return photoShare;
    }

    public boolean isMenuReady() {
        return menuReady;
    }

    public void setMenuReady(boolean menuReady) {
        this.menuReady = menuReady;
    }

}
