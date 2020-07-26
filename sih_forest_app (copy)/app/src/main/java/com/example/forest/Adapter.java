package com.example.forest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class Adapter extends BaseAdapter {
    private Context context;
    private ArrayList<Model> ModelArrayList;

    public Adapter(Context context, ArrayList<Model> ModelArrayList) {

        this.context = context;
        this.ModelArrayList = ModelArrayList;
    }
    public void remove(int position) {
        ModelArrayList.remove(position);
        notifyDataSetChanged();
    }
    @Override
    public int getViewTypeCount() {
        return getCount();
    }
    @Override
    public int getItemViewType(int position) {

        return position;
    }
    @Override
    public int getCount() {
        return ModelArrayList.size();
    }
    @Override
    public Object getItem(int position) {
        return ModelArrayList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null, true);

            holder.tvname =  convertView.findViewById(R.id.tv);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.tvname.setText(ModelArrayList.get(position).getName());
        return convertView;
    }
    private class ViewHolder {

        protected TextView tvname;
    }

}
