package cs211.voltesecurity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private App[] apps = null;
    private Button add;
    private Button del;
    private Button stopService;
    //ArrayList addlist = null;
    //ArrayList dellist = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        int num = 0, n = packages.size();
        //addlist = new ArrayList();
        //dellist = new ArrayList();
        apps = new App[n];
        for (int i = 0; i < n; i++)
            apps[i] = new App();

        LinearLayout ll = (LinearLayout) findViewById(R.id.mylinear);

        final int num_filter_app = 3;
        String[] st = new String[num_filter_app];
        st[0] = "chrome";
        st[1] = "firefox";
        st[2] = "youtube";

        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.uid <= 10000) continue;
            boolean in_filter_app_list = false;
            for (String s : st) {
                if (packageInfo.packageName.indexOf(s) >= 0) in_filter_app_list = true;
            }
            if (!in_filter_app_list) continue;
            //Log.d("myTag", "Installed package :" + packageInfo.packageName + "_" + packageInfo.uid);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            final CheckBox box = new CheckBox(this);
            box.setId(num);
            apps[num].id = packageInfo.uid;
            num++;
            box.setText(packageInfo.packageName + "_" + String.valueOf(packageInfo.uid));
            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    if (box.isChecked()) {
                        apps[id].perm = 1;
                    }
                    else  {
                        apps[id].perm = 0;
                    }
                    //Log.v("permission_set", String.valueOf(id));
                }
            });
            ll.addView(box, params);
            /*Log.d("myTag", "Source dir : " + packageInfo.sourceDir);
            Log.d("myTag", "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));*/
        }
        Log.v("myTag", String.valueOf(num) + " and " + String.valueOf(n));
        add = (Button) findViewById(R.id.button);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList addlist = new ArrayList();
                for (App app : apps)
                    if (app.perm == 1)
                        addlist.add(app.id);

                Log.v("myTag", addlist.toString());
                add(addlist);

            }
        });
        del = (Button) findViewById(R.id.button2);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList dellist = new ArrayList();
                for (App app : apps)
                    if (app.perm == 1)
                        dellist.add(app.id);

                Log.v("myTag", dellist.toString());
                del(dellist);
            }
        });
        /**
         * Start Iptable supervisor service
         * */
        Intent intent = new Intent(MainActivity.this,
                IptableService.class);
        startService(intent);
        Log.v("myTag", "start Service");
        stopService = (Button)findViewById(R.id.button3);
        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        IptableService.class);
                /** 退出Activity是，停止服务 */
                stopService(intent);
                Log.v("myTag", "shutDown service");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static String[] sendCommand(String cmd) {
        Process p;
        try {
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            BufferedReader bf = new BufferedReader(new InputStreamReader(p.getInputStream()));

            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            String output_str;
            ArrayList<String> output = new ArrayList<String>();
            while ((output_str = bf.readLine()) != null) {
                Log.v("Test: ", output_str);
                output.add(output_str);
            }
            String[] outputList = (String[]) output.toArray(new String[output.size()]);
            return outputList;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void Test() {
        String[] st = sendCommand("ls");
        for (String s : st) {
            Log.v("Test: ", s);
        }
    }

    public void add(ArrayList list) {
        String [] output;
        for (int i = 0; i < list.size(); i++) {
            int id = (int)(list.get(i));
            String st_id = "u0_a" + String.valueOf(id % 10000);
            output = sendCommand("iptables -L -n | grep DROP | grep owner | grep " + String.valueOf(id));
            Log.v("Test: ", String.valueOf(output.length));
            if (output.length == 0) {
                sendCommand("iptables -A OUTPUT -o wlan0 --match owner --uid-owner " + st_id + " -j DROP");
            }
        }
    }

    // iptables -D OUTPUT -o wlan0 --match owner --uid-owner 10197 -j DROP

    public void del(ArrayList list) {
        for (int i = 0; i < list.size(); i++) {
            int id = (int)(list.get(i)) % 10000;
            String st_id = "u0_a" + String.valueOf(id);
            sendCommand("iptables -D OUTPUT -o wlan0 --match owner --uid-owner " + st_id + " -j DROP");
        }
    }
    public class App {
        public int perm;
        public int id;
        App(int uid, int permission) {
            id = uid;
            perm = permission;
        }
        App() {
            perm = 0;
        }
    }
}
