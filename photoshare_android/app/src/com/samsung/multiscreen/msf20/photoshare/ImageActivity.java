package com.samsung.multiscreen.msf20.photoshare;
 
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
 
public class ImageActivity extends FragmentActivity {
    private static final String TAG = ImageActivity.class.getName();

    public static final String CUR_POS = "curPos";
    
    private ImageAdapter mAdapter;
    private ViewPager mPager;
    private int curPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: " + TAG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pager);

        Intent intent = getIntent();
        ArrayList<ImageInfo> imageInfos = intent.getParcelableArrayListExtra(ImageInfoUtils.IMAGE_INFOS_KEY);
        mAdapter = new ImageAdapter(getSupportFragmentManager(), imageInfos);
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setPageTransformer(true, new DepthPageTransformer());

        if (savedInstanceState != null) {
            curPos = savedInstanceState.getInt(CUR_POS);
        }
    }
 
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: " + TAG);
        super.onResume();

        if (curPos < 0) {
            Intent intent = getIntent();
            curPos = intent.getIntExtra(CUR_POS, 0);
        }
        mPager.setCurrentItem(curPos);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: " + TAG);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CUR_POS, mPager.getCurrentItem());
    }
    
    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: " + TAG);
        super.onStop();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
 
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: " + TAG);
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(CUR_POS, mPager.getCurrentItem());
        setResult(RESULT_OK, resultIntent);
        finish();
        super.onBackPressed();
    }
}
