package com.cniao5.music.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.cniao5.music.MainActivity;
import com.cniao5.music.R;
import com.cniao5.music.bean.Music;

import java.io.IOException;

public class MusicService extends Service{

    public static final String SERVICE_ACTION = "com.cniao5.Service";
    private int mNewMusic;
    private Music mMusic;
    private MediaPlayer mPlayer = new MediaPlayer();
    private int state = 0x11;//0x11:第一次播放歌曲  0x12:暂停 0x13:继续播放
    private int mCurPosition, mDuration;
    private Notification.Builder builder;

    @Override
    public void onCreate() {
        super.onCreate();

        //注册广播
        MyBroadcastReceiver receiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter(SERVICE_ACTION);
        registerReceiver(receiver, filter);

        //发出前台通知
        builder = new Notification.Builder(this);
        builder.setTicker("音乐播放器");
        builder.setSmallIcon(R.mipmap.music);
        builder.setContentTitle("音乐播放器");
        builder.setContentInfo("音乐播放器");
        PendingIntent pi = PendingIntent.getActivity(this,0,
                new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        startForeground(0, notification);
        manager.notify(0,notification);


        //歌曲播放完成，向Activity发送广播
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Intent intent = new Intent(MainActivity.ACTIVITY_ACTION);
                intent.putExtra("over", true);
                sendBroadcast(intent);
                mCurPosition = 0;
                mDuration = 0;
            }
        });
    }

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mNewMusic = intent.getIntExtra("newMusic", -1);//接受是否是新歌曲
            if (mNewMusic != -1) {
                mMusic = (Music) intent.getSerializableExtra("music");//获得歌曲对象
                if (mMusic != null) {
                    playMusic(mMusic);
                    state = 0x12;
                }
            }

            int isPlay = intent.getIntExtra("isPlay", -1);
            if (isPlay != -1) {
                switch (state) {
                    case 0x11://第一次播放
                        mMusic = (Music) intent.getSerializableExtra("music");//获得歌曲对象
                        playMusic(mMusic);
                        state = 0x12;//设置下一次按播放、停止键的状态
                        break;
                    case 0x12://暂停
                        mPlayer.pause();
                        state = 0x13;//设置下一次按播放、停止键的状态
                        break;
                    case 0x13://继续播放
                        mPlayer.start();
                        state = 0x12;//设置下一次按播放、停止键的状态
                        break;
                }
            }

            //获取进度条位置
            int progress = intent.getIntExtra("progress", -1);
            if (progress != -1) {
                mCurPosition = (int) (((progress * 1.0) / 100) * mDuration);
                mPlayer.seekTo(mCurPosition);
            }

            Intent intentActivity = new Intent(MainActivity.ACTIVITY_ACTION);
            intentActivity.putExtra("state", state);
            sendBroadcast(intentActivity);//将当前状态发送给activity
        }
    }

    //播放音乐
    public void playMusic(final Music music) {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            try {
                builder.setContentText(music.getName()+"-正在播放中");//显示正在播放的音乐
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = builder.build();
                startForeground(0, notification);
                manager.notify(0,notification);

                Intent intent = new Intent(MainActivity.ACTIVITY_ACTION);
                intent.putExtra("musicName",music.getName());
                sendBroadcast(intent);


                mPlayer.setDataSource(music.getPath());
                mPlayer.prepare();
                mPlayer.start();
                mDuration = mPlayer.getDuration();//获得歌曲时长

                //设置线程，不断向Activity发送广播，告知当前播放的位置与时间
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (mCurPosition < mDuration) {
                            try {
                                sleep(1000);
                                mCurPosition = mPlayer.getCurrentPosition();//获得当前进度位置
                                Intent intent = new Intent(MainActivity.ACTIVITY_ACTION);
                                intent.putExtra("curPosition", mCurPosition);
                                intent.putExtra("duration", mDuration);
                                sendBroadcast(intent);//将当前位置和总时长发送给activity
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        super.unregisterReceiver(receiver);
        unregisterReceiver(receiver);
    }
}
