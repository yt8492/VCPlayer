package jp.kaken.musicplayer;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.List;

public class AlbumListActivity extends AppCompatActivity {

    //起動直後の画面
    //アルバム一覧を表示
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final List<Album> albums = Album.getItems(this);//アルバムのリストを取得
        ListView albumList = (ListView)findViewById(R.id.listview1);
        ListAlbumAdapter adapter = new ListAlbumAdapter(this, albums);
        albumList.setAdapter(adapter);//アルバムをリスト表示
        //アルバム選択時の処理
        albumList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplication(),TrackListActivity.class);
                long albumId = albums.get(position).albumId;
                String title = albums.get(position).album;
                String art = albums.get(position).albumArt;
                intent.putExtra("ID", albumId);
                intent.putExtra("TITLE",title);
                intent.putExtra("ART",art);
                startActivity(intent);//トラック一覧画面の起動
            }
        });
    }

}
