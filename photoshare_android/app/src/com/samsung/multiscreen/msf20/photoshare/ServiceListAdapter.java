package com.samsung.multiscreen.msf20.photoshare;

import java.util.Comparator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.samsung.multiscreen.Service;
import com.samsung.multiscreen.msf20.sdk.ServiceWrapper;

public class ServiceListAdapter extends ArrayAdapter<ServiceWrapper> {

    private Context context;
    private int layoutResourceId;
    private LayoutInflater mInflater;

    public ServiceListAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        this.layoutResourceId = resource;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResourceId, parent, false);

            convertView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.right_to_left));
        }

        TextView fnTextView = (TextView)convertView.findViewById(R.id.name);
        TextView idTextView = (TextView)convertView.findViewById(R.id.ip);

        ServiceWrapper wrapper = getItem(position);
        if (wrapper != null) {
            Service service = wrapper.getService();
            fnTextView.setText(service.getName());
            idTextView.setText(service.getId());
        }

        return convertView;
    }

    public boolean contains(ServiceWrapper service) {
        return (getPosition(service) >= 0);
    }

    public void replace(ServiceWrapper service) {
        int position = getPosition(service);
        if (position >= 0) {
            remove(service);
            insert(service, position);
        }
    }
    
    public void alphaSort() {
        sort(alphaComparator);
    }
    
    private Comparator<ServiceWrapper> alphaComparator = new Comparator<ServiceWrapper>() {

        @Override
        public int compare(ServiceWrapper wrapper1, ServiceWrapper wrapper2) {
            Service s1 = wrapper1.getService(); 
            Service s2 = wrapper2.getService(); 
            return s1.getName().compareTo(s2.getName());
        }
    };
}
