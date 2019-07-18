package com.bytedance.videoplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    private boolean isPlay = false;
    private SeekBar seekBar;
    private VideoView videoView;
    private TextView tvRemainTime;
    Button bFullScreen;
    Handler handler = new Handler();

    private String[] mPermissionsArrays = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private final static int REQUEST_PERMISSION = 123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //landscape mode
        if(this.getResources().getConfiguration().orientation ==
                this.getResources().getConfiguration().ORIENTATION_LANDSCAPE){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().hide();
        }
        int progress = 0;
        if(savedInstanceState != null){
            progress = savedInstanceState.getInt("progress");
        }
        videoView = findViewById(R.id.videoView);
        seekBar = findViewById(R.id.sb_video);
        tvRemainTime = findViewById(R.id.tv_remain_time);
        bFullScreen = findViewById(R.id.b_full_screen);
        videoView.setVideoPath("android.resource://" + this.getPackageName() + "/" + R.raw.bytedance);
        final int finalProgress = progress;
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seekBar.setMax(videoView.getDuration());
                seekBar.setProgress(videoView.getDuration());
                videoView.seekTo(finalProgress);
                seekBar.setProgress(finalProgress);
            }
        });
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("hello", "onClick() called with: v = [" + v + "]");
                if (isPlay) {
                    videoView.pause();
                    isPlay = false;
                } else {
                    videoView.start();
                    isPlay = true;
                }
                return false;
            }
        });
        handler.post(new Runnable() {
            @Override
            public void run() {
//                int totalLength = videoView.getDuration();
                seekBar.setProgress(videoView.getCurrentPosition());
                handler.postDelayed(this, 1000);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int remainTime = (seekBar.getMax() - progress) / 1000;
                tvRemainTime.setText(remainTime / 3600 + ":" + remainTime % 3600 / 60 + ":" + remainTime % 60);
                if (fromUser) {
                    videoView.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        bFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //landscape mode
                if(MainActivity.this.getResources().getConfiguration().orientation==
                        MainActivity.this.getResources().getConfiguration().ORIENTATION_LANDSCAPE){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }else{ //portrait mode
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        if (intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            String str = Uri.decode(uri.getPath());
            videoView.setVideoPath(str);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.open_videos:
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 1);
                return  true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
            Uri selectedVideo = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedVideo,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String videoPath = cursor.getString(columnIndex);
            cursor.close();
            if (!checkPermissionAllGranted(mPermissionsArrays)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(mPermissionsArrays, REQUEST_PERMISSION);
                } else {

                }
            } else {
                Toast.makeText(MainActivity.this, "已经获取所有所需权限", Toast.LENGTH_SHORT).show();
                Log.d("hello", videoPath);
                videoView.setVideoPath(videoPath);
            }
        }
    }
    private boolean checkPermissionAllGranted(String[] permissions) {
        // 6.0以下不需要
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        handler.removeCallbacksAndMessages(this);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //save current video progress
        outState.putInt("progress", seekBar.getProgress());
    }
}