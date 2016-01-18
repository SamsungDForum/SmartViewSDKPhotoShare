package com.samsung.multiscreen.msf20.photoshare;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.samsung.multiscreen.Channel;
import com.samsung.multiscreen.Message;
import com.samsung.multiscreen.msf20.sdk.WebSocketMessage;
 
public class ImageFragment extends Fragment {
    public static final String TAG = ImageFragment.class.getName();

    private String imagePath;
    private ProgressBar progressBar;
    
    public static ImageFragment newInstance(int position, String imagePath) {
        ImageFragment imageFrag = new ImageFragment();
        
        // Supply val input as an argument.
        Bundle args = new Bundle();
        args.putString("path", imagePath);
        imageFrag.setArguments(args);
        return imageFrag;
    }
 
    public ImageFragment() {
        super();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
//        Log.d(TAG, "onCreate: " + TAG);
        super.onCreate(savedInstanceState);
        imagePath = getArguments() != null ? getArguments().getString("path") : null;
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
//        Log.d(TAG, "onCreateView: " + TAG);
        final View layoutView = inflater.inflate(R.layout.fragment_image, container,
                false);
        ImageView iv = (ImageView)layoutView.findViewById(R.id.imageView1);
        final View placeholder = layoutView.findViewById(R.id.imagePlaceholder);
        progressBar = (ProgressBar)layoutView.findViewById(R.id.sending);

        ImageLoader imageLoader = ImageLoader.getInstance();
        File file = new File(imagePath);
        if (file.exists()) {
            Uri uri = Uri.fromFile(file);
            
            layoutView.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
                @Override
                public void onSwipeTop() {
                    // Workaround a nasty bug in which a next fragment is used 
                    // for the onTouchListener after swiping to a previous 
                    // fragment.
                    String imagePath = (String)layoutView.getTag();
                    if (imagePath != null) {
                        sendRawBitmap(imagePath, App.getInstance().getPhotoShare().getApplication());
                    } else {
                        Log.d(TAG, "Null image uri. Image path: " + imagePath);
                    }
                }
            });
            imageLoader.displayImage(Uri.decode(uri.toString()), iv, new SimpleImageLoadingListener() {
                
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                }
                
                @Override
                public void onLoadingFailed(String imageUri, View view,
                        FailReason failReason) {
                    try {
                        // Oops. Failed to load the image. Corrupted??
                        TextView tv = (TextView)layoutView.findViewById(R.id.imageText);
                        tv.setText(Constants.CORRUPT_IMAGE_TEXT);
                        placeholder.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.d(TAG, Log.getStackTraceString(e));
                    }
                }
                
                @Override
                public void onLoadingComplete(String imageUri, final View view, Bitmap loadedImage) {

                    Uri fileUri = Uri.parse(imageUri);
                    layoutView.setTag(fileUri.getPath());
                    ImageView imageView = (ImageView)view;
                    imageView.setImageBitmap(loadedImage);
                    imageView.setVisibility(View.VISIBLE);
                }
                
                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                }
            });
        } else {
            // Show a image placeholder...
            TextView tv = (TextView)layoutView.findViewById(R.id.imageText);
            tv.setText(Constants.MISSING_IMAGE_TEXT);
            placeholder.setVisibility(View.VISIBLE);
        }
        
        return layoutView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
//        Log.d(TAG, "onActivityCreated: " + TAG);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
//        Log.d(TAG, "onResume: " + TAG);
        super.onResume();
    }
    
    private void sendRawBitmap(String realPath, final Channel channel) {
        final File file = new File(realPath);

        if (file.exists() && (channel != null) && channel.isConnected()) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            new Thread() {
                public void run() {
                    try {
                        int fileSize = (int)file.length();

                        byte[] bytes = new byte[fileSize];
                        InputStream is = new BufferedInputStream(new FileInputStream(file));
                        is.read(bytes, 0, fileSize);
                        is.close();

                        channel.publish(WebSocketMessage.PICTURE_METHOD, "", Message.TARGET_HOST, bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    hideProgress();
                }
            }.start();
        } else {
            Toast.makeText(getActivity(), 
                    Constants.DISCONNECTED_MSG, 
                    Toast.LENGTH_LONG).show();
        }
    }

    private void hideProgress() {
        if (progressBar.getVisibility() == ProgressBar.VISIBLE) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                 public void run() {
                    progressBar.setVisibility(ProgressBar.GONE);
                 } 
            });
        }
    }
}
