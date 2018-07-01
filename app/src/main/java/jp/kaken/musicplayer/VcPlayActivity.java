package jp.kaken.musicplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static jp.kaken.musicplayer.R.id.button_play_vc;

public class VcPlayActivity extends AppCompatActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener,
        Chronometer.OnChronometerTickListener,Runnable, MediaPlayer.OnCompletionListener{

    MediaPlayer player = null;
    private int duration;
    private SeekBar seekbar;
    private Chronometer chronometer;
    ImageButton buttonPlay = null;
    private boolean running;
    private Thread thread;
    private long trackId ;
    private String vcFilePath = null;

    //プレビュー画面
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vc_play);
        Intent intent = getIntent();//トラック一覧画面から渡された値を取得
        if (intent == null) {
            Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
        }

        //ツールバーにタイトルと戻るボタンを設定
        Toolbar toolBar = (Toolbar)findViewById(R.id.toolbar_vcplay);
        setSupportActionBar(toolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("Preview");

        trackId = intent.getLongExtra("trackId",0);
        vcFilePath = intent.getStringExtra("vcFilePath");

        //プレイヤー画面の設定
        Track track = Track.getItemsByTrackId(this, trackId);//現在のトラックを取得
        //タイトル、アーティスト名、アルバム名の表示
        String albumArt = Album.getArtByAlbumId(this, track.albumId);
        TextView trackTitle = (TextView) findViewById(R.id.trackplaytitle_vc);
        TextView trackArtist = (TextView) findViewById(R.id.trackplayartist_vc);
        TextView albumTitle = (TextView) findViewById(R.id.trackplayalbum_vc);
        trackTitle.setText(track.title + " vc");
        trackArtist.setText(track.artist);
        albumTitle.setText(track.album);
        trackTitle.setSelected(true);

        File vcFile = new File(vcFilePath);
        //アルバムアートの表示
        ImageView albumTitleArt = (ImageView) findViewById(R.id.artview_vc);
        if (albumArt != null) {
            albumTitleArt.setImageURI(Uri.parse(albumArt));
        } else {
            albumTitleArt.setImageResource(R.drawable.dummy_album_art);
        }

        player = MediaPlayer.create(this,Uri.fromFile(vcFile));//MediaPlayerの設定

        //再生時間、シークバーの表示
        chronometer = (Chronometer) findViewById(R.id.chronometer_vc);
        seekbar = (SeekBar) findViewById(R.id.seekBar_vc);
        duration = player.getDuration();
        Chronometer durationMeter = (Chronometer) findViewById(R.id.durationmeter_vc);
        durationMeter.setBase(SystemClock.elapsedRealtime() - duration);
        chronometer.setOnChronometerTickListener(this);
        chronometer.setBase(SystemClock.elapsedRealtime() - player.getCurrentPosition());
        seekbar.setProgress(0);
        seekbar.setMax(player.getDuration());
        seekbar.setOnSeekBarChangeListener(this);

        //再生・一時停止ボタンの設定
        findViewById(button_play_vc).setOnClickListener(this);

        running = true;
        start();
        thread = new Thread(this);
        thread.start();
        player.setOnCompletionListener(this);

    }

    //再生終了時の処理
    @Override
    public void onCompletion(MediaPlayer mp) {
        finishPlaying();
        finish();
    }

    public void run(){
        while(running){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (player!=null) {
                handler.sendMessage(Message.obtain(handler, player.getCurrentPosition()));
            }
        }
    }

    //シークバーと再生時間の表示の処理
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (player != null) {
                seekbar.setProgress(msg.what);
                chronometer.setBase(SystemClock.elapsedRealtime() - player.getCurrentPosition());
            }
        }
    };

    public void stopRunning(){
        running = false;
    }
    public void start(){
        if (!player.isPlaying()){
            player.seekTo(player.getCurrentPosition());
            player.start();
        }
    }

    //ボタンクリック時の処理
    @Override
    public void onClick(View v){
        if (v != null){
            switch (v.getId()){
                case button_play_vc://再生・一時停止ボタン
                    buttonPlay = (ImageButton)findViewById(button_play_vc);
                    if (player.isPlaying()){
                        buttonPlay.setImageResource(R.drawable.start);
                        player.pause();
                    }else {
                        buttonPlay.setImageResource(R.drawable.stop);
                        player.start();
                    }
                    break;
            }
        }
    }

    public void finishPlaying(){
        stopRunning();
        finish();
        player.reset();
        player.release();
        player = null;
    }

    //戻るボタンクリック時の処理
    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            finishPlaying();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    //シークバー操作時
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        player.seekTo(seekBar.getProgress());
        chronometer.setBase(SystemClock.elapsedRealtime() - player.getCurrentPosition());
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
