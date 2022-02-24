package com.xiongmaokuaishou.myapplication.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.xiongmaokuaishou.myapplication.R;

import java.util.List;

public class TakeAdapter extends ArrayAdapter<Take> {
    private int resourceId;
    public List<Take> mlist;
    // 适配器的构造函数，把要适配的数据传入这里
    public TakeAdapter(Context context, int textViewResourceId, List<Take> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
        this.mlist=objects;
    }
    public void updateList(List<Take> listInfo){
        Log.d("qxj21","---"+listInfo.size());
        mlist.clear();
        Log.d("qxj21","---"+listInfo.size());
        mlist.addAll(listInfo);
        Log.d("qxj2","---"+mlist.size());
        Log.d("qxj2","---"+listInfo.size());
        notifyDataSetChanged();
    }
    @Nullable
    @Override
    public Take getItem(int position) {
        return mlist.get(position);
    }
    // convertView 参数用于将之前加载好的布局进行缓存
    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
         Take take = getItem(position); //获取当前项的Fruit实例
        Log.d("qxj2-----","---"+position);
        // 加个判断，以免ListView每次滚动时都要重新加载布局，以提高运行效率
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            // 避免ListView每次滚动时都要重新加载布局，以提高运行效率
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            // 避免每次调用getView()时都要重新获取控件实例
            viewHolder = new ViewHolder();
            viewHolder.takeCode = view.findViewById(R.id.take_code);
            viewHolder.orderID = view.findViewById(R.id.take_orderID);
            // 将ViewHolder存储在View中（即将控件的实例存储在其中）
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        // 获取控件实例，并调用set...方法使其显示出来
        viewHolder.takeCode.setText( take.getTakeCode());
        viewHolder.orderID.setText(take.getOrderID());
        return view;
    }

    // 定义一个内部类，用于对控件的实例进行缓存
    class ViewHolder {
        TextView takeCode;
        TextView orderID;
    }
}