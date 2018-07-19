package com.example.myappandroid;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class CustomAdapter extends BaseAdapter {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private ArrayList<String> mData = new ArrayList<>();
    private TreeSet<Integer> sectionHeader = new TreeSet<>();
    private LayoutInflater mInflater;
    private List<Todo> todos;
    private Context context;
    public CustomAdapter(Context context, List<Todo> todos) {
        mInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.todos = todos;
    }
    public void addItem(final String item) {
        mData.add(item);
        notifyDataSetChanged();
    }
    public void addSectionDataItem(final String item) {
        mData.add(item);
        sectionHeader.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int rowType = getItemViewType(position);
        if (convertView == null) {
            holder = new ViewHolder();
            switch (rowType) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.cell, null);
                    holder.checkBox = convertView.findViewById(R.id.checkbox);
                    break;
                case TYPE_SEPARATOR:
                    convertView = mInflater.inflate(R.layout.header, null);
                    holder.textView = convertView.findViewById(R.id.textSeparator);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (holder.textView != null) {
            holder.textView.setText(mData.get(position));

        } else if (holder.checkBox != null) {
            holder.checkBox.setText(mData.get(position));
            Todo currentTodo = new Todo();
            for (int i = 0; i < todos.size(); i++) {
                if (Objects.equals(mData.get(position), todos.get(i).text)) {
                    currentTodo = todos.get(i);
                    holder.checkBox.setChecked(currentTodo.isCompleted);
                }
            }
            if (currentTodo.isCompleted) crossOut(holder.checkBox);
            holder.checkBox.setTag(currentTodo);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView != null) {
                        Todo todoInFocus = (Todo) buttonView.getTag();
                        if (todoInFocus.isCompleted == isChecked) return;
                        if (isChecked) crossOut(buttonView); else removeLineThrough(buttonView);
                        todoInFocus.isCompleted = isChecked;
                        String url = context.getString(R.string.apiRequest) + todoInFocus.project_id +
                                "/todos/" + todoInFocus.id + "/update_mobile";
                        JsonObject params = new JsonObject();
                        params.addProperty("id", todoInFocus.id);
                        params.addProperty("isCompleted", todoInFocus.isCompleted);
                        Ion.with(context)
                                .load("PATCH", url)
                                .setJsonObjectBody(params)
                                .asJsonObject()
                                .setCallback(new FutureCallback<JsonObject>() {
                                    @Override
                                    public void onCompleted(Exception e, JsonObject result) {
                                    }
                                });
                    }
                }
            });
        }
        return convertView;
    }
    private void crossOut(TextView textView) {
        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }
    private void removeLineThrough(TextView textView) {
        textView.setPaintFlags(0);
    }

    public static class ViewHolder {
        public TextView textView;
        public CheckBox checkBox;
    }
}

