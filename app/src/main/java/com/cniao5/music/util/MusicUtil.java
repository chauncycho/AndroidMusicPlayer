package com.cniao5.music.util;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;

import com.cniao5.music.MainActivity;
import com.cniao5.music.bean.Music;

import java.util.ArrayList;
import java.util.List;

public class MusicUtil {
    //从文件获取歌曲
    public static List<Music> getMusicDate(Context context){
        List<Music> list = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);//从文件获取歌曲
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            String author = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

            if (author.equals("<unknown>")){
                author = "未知艺术家";
            }

            if (duration > 20000){
                Music music = new Music(name,author,path,duration);
                list.add(music);
            }
        }
        return list;
    }

    public static boolean checkPublishPermission(Activity activity) {
        //1、ActivityCompat.checkSelfPermission() 判断权限
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            //2、ActivityCompat.requestPermissions() 申请权限
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(activity
                        , permissions.toArray(new String[0]),
                        MainActivity.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return false;
            }
        }
        return true;
    }
}
