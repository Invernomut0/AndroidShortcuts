package com.invernomuto.DualBoot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hanks.htextview.base.AnimationListener;
import com.hanks.htextview.base.HTextView;
import com.nambimobile.widgets.efab.ExpandableFab;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.hanks.htextview.base.HTextView;

public class MainActivity extends AppCompatActivity {

    private boolean dualboot = true;
    private String active_slot = "0";
    private final static String ACTION_1 = "action1";
    private final static String ACTION_2 = "action2";
    private final static String ACTION_3 = "action3";
    private final static String ACTION_4 = "action4";

    //@SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String cmd;

        TextView tBoot = (TextView) findViewById(R.id.tBootctl);
        TextView tDev = (TextView) findViewById(R.id.tDevice);
        Button bRA = (Button) findViewById(R.id.button);
        Button bRB = (Button) findViewById(R.id.button2);
        Button bSA = (Button) findViewById(R.id.button3);
        Button bSB = (Button) findViewById(R.id.button4);
        Button bPA = (Button) findViewById(R.id.button5);
        Button bPB = (Button) findViewById(R.id.button6);
        ImageView iDB = (ImageView) findViewById(R.id.imageView);

        String os = Build.FINGERPRINT;
        ImageView iv = (ImageView) findViewById(R.id.imageView);
        String[] cmdline1 = new String[]{"su", "-c", "/data/adb/DualBoot/bootctl hal-info"};
        String res = exec_command(cmdline1);
        if (res.startsWith("HAL")) {
            tBoot.setText(res);
        } else {
            tBoot.setText(R.string.bootctl_not_found);
            dualboot = false;

            bRA.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            bRB.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            bSA.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            bSB.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

            bRA.setEnabled(false);
            bRB.setEnabled(false);
            bSA.setEnabled(false);
            bSB.setEnabled(false);
            iDB.setColorFilter(getResources().getColor(R.color.efab_disabled), PorterDuff.Mode.MULTIPLY);
            //ef.setEfabEnabled(false);
        }
        String[] cmdline = new String[]{"sh", "-c", "getprop ro.boot.slot_suffix"};
        active_slot = exec_command(cmdline);
        if (!active_slot.startsWith("\n")) {
            tDev.setText(getString(R.string.Active_Slot) + active_slot);

            if (active_slot.contains("a")) {
                iv.setImageResource(R.drawable.logo_resized_a);
            }
            if (active_slot.contains("b")) {
                iv.setImageResource(R.drawable.logo_resized_b);
            }
        } else {
            tDev.setText(R.string.AB_partition_not_found);
            dualboot = false;

            bRA.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            bRB.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            bSA.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            bSB.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

            bRA.setEnabled(false);
            bRB.setEnabled(false);
            bSA.setEnabled(false);
            bSB.setEnabled(false);
            //ef.setEfabEnabled(false);
        }
        //textView.animateText(tLog);
        switch (getIntent().getAction()) {
            case ACTION_1:
                switchSlot(this, "_a", 0);
                break;
            case ACTION_2:
                switchSlot(this, "_b", 0);
                break;
            case ACTION_3:
                switchSlot(this, "_b", 1);
                break;
            case ACTION_4:
                switchSlot(this, "_b", 1);
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
                    //TextView textView = (TextView) findViewById(R.id.textview);
                    if (currentSlot.contains("_b")) {
                        Runtime.getRuntime().exec("su -c /data/adb/DualBoot/bootctl set-active-boot-slot 0");
                    }
                    if (currentSlot.contains("_a")) {
                        Runtime.getRuntime().exec("su -c /data/adb/DualBoot/bootctl set-active-boot-slot 1");
                    }
                    if (recovery == 0) {
                        Runtime.getRuntime().exec("su -c reboot");
                    } else {
                        Runtime.getRuntime().exec("su -c reboot recovery");
                    }

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

    //
    public void onClickInfo(View view) {
        // definisco l'intenzione di aprire l'Activity "Page1.java"
        Intent InfoPage = new Intent(MainActivity.this,Info.class);
        // passo all'attivazione dell'activity page1.java
        startActivity(InfoPage);
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
