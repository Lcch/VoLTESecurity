package cs211.voltesecurity;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.Buffer;

/**
 * Created by wsm on 5/29/15.
 */
public class IptableService extends Service{
    private boolean active;
    private File config;
    @Override
    public void onCreate() {
        super.onCreate();
        active = true;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        config = new File(path, "config.txt");

        if (!config.exists()) {
            Log.v("myTag", "file not found");
        }
        new Thread(new Runnable() {
            public void run() {
                while (active) {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(config));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            Log.v("myTag", line);
                         }
                        Log.v("myTag", "fuck!");
                        reader.close();
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Log.v("IptableService", "hello");
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        active = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
