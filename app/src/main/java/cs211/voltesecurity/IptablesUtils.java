package cs211.voltesecurity;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class IptablesUtils {
    private String _interface_name;
    private String _iptables;

    public IptablesUtils() {
        _iptables = "iptables";
        _interface_name = "wlan0";
        SetDefault();
    }

    public IptablesUtils(String iptables, String interface_name) {
        _iptables = iptables;
        _interface_name = interface_name;
        SetDefault();
    }

    public static String[] sendCommand(String cmd) {
        Log.v("Cmd: ", cmd);
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
                Log.v("CommandOutput: ", output_str);
                output.add(output_str);
            }
            String[] outputList = (String[]) output.toArray(new String[output.size()]);
            return outputList;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] ParseIptables() {
        String[] st = sendCommand(_iptables + " -S | grep \"A OUTPUT\"");
        return st;
    }

    public String[] Filter(String[] st) {
        ArrayList<String> output = new ArrayList<String>();
        for (String x : st) {
            if (x.indexOf("ACCEPT") >= 0 || x.indexOf("DROP") >= 0) {
                output.add(x);
            }
        }
        return (String[]) output.toArray(new String[output.size()]);
    }

    public void Clear(String[] st) {
        for (String x : st) {
            if (x.indexOf("-A") == 0) {
                String delete_cmd = _iptables + " -D" + x.substring(2);
                sendCommand(delete_cmd);
            }
        }
    }

    public void ClearAll() {
        Log.v("Apply: ", "ClearAll");
        Clear(Filter(ParseIptables()));
    }

    public void SetDefault() {
        Clear(Filter(ParseIptables()));
        sendCommand(_iptables + " -I OUTPUT -o " + _interface_name + " -j DROP");
        sendCommand(_iptables + " -I OUTPUT -o " + _interface_name + " -m owner --gid-owner root -j ACCEPT");
        sendCommand(_iptables + " -I OUTPUT -o " + _interface_name + " -m owner --gid-owner system -j ACCEPT");
    }

    private void AddEntry(int uid) {
        sendCommand(_iptables + " -I OUTPUT -o " + _interface_name +
                " --match owner --uid-owner " +
                String.valueOf(uid) + " -j ACCEPT");
    }

    private int FindCmdinList(String cmd, String[] st_list) {
        int get_it = -1;
        for (int i = 0; i < st_list.length; i++) {
            if (st_list[i].indexOf(cmd) >= 0) {
                get_it = i;
                break;
            }
        }
        return get_it;
    }

    private boolean Consistent(Integer[] uid_list, String[] st_list) {
        // 3 more default entries
        if (uid_list.length + 3 > st_list.length) return false;

        ArrayList mark = new ArrayList ();
        for (int i = 0; i < st_list.length; i++) mark.add(false);

        String cmd;
        int id;
        for (int uid : uid_list) {
            cmd = "-A OUTPUT -o " + _interface_name + " -m owner --uid-owner " +
                  String.valueOf(uid) + " -j ACCEPT";
            id = FindCmdinList(cmd, st_list);
            if (id < 0) return false;
            else mark.set(id, true);
        }
        id = FindCmdinList("-A OUTPUT -o " + _interface_name + " -j DROP", st_list);
        if (id != uid_list.length + 2) return false;
        else mark.set(id, true);
        id = FindCmdinList("-A OUTPUT -o " + _interface_name + " -m owner --gid-owner 0 -j ACCEPT",
                           st_list);
        if (id < 0) return false;
        else mark.set(id, true);
        id = FindCmdinList("-A OUTPUT -o " + _interface_name + " -m owner --gid-owner 1000 -j ACCEPT",
                           st_list);
        if (id < 0) return false;
        else mark.set(id, true);

        for (int i = 0; i < uid_list.length + 3; i++) {
            if (!(boolean)mark.get(i)) return false;
        }
        return true;
    }

    public void Apply(Integer[] uid_list) {
        String[] st_list = Filter(ParseIptables());
        for (int i = 0; i < uid_list.length; i++) {
            Log.v("Apply: ", String.valueOf(uid_list[i]));
        }
        for (int i = 0; i < st_list.length; i++) {
            Log.v("Apply: ", st_list[i]);
        }
        boolean consist = Consistent(uid_list, st_list);
        Log.v("Apply: ", String.valueOf(consist));

        if (!consist) {
            Clear(st_list);
            SetDefault();
            for (int uid : uid_list) {
                AddEntry(uid);
            }
        }
    }
};
