package com.cniao5.music.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cniao5.music.R;
import com.cniao5.music.bean.Music;

import java.text.SimpleDateFormat;
import java.util.List;

public class MusicAdapter extends BaseAdapter {

    private List<Music> list;
    private Context context;
    private LayoutInflater inflater;

    public MusicAdapter(List<Music> list, Context context) {
        this.list = list;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list==null?0:list.size();
    }

    @Override
    public Music getItem(int position) {
        return list==null?null:list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list==null?0:position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHoulder viewHoulder = null;
        if (view == null){
            viewHoulder = new ViewHoulder();
            view = inflater.inflate(R.layout.list_item,null);
            viewHoulder.img = (ImageView) view.findViewById(R.id.img);
            viewHoulder.name = (TextView) view.findViewById(R.id.tv_name);
            viewHoulder.author = (TextView) view.findViewById(R.id.tv_author);
            viewHoulder.duration = (TextView) view.findViewById(R.id.tv_duration);
            view.setTag(viewHoulder);
        }else {
            viewHoulder = (ViewHoulder) view.getTag();
        }

        viewHoulder.img.setImageResource(R.mipmap.music);
        viewHoulder.name.setText(list.get(position).getName());
        viewHoulder.author.setText(list.get(position).getAuthor());
        viewHoulder.duration.setText(getduration(list.get(position).getDuration()));
        return view;
    }

    private String getduration(long duration){
        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
        String time = format.format(duration);
        return time;
    }

    class ViewHoulder{
        ImageView img;
        TextView name;
        TextView author;
        TextView duration;
    }
}
