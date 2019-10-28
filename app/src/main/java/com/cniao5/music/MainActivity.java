package com.cniao5.music;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cniao5.music.adapter.MusicAdapter;
import com.cniao5.music.bean.Music;
import com.cniao5.music.service.MusicService;
import com.cniao5.music.util.MusicUtil;

import java.util.List;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener,
        View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    public static final String ACTIVITY_ACTION = "com.cniao5.activity";
    private ListView mListView;
    private ImageButton mBtnPlay, mBtnPrev, mBtnNext, mBtnModal;
    private TextView mTvTime;
    private SeekBar mSeekBar;
    private TextView currentMusic;
    private MusicAdapter mAdapter;
    private List<Music> mList;
    private Music mMusic;
    private Context mContext;
    private int mIndex;
    private int state = 0x11;
    private int flag = 0;//0:列表循环 1：单曲循环 2：随机播放
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private boolean mPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }


    private void initView() {
        //注册组件
        mListView = (ListView) findViewById(R.id.list_view);
        mBtnPrev = (ImageButton) findViewById(R.id.img_prev);
        mBtnPlay = (ImageButton) findViewById(R.id.img_play);
        mBtnNext = (ImageButton) findViewById(R.id.img_next);
        mBtnModal = (ImageButton) findViewById(R.id.img_modal);
        mTvTime = (TextView) findViewById(R.id.tv_time);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        currentMusic = (TextView) findViewById(R.id.current_music);

        //获取上下文
        mContext = MainActivity.this;

        //获取权限
        mPermission = MusicUtil.checkPublishPermission(this);
        if (mPermission) {
            mList = MusicUtil.getMusicDate(mContext);//
        }

        //注册适配器
        mAdapter = new MusicAdapter(mList, this);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);
        mBtnPrev.setOnClickListener(this);
        mBtnPlay.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnModal.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);

        //注册广播
        MyReceiver receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter(ACTIVITY_ACTION);
        registerReceiver(receiver, filter);

        //启动服务
        Intent intent = new Intent(mContext, MusicService.class);
        startService(intent);

        mPreferences = getSharedPreferences("Date",0);
        mEditor = mPreferences.edit();
    }

    //请求读取文件权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                for (int ret:grantResults){
                    if (ret != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                }
                mPermission = true;
                break;
            default:
                break;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mIndex = position;//获得当前下标
        mMusic = mList.get(position);
        Intent intent = new Intent(MusicService.SERVICE_ACTION);
        intent.putExtra("music", mMusic);
        intent.putExtra("newMusic", 1);
        sendBroadcast(intent);//发送广播到Service服务
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MusicService.SERVICE_ACTION);
        switch (v.getId()) {
            case R.id.img_prev://上一曲
                if (mIndex == 0) {
                    mIndex = mList.size() - 1;
                } else {
                    mIndex--;
                }
                mMusic = mList.get(mIndex);
                intent.putExtra("newMusic", 1);
                intent.putExtra("music", mMusic);
                break;
            case R.id.img_play://播放、暂停
                if (mMusic == null) {
                    mMusic = mList.get(mIndex);//第一次进入播放器，播放第一首歌曲
                    intent.putExtra("music", mMusic);
                }
                intent.putExtra("isPlay", 1);//当前是否正在播放歌曲
                break;
            case R.id.img_next://下一曲
                if (mIndex == mList.size() - 1) {
                    mIndex = 0;
                } else {
                    mIndex++;
                }
                mMusic = mList.get(mIndex);
                intent.putExtra("newMusic", 1);
                intent.putExtra("music", mMusic);
                break;
            case R.id.img_modal:
                flag++;
                if (flag > 2) {
                    flag = 0;
                }
                if (flag == 0) {//列表循环
                    mBtnModal.setImageResource(R.mipmap.order);
                } else if (flag == 1) {//单曲循环
                    mBtnModal.setImageResource(R.mipmap.single);
                } else {//随机播放
                    mBtnModal.setImageResource(R.mipmap.random);
                }
                break;
        }
        sendBroadcast(intent);
    }

    //当进度条改变时调用
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    //当开始拖动进度条的时候调用
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    //当拖动条停止的时候调用
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Intent intent = new Intent(MusicService.SERVICE_ACTION);
        intent.putExtra("progress", mSeekBar.getProgress());//获取进度条位置
        sendBroadcast(intent);
    }

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            state = intent.getIntExtra("state", state);
            //接收到播放、停止操作时，切换播放图标
            switch (state) {
                case 0x11:
                    mBtnPlay.setImageResource(R.mipmap.play);
                    break;
                case 0x12:
                    mBtnPlay.setImageResource(R.mipmap.pause);
                    break;
                case 0x13:
                    mBtnPlay.setImageResource(R.mipmap.play);
                    break;
            }

            //接收当前播放歌曲名的广播
            String currentMusicName = intent.getStringExtra("musicName");//当前设置歌曲名
            if (currentMusicName != null) {
                currentMusic.setText(currentMusicName);
            }

            //接收进度条的广播
            int curPosition = intent.getIntExtra("curPosition", -1);
            int duration = intent.getIntExtra("duration", -1);
            //设置进度条
            if (curPosition != -1) {
                mSeekBar.setProgress((int) ((curPosition * 1.0) / duration * 100));//为进度条设置当前进度
                mTvTime.setText(initTime(curPosition, duration));//显示时间
            }

            //歌曲播放完成时，广播"over"
            boolean isOver = intent.getBooleanExtra("over", false);
            if (isOver == true) {
                Intent intentService = new Intent(MusicService.SERVICE_ACTION);
                if (flag == 0) {//列表循环
                    if (mIndex == mList.size() - 1) {
                        mIndex = 0;
                    } else {
                        mIndex++;
                    }
                    playMusic(intentService);
                } else if (flag == 1) {//单曲循环
                    playMusic(intentService);
                } else {//随机播放
                    mIndex = (int) (Math.random() * mList.size());
                    playMusic(intentService);
                }

                mEditor.putInt("index",mIndex);
                mEditor.commit();
            }

        }
    }

    private void playMusic(Intent intentService) {
        mMusic = mList.get(mIndex);//获取列表中的音乐
        intentService.putExtra("newMusic", 1);
        intentService.putExtra("music", mMusic);
        sendBroadcast(intentService);//将该音乐广播到服务
    }

    //时间格式转换
    private String initTime(int curPosition, int duration) {
        int curMinute = curPosition / 1000 / 60;//分
        int curSecond = curPosition / 1000 % 60;//秒
        int durMinute = duration / 1000 / 60;//分
        int durSecond = duration / 1000 % 60;//秒
        return getTime(curMinute) + ":" + getTime(curSecond) + "/" + getTime(durMinute) + ":" + getTime(durSecond);
    }

    //显示时间格式
    private String getTime(int time) {
        if (time < 10) {
            return "0" + time;
        } else {
            return time + "";
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //返回
        if (keyCode == KeyEvent.KEYCODE_BACK){
            mEditor.putInt("stateSave", state);
            mEditor.putInt("index", mIndex);
            mEditor.commit();

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE,0,1,"退出");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()){
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("提示");
                builder.setMessage("您确定要退出吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(mContext, MusicService.class);
                        stopService(intent);
                        mEditor.clear();
                        System.exit(0);
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                break;

        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        super.unregisterReceiver(receiver);
        unregisterReceiver(receiver);
    }
}
