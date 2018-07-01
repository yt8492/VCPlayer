package jp.kaken.musicplayer;

import android.content.Intent;
import android.os.Handler;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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

import static jp.kaken.musicplayer.R.id.button_back;
import static jp.kaken.musicplayer.R.id.button_next;
import static jp.kaken.musicplayer.R.id.button_play;

public class MusicPlayerActivity extends AppCompatActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener,
Chronometer.OnChronometerTickListener,Runnable, MediaPlayer.OnCompletionListener {

    MediaPlayer player = null;
    private int duration;
    private SeekBar seekbar;
    private Chronometer chronometer;
    ImageButton buttonPlay = null;
    private boolean running;
    private Thread thread;
    private int nowPosition;
    private long[] trackIDs = null;

    //プレイヤー画面
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        Intent intent = getIntent();//トラック一覧画面から渡された値を取得
        if (intent == null) {
            Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
        }

        //ツールバーにタイトルと戻るボタンを設定
        Toolbar toolBar = (Toolbar)findViewById(R.id.toolbar_musicplay);
        setSupportActionBar(toolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("Player");

        //プレイヤー画面の設定
        trackIDs = intent.getLongArrayExtra("trackIDs");
        nowPosition = intent.getIntExtra("position",-1);
        if (nowPosition != -1) {
            Track track = Track.getItemsByTrackId(this, trackIDs[nowPosition]);//現在のトラックを取得
            Uri trackUri = track.uri;
            //タイトル、アーティスト名、アルバム名の表示
            String albumArt = Album.getArtByAlbumId(this, track.albumId);
            TextView trackTitle = (TextView) findViewById(R.id.trackplaytitle);
            TextView trackArtist = (TextView) findViewById(R.id.trackplayartist);
            TextView albumTitle = (TextView) findViewById(R.id.trackplayalbum);
            trackTitle.setText(track.title);
            trackArtist.setText(track.artist);
            albumTitle.setText(track.album);
            trackTitle.setSelected(true);

            //アルバムアートの表示
            ImageView albumTitleArt = (ImageView) findViewById(R.id.artview);
            if (albumArt != null) {
                albumTitleArt.setImageURI(Uri.parse(albumArt));
            } else {
                albumTitleArt.setImageResource(R.drawable.dummy_album_art);
            }

            player = MediaPlayer.create(this, trackUri);//MediaPlayerの設定

            //再生時間、シークバーの表示
            chronometer = (Chronometer) findViewById(R.id.chronometer);
            seekbar = (SeekBar) findViewById(R.id.seekBar);
            duration = player.getDuration();
            Chronometer durationMeter = (Chronometer) findViewById(R.id.durationmeter);
            durationMeter.setBase(SystemClock.elapsedRealtime() - duration);
            chronometer.setOnChronometerTickListener(this);
            chronometer.setBase(SystemClock.elapsedRealtime() - player.getCurrentPosition());
            seekbar.setProgress(0);
            seekbar.setMax(player.getDuration());
            seekbar.setOnSeekBarChangeListener(this);

            //再生・一時停止ボタン、戻るボタン、進むボタンの設定
            findViewById(button_play).setOnClickListener(this);
            findViewById(R.id.button_back).setOnClickListener(this);
            findViewById(R.id.button_next).setOnClickListener(this);

            running = true;
            start();
            thread = new Thread(this);
            thread.start();
            player.setOnCompletionListener(this);
        }

    }

    //再生終了時の処理
    @Override
    public void onCompletion(MediaPlayer mp) {
        finishPlaying();
        if (nowPosition+1 < trackIDs.length) {
            //次の楽曲を再生
            Intent intent = new Intent(getApplication(), MusicPlayerActivity.class);
            intent.putExtra("position", nowPosition + 1);
            intent.putExtra("trackIDs", trackIDs);
            startActivity(intent);
        }
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
                case button_play://再生・一時停止ボタン
                    buttonPlay = (ImageButton)findViewById(button_play);
                    if (player.isPlaying()){
                        buttonPlay.setImageResource(R.drawable.start);
                        player.pause();
                    }else {
                        buttonPlay.setImageResource(R.drawable.stop);
                        player.start();
                    }
                    break;
                case button_back://戻るボタン
                    if (player.isPlaying()){
                        if (player.getCurrentPosition()<5000){
                            if (nowPosition >0) {
                                Intent intent = new Intent(getApplication(), MusicPlayerActivity.class);
                                intent.putExtra("position", nowPosition - 1);
                                intent.putExtra("trackIDs", trackIDs);
                                startActivity(intent);
                                finish();
                            }
                        }else {
                            player.seekTo(0);
                            chronometer.setBase(SystemClock.elapsedRealtime() - player.getCurrentPosition());
                        }
                    }else {
                        if (nowPosition >0) {
                            Intent intent = new Intent(getApplication(), MusicPlayerActivity.class);
                            intent.putExtra("position", nowPosition - 1);
                            intent.putExtra("trackIDs", trackIDs);
                            startActivity(intent);
                            finish();
                        }
                    }
                    break;
                case button_next://進むボタン
                    onCompletion(player);
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
