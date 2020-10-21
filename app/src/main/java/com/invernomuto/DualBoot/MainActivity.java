package com.invernomuto.DualBoot;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hanks.htextview.base.AnimationListener;
import com.hanks.htextview.base.HTextView;
import com.hanks.htextview.line.LineTextView;
import com.invernomuto.DualBoot.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

class ExecAsRoot extends ExecuteAsRootBase {
    ArrayList<String> cmds = new ArrayList<String>();
    String _runcmd;
    ExecAsRoot(String runcmd){
        _runcmd=runcmd;
    }
    @Override
    protected ArrayList<String> getCommandsToExecute() {
        cmds.add(_runcmd);
        return cmds;
    }
}

public class MainActivity extends AppCompatActivity {

    private boolean dualboot = true;
    private String active_slot = "0";
    private final static String ACTION_1 = "action1";
    private final static String ACTION_2 = "action2";
    private final static String ACTION_3 = "action3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String cmd;
        TextView textView = (TextView) findViewById(R.id.textview);
        textView.setText(textView.getText() + "Checking Partitions\n");
        //getprop ro.boot.slot_suffix

        String[] cmdline = new String[] {"sh", "-c", "getprop ro.boot.slot_suffix"};
        active_slot = exec_command(cmdline);

        if (!active_slot.startsWith("\n")) {
            textView.setText(textView.getText() + getString(R.string.Active_Slot) + active_slot);
        }
        else
        {
            textView.setText(textView.getText() + "Device not compatible.\nA/B partition not found.");
            dualboot=false;
        }
        /*reboot = new Reboot();
        reboot.getCommandsToExecute();
        reboot.execute();
        */
        ExecAsRoot ear;
        switch (getIntent().getAction()){
            case ACTION_1:
                textView.setText(textView.getText() + "\nReboot to Slot B!");
                cmd="su -c bootctl set-active-boot-slot 1";
                ear = new ExecAsRoot(cmd);
                ear.execute();
                cmd="su -c am start -a android.intent.action.REBOOT";
                ear = new ExecAsRoot(cmd);
                ear.execute();
                break;
            case ACTION_2:
                textView.setText(textView.getText() + "\nReboot to Slot A!");
                cmd="su -c bootctl set-active-boot-slot 0";
                ear = new ExecAsRoot(cmd);
                ear.execute();
                cmd="su -c am start -a android.intent.action.REBOOT";
                ear = new ExecAsRoot(cmd);
                ear.execute();
               // cmdline = new String[] {"sh", "-c", "am start -a android.intent.action.REBOOT"};
                break;
            case ACTION_3:
                textView.setText(ACTION_3);
                break;
            default:
                break;
        }
    }
    public void slotb_click(View view) {
        TextView textView = (TextView) findViewById(R.id.textview);
        textView.setText(textView.getText() + "\nReboot to Slot B!");
        String cmd="su -c bootctl set-active-boot-slot 1";
        ExecAsRoot ear = new ExecAsRoot(cmd);
        ear.execute();
        cmd="su -c am start -a android.intent.action.REBOOT";
        ear = new ExecAsRoot(cmd);
        new AlertDialog.Builder(this)
                .setTitle("Title")
                .setMessage("Do you really want to whatever?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(MainActivity.this, "Yaay", Toast.LENGTH_SHORT).show();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
        //ear.execute();
    }

    public void slota_click(View view) {
        TextView textView = (TextView) findViewById(R.id.textview);
        textView.setText(textView.getText() + "\nReboot to Slot A!");
        String cmd="su -c bootctl set-active-boot-slot 0";
        ExecAsRoot ear = new ExecAsRoot(cmd);
        ear.execute();
        cmd="su -c am start -a android.intent.action.REBOOT";
        ear = new ExecAsRoot(cmd);
        new AlertDialog.Builder(this)
                .setTitle("Title")
                .setMessage("Do you really want to whatever?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(MainActivity.this, "Yaay", Toast.LENGTH_SHORT).show();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
        //ear.execute();
    }
    public String exec_command(String[] cmd) {

        try {
            // Executes the command.
            Process process = Runtime.getRuntime().exec(cmd);
            // Reads stdout.
            // NOTE: You can write to stdin of the command using
            //       process.getOutputStream().
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            // Waits for the command to finish.
            process.waitFor();

            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
