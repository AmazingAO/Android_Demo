package com.example.delectbuttoninlistview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.util.List;

public class MyAdapter extends ArrayAdapter<String> {
    public MyAdapter(Context context, int textViewResourceId, List<String> objects){
        super(context,textViewResourceId,objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.my_list_view_item,null);
        }else {
            view = convertView;
        }
        TextView textView = (TextView)view.findViewById(R.id.text_view);
        textView.setText(getItem(position));
        return view;
    }
}
