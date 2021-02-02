package com.invernomuto.DualBoot;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nambimobile.widgets.efab.ExpandableFab;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private boolean dualboot = true;
    private String active_slot = "0";
    private final static String ACTION_1 = "action1";
    private final static String ACTION_2 = "action2";
    private final static String ACTION_3 = "action3";
    private final static String ACTION_4 = "action4";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String cmd;
        ExpandableFab ef = findViewById(R.id.expandable_fab);
        ef.setSize(FloatingActionButton.SIZE_NORMAL);
        TextView textView = (TextView) findViewById(R.id.textview);
        textView.setText(textView.getText() + "Checking Partitions\n");
        String[] cmdline = new String[] {"sh", "-c", "getprop ro.boot.slot_suffix"};
        active_slot = exec_command(cmdline);
        if (!active_slot.startsWith("\n")) {
            textView.setText(textView.getText() + getString(R.string.Active_Slot) + active_slot);
        }
        else
        {
            textView.setText(textView.getText() + "Device not compatible.\nA/B partition not found.\n\nSorry, nothing to do here...\n");
            dualboot=false;
            ef.setEfabEnabled(false);
        }
        switch (getIntent().getAction()){
            case ACTION_1:
                switchSlot(this, "_a",0);
                break;
            case ACTION_2:
                switchSlot(this, "_b",0);
                break;
            case ACTION_3:
                switchSlot(this, "_b",1);
                break;
            case ACTION_4:
                switchSlot(this, "_b",1);
                break;
            default:
                break;
        }
    }

    public void switchSlot(MainActivity view, final String currentSlot, final int recovery) {
        //Toast.makeText(this, getString(R.string.error_ab_device), Toast.LENGTH_LONG).show(); //This is not an A/B device!
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(android.R.string.dialog_alert_title);
        builder.setMessage(getString(R.string.dialog_confirmation));
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    TextView textView = (TextView) findViewById(R.id.textview);
                    if (currentSlot.contains("_b")) {
                        Runtime.getRuntime().exec("su -c /data/adb/DualBoot/bootctl set-active-boot-slot 0");
                    } if (currentSlot.contains("_a")) {
                        Runtime.getRuntime().exec("su -c /data/adb/DualBoot/bootctl set-active-boot-slot 1");
                    }
                    if (recovery == 0 )
                    {Runtime.getRuntime().exec("su -c reboot");}
                    else
                    {Runtime.getRuntime().exec("su -c reboot recovery");}

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton(getString(android.R.string.no), null);
        AlertDialog dialog = builder.create();
        dialog.show();
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

    public void onClickRebootA(View view) {
         switchSlot(this, "_b", 0);
    }

    public void onClickRebootB(View view) {
         switchSlot(this, "_a", 0);
    }
    public void onClickRRebootA(View view) {
        switchSlot(this, "_b", 1);
    }

    public void onClickRRebootB(View view) {
        switchSlot(this, "_a", 1);
    }
}
