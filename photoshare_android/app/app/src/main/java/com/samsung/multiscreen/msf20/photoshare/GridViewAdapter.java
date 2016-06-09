package com.samsung.multiscreen.msf20.photoshare;

import java.io.File;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class GridViewAdapter extends SimpleCursorAdapter {

    private HashMap<Integer, Integer> idMap;
    private HashMap<Integer, String> thumbnailMap;
    
    private ImageLoader imageLoader;
    
    private Cursor myCursor;
    private Context myContext;

    public GridViewAdapter(Context context, int layout, Cursor c, String[] from,
            int[] to, int flags) {
        super(context, layout, c, from, to, flags);

        this.myCursor = c;
        this.myContext = context;
        this.imageLoader = ImageLoader.getInstance();
        
        int count = (c != null)?c.getCount():0;
        this.idMap = new HashMap<Integer, Integer>(count);
        this.thumbnailMap = new HashMap<Integer, String>(count);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.gridview_item, parent, false);

            holder = new ViewHolder();
            holder.thumb = (ImageView)convertView.findViewById(R.id.thumb);
            holder.placeholder = convertView.findViewById(R.id.grid_stub);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.thumb.setVisibility(View.GONE);
        holder.placeholder.setVisibility(View.GONE);

        // Try to get image id from cache.
        Integer myIDInt = idMap.get(position);
        final int myID;
        if (myIDInt != null) {
            myID = myIDInt.intValue();
        } else {
            myCursor.moveToPosition(position);
            myID = myCursor.getInt(myCursor.getColumnIndex(MediaStore.Images.Media._ID));
            idMap.put(position, myID);
        }

        // Try to get the thumbnail page from cache.
        String thumbPath = thumbnailMap.get(myID);
        if (thumbPath == null) {
            // Did not find the associated thumbnail path to this image id.
            String[] thumbColumns = {ImageInfoUtils.thumb_DATA};
            CursorLoader thumbCursorLoader = new CursorLoader(
                    myContext, 
                    ImageInfoUtils.thumbUri, 
                    thumbColumns, 
                    ImageInfoUtils.thumb_IMAGE_ID + "=" + myID, 
                    null, 
                    null);
            Cursor thumbCursor = thumbCursorLoader.loadInBackground();

            if (thumbCursor.moveToFirst()) {
                int thColumnIndex = thumbCursor.getColumnIndex(ImageInfoUtils.thumb_DATA);
                thumbPath = thumbCursor.getString(thColumnIndex);
                thumbnailMap.put(myID, thumbPath);
            }

            thumbCursor.close();
        }

        final View rl = convertView;
        if (thumbPath != null) {
            File file = new File(thumbPath);
            if (!file.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap myBitmap = MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(), myID, MediaStore.Images.Thumbnails.MICRO_KIND, options);
                if (myBitmap != null) {
                    myBitmap.recycle();
                }
            }

            Uri thumbUri = Uri.fromFile(file);
            
            imageLoader.displayImage(Uri.decode(thumbUri.toString()), holder.thumb, new SimpleImageLoadingListener() {
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
        } else {
            // Oops. Could not get the thumbnail path even though the image id exists
            rl.setBackgroundColor(Color.BLACK);
            holder.thumb.setImageDrawable(null);
            holder.placeholder.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
    
    private class ViewHolder {
        ImageView thumb;
        View placeholder;
    }
}
