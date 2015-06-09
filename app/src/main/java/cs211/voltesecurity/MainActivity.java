package cs211.voltesecurity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private ArrayList<App> apps = null;
    //private Button add;
    //private Button del;
    private Button stopService;
    private File config;
    //ArrayList addlist = null;
    //ArrayList dellist = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Test();

        final PackageManager pm = getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        //addlist = new ArrayList();
        //dellist = new ArrayList();
        apps = new ArrayList<App>();
        LinearLayout ll = (LinearLayout) findViewById(R.id.mylinear);

        final int num_filter_app = 7;
        int num = 0;
        String[] st = new String[num_filter_app];
        st[0] = "chrome";
        st[1] = "firefox";
        st[2] = "youtube";
        st[3] = "volteattacker";
        st[4] = "spotify";
        st[5] = "wikipedia";
        st[6] = "youdao";
        for (ApplicationInfo packageInfo : packages) {
            //if (packageInfo.uid <= 10000) continue;
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
            apps.add(new App(packageInfo.uid, 0));
            num++;
            box.setText(packageInfo.packageName + "_" + String.valueOf(packageInfo.uid));
            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    //Log.v("myTag-id", String.valueOf(id));
                    if (box.isChecked()) {
                        apps.get(id).perm = 1;
                    }
                    else  {
                        apps.get(id).perm = 0;
                    }
                    updateConfigFile();
                    //Log.v("permission_set", String.valueOf(id));
                }
            });
            ll.addView(box, params);
            /*Log.d("myTag", "Source dir : " + packageInfo.sourceDir);
            Log.d("myTag", "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));*/
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        final CheckBox box  = new CheckBox(this);
        box.setId(num);
        apps.add(new App(0, 0));
        box.setText("root_0");
        box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                //Log.v("myTag-id", String.valueOf(id));
                if (box.isChecked()) {
                    apps.get(id).perm = 1;
                }
                else  {
                    apps.get(id).perm = 0;
                }
                updateConfigFile();
            }
        });
        ll.addView(box, params);
         /*add = (Button) findViewById(R.id.button);
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
        });*/
        /**
         * Start Iptable supervisor service
         * */

        stopService = (Button)findViewById(R.id.button3);
        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        IptableService.class);
                if (stopService.getText().equals("Stop")) {
                    stopService(intent);
                    v.setBackgroundColor(Color.GRAY);
                    stopService.setText("Start");
                    Log.v("myTag", "shutDown service");
                }
                else {
                    startService(intent);
                    Log.v("myTag", "Start Service");
                    stopService.setText("Stop");
                }
            }
        });
        /**
         * Configuration file setup
         * */
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        path.mkdir();
        Log.v("myTag", path.toString());
        config = new File(path,"config.txt");
        if (config.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(config));
                String line;
                while ((line = reader.readLine()) != null) {
                    for (int i = 0; i < apps.size(); i++)
                        if (apps.get(i).id ==Integer.valueOf(line)) {
                            CheckBox check = (CheckBox)findViewById(i);
                            check.setChecked(true);
                            apps.get(i).perm = 1;
                        }
                }
                updateConfigFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        String FILENAME = "hellotest.sh";
        String string = "while true; do\n" +
                        "   output=$(ps | grep cs211.voltesecurity)\n" +
                        "   if [ ${#output} -lt 1 ]; then\n" +
                        "       am start -n cs211.voltesecurity/cs211.voltesecurity.MainActivity\n" +
                        "   fi\n" +
                        "   sleep 10\n" +
                        "done\n";
        try {
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendCommand("chmod 755 /data/data/cs211.voltesecurity/files/hellotest.sh");
        sendCommand("/data/data/cs211.voltesecurity/files/hellotest.sh &");
    }
    public void updateConfigFile() {
        if (config.exists())
            config.delete();
        try {
            PrintWriter writer = new PrintWriter(config);
            for (App app : apps)
                if (app.perm == 1) {
                    writer.println(app.id);
                }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /*public void add(ArrayList list) {
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
    }*/

    // iptables -D OUTPUT -o wlan0 --match owner --uid-owner 10197 -j DROP

    /*public void del(ArrayList list) {
        for (int i = 0; i < list.size(); i++) {
            int id = (int)(list.get(i)) % 10000;
            String st_id = "u0_a" + String.valueOf(id);
            sendCommand("iptables -D OUTPUT -o wlan0 --match owner --uid-owner " + st_id + " -j DROP");
        }
    }*/
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
