package com.skku.grad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DestListAdapter extends BaseAdapter {
    private ArrayList items;
    private Context context;

    public DestListAdapter(ArrayList items, Context mContext) {
        this.items = items;
        this.context = mContext;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view==null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.dest_list_item, viewGroup, false);
        }

        TextView destNameTV = view.findViewById(R.id.destNameTV);
        TextView destLineTV = view.findViewById(R.id.destLineTV);

        String[] destInfo = (String[]) items.get(i);
        destNameTV.setText(destInfo[0]);
        destLineTV.setText(destInfo[1]);

        return view;
    }
}
