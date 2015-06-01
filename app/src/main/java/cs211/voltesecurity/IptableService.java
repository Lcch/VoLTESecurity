package cs211.voltesecurity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by wsm on 5/29/15.
 */
public class IptableService extends Service{
    private boolean active;
    @Override
    public void onCreate() {
        super.onCreate();
        active = true;
        new Thread(new Runnable() {
            public void run() {
                while (active) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                    Log.v("IptableService", "hello");
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
