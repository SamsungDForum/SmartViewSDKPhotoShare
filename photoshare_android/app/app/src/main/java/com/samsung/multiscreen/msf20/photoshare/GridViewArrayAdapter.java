package com.samsung.multiscreen.msf20.photoshare;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class GridViewArrayAdapter extends ArrayAdapter<ImageInfo> {

    DisplayImageOptions dispOptions = new DisplayImageOptions.Builder()
        .cacheOnDisk(true)
        .considerExifParams(true)
        .imageScaleType(ImageScaleType.EXACTLY)
        .bitmapConfig(Bitmap.Config.RGB_565)
        .resetViewBeforeLoading(true)
        .delayBeforeLoading(5)
        .build();
    
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    
    public GridViewArrayAdapter(Context context, int resource) {
        super(context, resource);
        this.mInflater = LayoutInflater.from(context);
        this.mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null){
            convertView = mInflater.inflate(R.layout.gridview_item, parent, false);

            holder = new ViewHolder();
            holder.thumb = (ImageView)convertView.findViewById(R.id.thumb);
            holder.placeholder = convertView.findViewById(R.id.grid_stub);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.thumb.setVisibility(View.GONE);
        holder.placeholder.setVisibility(View.GONE);

        ImageInfo imageInfo = getItem(position);

        final View rl = convertView;
        File file = new File(imageInfo.getThumbPath());
        Uri uri = Uri.fromFile(file);
            
        mImageLoader.displayImage(Uri.decode(uri.toString()), holder.thumb, dispOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view,
                    FailReason failReason) {
                // Oops. Failed to load the image. Corrupted??
                rl.setBackgroundColor(Color.BLACK);
                holder.thumb.setImageDrawable(null);
                holder.placeholder.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                holder.thumb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
            }
        });

        return convertView;
    }

    private class ViewHolder {
        ImageView thumb;
        View placeholder;
    }
}
