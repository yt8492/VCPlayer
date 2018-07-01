package jp.kaken.musicplayer;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class TrackListActivity extends AppCompatActivity {

    long[] trackIDs = null;
    List<Track> tracks = null;

    //ボーカルカット後のファイルを入れるフォルダのパス
    String folder = Environment.getExternalStorageDirectory().toString() + "/VCdata";

    //トラック一覧画面
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Intent intent = getIntent();//アルバム一覧画面から渡された値を取得
        if (intent == null) {
            Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
        }

        //ツールバーにタイトルと戻るボタンを設定
        Toolbar toolBar = (Toolbar)findViewById(R.id.toolbar_tracklist);
        setSupportActionBar(toolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("Tracks" );

        //ボーカルカット後のファイルを入れるフォルダを作成
        File dir = new File(folder);
        Log.d("makeDir",dir.getAbsolutePath());
        Toast.makeText(getApplicationContext(),dir.getAbsolutePath(),Toast.LENGTH_LONG);
        if(!dir.exists()){
            boolean result = dir.mkdirs();
            if(!result){
                Toast.makeText(getApplicationContext(),"ディレクトリの作成に失敗しました",Toast.LENGTH_LONG);
            }
        }

        //アルバムID、アルバムタイトル、アルバムアートの設定
        long albumId = intent.getLongExtra("ID",0);
        final String albumTitle = intent.getStringExtra("TITLE");
        final String albumArt = Album.getArtByAlbumId(this,albumId);
        TextView albumName = (TextView)findViewById(R.id.album_title);
        TextView trackCount = (TextView)findViewById(R.id.trackCount);
        ImageView albumTitleArt = (ImageView)findViewById(R.id.album_title_art);
        albumTitleArt.setImageResource(R.drawable.dummy_album_art_slim);
        if(albumArt!=null){
            albumTitleArt.setTag(albumArt);
            ImageGetTask task = new ImageGetTask(albumTitleArt);
            task.execute(albumArt);
        }
        albumName.setText(albumTitle);

        //トラックのリストを作成
        tracks = Track.getItemsByAlbum(this,albumId);
        trackCount.setText(tracks.size() + "曲");
        trackIDs = new long[tracks.size()];
        for(int i=0;i<tracks.size();i++){
            trackIDs[i] = tracks.get(i).id;
        }
        final ListView trackList = (ListView)findViewById(R.id.listview2);
        ListTrackAdapter adapter = new ListTrackAdapter(this,tracks);
        trackList.setAdapter(adapter);//トラックをリスト表示
        //トラック選択時の処理
        trackList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplication(),MusicPlayerActivity.class);
                intent.putExtra("trackIDs", trackIDs);
                intent.putExtra("position",position);
                startActivity(intent);//プレイヤー画面の起動
            }
        });
        registerForContextMenu(trackList);

    }

    //長押しメニューの設定
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(tracks.get(info.position).title);
        //メニューの内容
        switch(v.getId()){
            case R.id.listview2:
                menu.add(Menu.NONE,1,Menu.NONE,"再生");
                menu.add(Menu.NONE,2,Menu.NONE,"ボーカルカット");
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //長押しメニュー選択時の設定
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch (item.getItemId()) {
            case 1://再生を選んだ場合
                Intent intent1 = new Intent(getApplication(),MusicPlayerActivity.class);
                intent1.putExtra("trackIDs", trackIDs);
                intent1.putExtra("position",info.position);
                startActivity(intent1);//プレイヤー画面の起動
                return true;
            case 2://ボーカルカットを選んだ場合
                Toast.makeText(this,"start",Toast.LENGTH_LONG);
                WavVocalCutToMonoral wavVocalCutToMonoral = new WavVocalCutToMonoral();
                File outputFile1 = wavVocalCutToMonoral.wavVocalCut(tracks.get(info.position));//ボーカルカットの実行
                Intent intent2 = new Intent(getApplication(),VcPlayActivity.class);
                intent2.putExtra("trackId",trackIDs[info.position]);
                intent2.putExtra("vcFilePath",outputFile1.getAbsolutePath());
                startActivity(intent2);//プレビュー画面の起動
                // ボーカルカット後のファイルをContentProviderに登録
                MediaScannerConnection.scanFile(getApplicationContext(),new String[]{outputFile1.getAbsolutePath()},null,null);
                Toast.makeText(this,"finish",Toast.LENGTH_LONG);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
