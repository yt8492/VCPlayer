package jp.kaken.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_PERMISSION = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 23) {
            // permissionの確認
            checkPermission();
        } else {
            setupSearch();
        }
    }

    private void setupSearch() {
        Intent intent = new Intent(getApplication(), AlbumListActivity.class);
        startActivity(intent);
    }

    // Runtime Permission check
    private void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            setupSearch();
        }
        // 拒否していた場合
        else {
            requestLocationPermission();
        }
    }

    // 許可を求める
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);

        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "許可されないとアプリが実行できません", Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, REQUEST_PERMISSION);

        }
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupSearch();
                return;

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}