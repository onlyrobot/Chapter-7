package com.bytedance.videoplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    private boolean isPlay = false;
    private SeekBar seekBar;
    private VideoView videoView;
    private TextView tvRemainTime;
    Button bFullScreen;
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            String str = Uri.decode(uri.getEncodedPath());
        }

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