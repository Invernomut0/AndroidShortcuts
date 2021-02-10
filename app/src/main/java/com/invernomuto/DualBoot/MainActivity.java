package com.invernomuto.DualBoot;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.yalantis.guillotine.animation.GuillotineAnimation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private static final long RIPPLE_DURATION = 250;


    private boolean dualboot = true;
    private String active_slot = "0";
    private final static String ACTION_1 = "action1";
    private final static String ACTION_2 = "action2";
    private final static String ACTION_3 = "action3";
    private final static String ACTION_4 = "action4";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Switch pwd;
    Switch NoWarn;
    TextView tLog;
    Boolean bPwd = false;
    Boolean bNoWarn = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String cmd;
        SharedPreferences pref = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        FrameLayout root = (FrameLayout) findViewById(R.id.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        View contentHamburger = (View) findViewById(R.id.content_hamburger);
        View guillotineMenu = LayoutInflater.from(this).inflate(R.layout.preferences_layout, null);
        root.addView(guillotineMenu);
        TextView tLog = (TextView) findViewById(R.id.tLog);
        //tLog.setMovementMethod(new ScrollingMovementMethod());

        new GuillotineAnimation.GuillotineBuilder(guillotineMenu, guillotineMenu.findViewById(R.id.guillotine_hamburger), contentHamburger)
                .setStartDelay(RIPPLE_DURATION)
                .setActionBarViewForAnimation(toolbar)
                .setClosedOnStart(true)
                .build();
        pwd = (Switch) findViewById(R.id.pwd);
        pwd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TextView tLog = (TextView) findViewById(R.id.tLog);
                bPwd = false;

                if(pwd.isChecked())
                {
                    bPwd = true;
                }
                else
                {
                    bPwd = false;
                }
                SharedPreferences pref = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0); // 0 - for private mode
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("ErasePwd", bPwd);
                editor.commit();

                log("Saving preferences: ErasePWD -> " + bPwd.toString());
            }
        });

        NoWarn = (Switch) findViewById(R.id.NoWarn);
        NoWarn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TextView tLog = (TextView) findViewById(R.id.tLog);
                bNoWarn = false;

                if(NoWarn.isChecked())
                {
                    bNoWarn = true;
                }
                else
                {
                    bNoWarn = false;
                }
                SharedPreferences pref = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0); // 0 - for private mode
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("NoWarn", bNoWarn);
                editor.commit();

                //log("Saving preferences: NoWarn -> " + bNoWarn.toString());
            }
        });

        Button bRA = (Button) findViewById(R.id.button);
        Button bRB = (Button) findViewById(R.id.button2);
        Button bSA = (Button) findViewById(R.id.button3);
        Button bSB = (Button) findViewById(R.id.button4);
        Button bPA = (Button) findViewById(R.id.button5);
        Button bPB = (Button) findViewById(R.id.button6);

        bRA.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ra, 0, 0, 0);
        bRB.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rb, 0, 0, 0);
        bSA.setCompoundDrawablesWithIntrinsicBounds(R.drawable.a, 0, 0, 0);
        bSB.setCompoundDrawablesWithIntrinsicBounds(R.drawable.b, 0, 0, 0);
        bPA.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rap, 0, 0, 0);
        bPB.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rbp, 0, 0, 0);

        //PASSWORD DISABLED - TODO
        disableButton(bPA,"A");
        disableButton(bPB,"B");
        pwd.setEnabled(false);
        //END


        ImageView iDB = (ImageView) findViewById(R.id.imageView);

        String os = Build.FINGERPRINT;
        ImageView iv = (ImageView) findViewById(R.id.imageView);
        log(getString(R.string.AppStarted));

        log(getString(R.string.reading_options));
        if (pref.contains("ErasePwd")) {
            Boolean ErasePwd = pref.getBoolean("ErasePwd", false);
            log(getString(R.string.P_ErasPWD) + ErasePwd.toString());
            pwd.setChecked(ErasePwd);
        }
        if (pref.contains("NoWarn")) {
            Boolean bNoWarn = pref.getBoolean("NoWarn", false);
            log(getString(R.string.P_NoWarn) + bNoWarn.toString());
            NoWarn.setChecked(bNoWarn);
        }
        String[] cmdline1 = new String[]{"su", "-c", "/data/adb/DualBoot/bootctl hal-info"};
        String res = exec_command(cmdline1);
        if (res.startsWith("HAL")) {
            log(getString(R.string.Android_signature) + os + "\n- " + res );
        } else {
            log(getString(R.string.bootctl_not_found));
            dualboot = false;
            disableButton(bRA,"A");
            disableButton(bRB,"B");
            disableButton(bSA,"A");
            disableButton(bSB,"B");

            iDB.setColorFilter(getResources().getColor(R.color.efab_disabled), PorterDuff.Mode.MULTIPLY);
            //ef.setEfabEnabled(false);
        }
        String[] cmdline = new String[]{"sh", "-c", "getprop ro.boot.slot_suffix"};
        active_slot = exec_command(cmdline);
        if (!active_slot.startsWith("\n")) {


            if (active_slot.contains("a")) {
                log(getString(R.string.Active_Slot) + "A");
                iv.setImageResource(R.drawable.logo_resized_a);
            }
            if (active_slot.contains("b")) {
                log(getString(R.string.Active_Slot) + "B");
                iv.setImageResource(R.drawable.logo_resized_b);
            }

        }
        else
            { log(getString(R.string.AB_partition_not_found));
            dualboot = false;
            disableButton(bRA,"A");
            disableButton(bRB,"B");
            disableButton(bSA,"A");
            disableButton(bSB,"B");
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
        if(!bNoWarn)
        {
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
        else
        {
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
    public void onClickErasePwdA(View view) {
        //log("Erase PWD A");
    }
    public void onClickErasePwdB(View view) {
        //log("Erase PWD B");
    }

    public void log(String s)
    {
        TextView tLog = (TextView) findViewById(R.id.tLog);
        tLog.append(s + "\n");
        tLog.setMovementMethod(new ScrollingMovementMethod());
        while (tLog.canScrollVertically(1)) {
            tLog.scrollBy(0, 10);
        }

    }

    public void disableButton(Button btn, String slot)
    {

        btn.setEnabled(false);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        if (slot.contains("A")) btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.disable_a, 0, 0, 0);
        else btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.disable_b, 0, 0, 0);
        btn.setTextColor(Color.DKGRAY);
    }
}
