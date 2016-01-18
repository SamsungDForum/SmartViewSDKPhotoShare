package com.samsung.multiscreen.msf20.photoshare;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ImageAdapter extends FragmentStatePagerAdapter {

    private ArrayList<ImageInfo> imageInfos;

    public ImageAdapter(FragmentManager fragmentManager, 
            ArrayList<ImageInfo> imageInfos) {
        super(fragmentManager);
        this.imageInfos = imageInfos;
    }

    @Override
    public int getCount() {
        return imageInfos.size();
    }

    @Override
    public Fragment getItem(int position) {
        String realPath = imageInfos.get(position).getImagePath();
        return ImageFragment.newInstance(position, realPath);
    }
}
