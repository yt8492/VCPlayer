package jp.kaken.musicplayer;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;


public class WavVocalCutToMonoral {
    private final int FILESIZE_SEEK = 4;
    private final int DATASIZE_SEEK = 40;

    private RandomAccessFile raf = null;
    private File recFile = null;

    //wavヘッダ
    private byte[] RIFF = {'R','I','F','F'};
    private int fileSize = 36;
    private byte[] WAVE = {'W','A','V','E'};
    private byte[] fmt = {'f','m','t',' '};
    private int fmtSize = 16;
    private byte[] fmtID = {1, 0}; // 2byte
    private short chCount = 1;
    private int sampleRate = 44100;
    private int bytePerSec = 44100 * 2;
    private short blockSize = 2;
    private short bitPerSample = 16;
    private byte[] data = {'d', 'a', 't', 'a'};
    private int dataSize = 0;

    public File wavVocalCut(Track track){
        File inputFile = new File(track.path);
        Log.d("inputPath",inputFile.getAbsolutePath());
        String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VCdata";
        String filePath = folder + "/" + track.title + " vc.wav";
        recFile = new File(filePath);
        if (!recFile.exists()) {
            try {
                //wavヘッダ書き込み
                raf = new RandomAccessFile(recFile, "rw");
                raf.seek(0);
                raf.write(RIFF);
                raf.write(littleEndianInteger(fileSize));
                raf.write(WAVE);
                raf.write(fmt);
                raf.write(littleEndianInteger(fmtSize));
                raf.write(fmtID);
                raf.write(littleEndianShort(chCount));
                raf.write(littleEndianInteger(sampleRate));
                raf.write(littleEndianInteger(bytePerSec));
                raf.write(littleEndianShort(blockSize));
                raf.write(littleEndianShort(bitPerSample));
                raf.write(data);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                FileInputStream fis = new FileInputStream(inputFile);
                InputStream is = new BufferedInputStream(fis);
                // オーディオ入力ストリームからデータを読む
                is.skip(FILESIZE_SEEK + DATASIZE_SEEK);
                int dataLength = is.available();
                if (fis.available() % 4 != 0) {
                    dataLength += 4 - (is.available() % 4);
                }
                byte[] data1 = new byte[dataLength];    //byte配列(元データ)
                short[] data2 = new short[dataLength / 2];//short配列(元データ)
                short[] vcdata = new short[dataLength / 4];//short配列(ボーカルカット)
                byte[] data3 = new byte[dataLength / 2];//byte配列(ボーカルカット)
                is.read(data1);//音データ読み込み
                is.close();

                //ステレオ16ビットのためbyte->short変換を一旦行う
                //左チャンネル-右チャンネルでボーカルカット
                for (int i = 0, j = 0, k = 0; i < data1.length; i += 4, j += 2, k++) {
                    data2[j] = (short) ((data1[i] & 0xff) | ((data1[i + 1] << 8) & 0xff00));
                    data2[j + 1] = (short) ((data1[i + 2] & 0xff) | ((data1[i + 3] << 8) & 0xff00));
                    vcdata[k] = (short) ((data2[j] - data2[j + 1]) / 2);
                }
                //short->byte変換
                for (int i = 0, j = 0; i < data2.length; i += 2, j++) {
                    data3[i] = (byte) (vcdata[j]);
                    data3[i + 1] = (byte) (vcdata[j] >> 8);
                }

                //ボーカルカットデータ書き込み
                raf.seek(raf.length());
                raf.write(littleEndianInteger(data3.length));
                raf.write(data3);
                updateDataSize();
                updateFileSize();
                raf.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return recFile;
    }


    // int型をリトルエンディアンのbyte配列に変更
    private byte[] littleEndianInteger(int i){

        byte[] buffer = new byte[4];

        buffer[0] = (byte) i;
        buffer[1] = (byte) (i >> 8);
        buffer[2] = (byte) (i >> 16);
        buffer[3] = (byte) (i >> 24);

        return buffer;

    }

    // short型変数をリトルエンディアンのbyte配列に変更
    private byte[] littleEndianShort(short s){

        byte[] buffer = new byte[2];

        buffer[0] = (byte) s;
        buffer[1] = (byte) (s >> 8);

        return buffer;

    }

    private void updateFileSize(){

        fileSize = (int) (recFile.length() - 8);
        byte[] fileSizeBytes = littleEndianInteger(fileSize);
        try {
            raf.seek(FILESIZE_SEEK);
            raf.write(fileSizeBytes);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // データサイズを更新
    private void updateDataSize(){

        dataSize = (int) (recFile.length() - 44);
        byte[] dataSizeBytes = littleEndianInteger(dataSize);
        try {
            raf.seek(DATASIZE_SEEK);
            raf.write(dataSizeBytes);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}