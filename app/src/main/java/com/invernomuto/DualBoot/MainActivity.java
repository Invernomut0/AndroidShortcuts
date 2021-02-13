package com.invernomuto.DualBoot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.topjohnwu.superuser.Shell;
import com.yalantis.guillotine.animation.GuillotineAnimation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        ImageView iv = (ImageView) findViewById(R.id.imageView);
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

                //log("Saving preferences: ErasePWD -> " + bPwd.toString());
            }
        });
        log(getString(R.string.AppStarted));
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
        bPA.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_lock_open_47, 0, 0, 0);
        bPB.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_round_android_47, 0, 0, 0);

        //PASSWORD DISABLED -
        //disableButton(bPA,"A");
        //disableButton(bPB,"B");
        //pwd.setEnabled(false);

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
            if(active_slot.contains("a"))
            {
                disableButton(bRB,"B");
                disableButton(bSB,"B");
            }
            else
            {
                disableButton(bRA,"A");
                disableButton(bSA,"A");
            }


        }

        //Swap button if active slot is A
        if (active_slot.contains("a")) {
            ViewGroup layout = (ViewGroup) bPA.getParent();
            if (null != layout) //for safety only  as you are doing onClick
                layout.removeView(bPA);

            layout.addView((View) bPA);
        }
        //END
        //LinearLayout ln = (LinearLayout) findViewById(R.id.main);
        //int index = ln.indexOfChild((View) bPB);
        //ln.removeViewAt(index);

        ImageView iDB = (ImageView) findViewById(R.id.imageView);

        String os = Build.FINGERPRINT;

        log(getString(R.string.reading_options));
        if (pref.contains("ErasePwd")) {
            Boolean ErasePwd = pref.getBoolean("ErasePwd", false);
            //log(getString(R.string.P_ErasPWD) + ErasePwd.toString());
            pwd.setChecked(ErasePwd);
        }
        if (pref.contains("NoWarn")) {
            Boolean bNoWarn = pref.getBoolean("NoWarn", false);
            //log(getString(R.string.P_NoWarn) + bNoWarn.toString());
            NoWarn.setChecked(bNoWarn);
        }
        String[] cmdline1 = new String[]{"su", "-c", "/data/adb/DualBoot/bootctl hal-info"};
        String res = exec_command(cmdline1);
        if (res.startsWith("HAL")) {
            log(getString(R.string.Android_signature) + os + "\n- " + res );
        } else {
            log(getString(R.string.bootctl_not_found));
            dualboot = false;
            if(active_slot.contains("a"))
            {
                disableButton(bRB,"B");
                disableButton(bSB,"B");
            }
            else
            {
                disableButton(bRA,"A");
                disableButton(bSA,"A");
            }

            iDB.setColorFilter(getResources().getColor(R.color.efab_disabled), PorterDuff.Mode.MULTIPLY);

        }

                /* ANDROID INFO */
        TextView tSlotAtitle = (TextView) findViewById(R.id.slotatitle);
        TextView tSlotBtitle = (TextView) findViewById(R.id.slotbtitle);
        TextView tSlotA = (TextView) findViewById(R.id.slota);
        TextView tSlotB = (TextView) findViewById(R.id.slotb);
        String sAndroidInfoA = getString(R.string.slota);
        String sAndroidInfoB = getString(R.string.slotb);
        List<String> out = new ArrayList<String>();
        String partition="";
        Shell.Result result;
        result = Shell.su("mkdir /data/adb/DualBoot/system_").exec();
        result = Shell.su("mkdir /data/adb/DualBoot/data_").exec();
        //log("\nRoot permission: " + Shell.getShell().isRoot());
        if (active_slot.contains("a")) {
            //Mount system_b
            //out = Shell.su("ls -la /dev/block/by-name/ | grep system_b").exec().getOut();
            //partition = matcher_partitions(out.toString());
            //log("Mount system_b: /dev/block/" + partition);
            result = Shell.su("blkid /dev/block/by-name/system_b").exec();
            //log("System_b info: " + result.getOut().toString() + "\nError Code: " + result.getErr().toString());
            result = Shell.su("umount /dev/block/by-name/system_b").exec();
            result = Shell.su("mount -t ext4 /dev/block/by-name/system_b /data/adb/DualBoot/system_").exec();

            if (!result.isSuccess())
            {
                log("Mount System_b failed: " + result.getOut().toString() + "\nError Code: " + result.getErr().toString());
            }
            tSlotAtitle.setText("Slot A");
            tSlotBtitle.setText("Slot B");

        }
        else if (active_slot.contains("b")) {
            //Mount system_b
            result = Shell.su("blkid /dev/block/by-name/system_a").exec();
           // log("System_a info: " + result.getOut().toString() + "\nError Code: " + result.getErr().toString());
            result = Shell.su("umount /dev/block/by-name/system_a").exec();

            result = Shell.su("mount -t ext4 /dev/block/by-name/system_a /data/adb/DualBoot/system_").exec();

            if (!result.isSuccess())
            {
                log("Mount System_a failed: " + result.getOut().toString() + "\nError Code: " + result.getErr().toString());
            }
            tSlotBtitle.setText("Slot A");
            tSlotAtitle.setText("Slot B");

        }
        tSlotA.setText("");
        tSlotB.setText("");

        out = Shell.su("cat /system/build.prop").exec().getOut();
        for (int i = 0; i < out.size(); i++)
        {
            if (out.get(i).contains("ro.build.version.security_patch=")) tSlotA.append("\n" + getString(R.string.security_patch) + out.get(i).split("(?<==)")[1]);
            else if (out.get(i).contains("ro.system.build.version.release=")) tSlotA.append("\n" + getString(R.string.Release_version) + out.get(i).split("(?<==)")[1]);
            else if (out.get(i).contains("ro.build.tags=release-keys=")) tSlotA.append("\n" + getString(R.string.Release_key) + out.get(i).split("(?<==)")[1]);
            else if (out.get(i).contains("ro.system.build.date=")) tSlotA.append("\n"+getString(R.string.build) + out.get(i).split("(?<==)")[1]);
            else if (out.get(i).contains("ro.system.build.id=")) tSlotA.append("\n" + getString(R.string.build_id) + out.get(i).split("(?<==)")[1]);
            else if (out.get(i).contains("ro.build.flavor=")) tSlotA.append("\n" + getString(R.string.Build_flavor) + (out.get(i).split("(?<==)")[1]));
        }
        out = Shell.su("cat /data/adb/DualBoot/system_/system/build.prop").exec().getOut();
        for (int i = 0; i < out.size(); i++)
        {
            if (out.get(i).contains("ro.build.version.security_patch=")) tSlotB.append("\n" + getString(R.string.security_patch) + out.get(i).split("(?<==)")[1]);
            else if (out.get(i).contains("ro.system.build.version.release=")) tSlotB.append("\n" + getString(R.string.Release_version) + out.get(i).split("(?<==)")[1]);
            else if (out.get(i).contains("ro.build.tags=release-keys=")) tSlotB.append("\n" + getString(R.string.Release_key) + out.get(i).split("(?<==)")[1]);
            else if (out.get(i).contains("ro.system.build.date=")) tSlotB.append("\n"+getString(R.string.build) + out.get(i).split("(?<==)")[1]);
            else if (out.get(i).contains("ro.system.build.id=")) tSlotB.append("\n" + getString(R.string.build_id) + out.get(i).split("(?<==)")[1]);
            else if (out.get(i).contains("ro.build.flavor=")) tSlotB.append("\n" + getString(R.string.Build_flavor) + out.get(i).split("(?<==)")[1]);
        }

        //log("OK");

        /*END ANDROID INFO */


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

        Boolean deletepwd = false;
        if(bPwd && !currentSlot.contains(active_slot))
        {
            log("1");
            //onClickErasePwdA((View) new View.TEXT_ALIGNMENT_CENTER);
        }
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
        Shell.Result result;
        log(getString(R.string.Reading_partition));
        // Execute commands synchronously
        Shell sh = Shell.getShell();
        Boolean root = sh.isRoot();
        List<String> out = new ArrayList<String>();
        String partition = "";
        Shell.Result res;
        String formato = "";
        if (active_slot.contains("a"))
        {
            //Mount userdata_a
            res = Shell.su("blkid /dev/block/by-name/userdata_b").exec();
            if(res.getOut().toString().contains("ext4"))
            {
                formato="ext4";
            }
            else if(res.getOut().toString().contains("f2fs"))
            {
                formato="f2fs";
            }
            else
            {
                log(getString(R.string.error_userdata_b));
                return;
            }
            out = Shell.su("umount /dev/block/by-name/userdata_b" + partition).exec().getOut();
            log("Mount userdata_b: /dev/block/by-name/userdata_b");
            out =  Shell.su("mount -t " + formato + " /dev/block/by-name/userdata_b /data/adb/DualBoot/data_").exec().getOut();
        }
        else if(active_slot.contains("b"))
        {
            //Mount userdata_b
            res = Shell.su("blkid /dev/block/by-name/userdata_a").exec();
            if(res.getOut().toString().contains("ext4"))
            {
                formato="ext4";
            }
            else if(res.getOut().toString().contains("f2fs"))
            {
                formato="f2fs";
            }
            else
            {
                log(getString(R.string.error_userdata_a));
                return;
            }
            out = Shell.su("umount /dev/block//by-name/userdata_a").exec().getOut();
            log("Mount userdata_a: /dev/block/by-name/userdata_a");
            out =  Shell.su("mount -t " + formato + " /dev/block//by-name/userdata_a /data/adb/DualBoot/data_").exec().getOut();
            //log("mount res: " + out.toString());
        }
        else return;

        res = Shell.su("test -f /data/adb/DualBoot/data_/system/locksettings.db").exec();
        if (res.isSuccess())
        {
            res = Shell.su("rm -f /data/adb/DualBoot/data_/system/locksettings.db").exec();
            log(getText(R.string.remove_locksettings).toString());
        }
        else
        {
            log(getText(R.string.locksettings_not_found).toString());
        }

        /*
        out = Shell.su("ls -la /data/adb/DualBoot/data_/system/lock_settings.db").exec().getOut();
        //log("lock: " + out.toString());
        if(out.toString().contains("locksettings.db"))
        {
            log(getString(R.string.remove_locksettings));
            out = Shell.su("rm -f /data/adb/DualBoot/data_/system/locksettings.db").exec().getOut();
            //log("res: " + out);
        }
        else {
            log(getString(R.string.locksettings_not_found));
        }
        */
    }
    public void onClickBootloader(View view)
    {
        if(!bNoWarn)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(android.R.string.dialog_alert_title);
            builder.setMessage(getString(R.string.dialog_confirmation));
            builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        Runtime.getRuntime().exec("su -c reboot bootloader");
                    }
                    catch (IOException e) {
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
                Runtime.getRuntime().exec("su -c reboot bootloader");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String matcher_partitions(String s)
    {
        Pattern p = Pattern.compile(".*/([a-z]..[0-9].).*");
        Matcher m = p.matcher(s);

        String sa;
        if(m.matches())
            return m.group(1);
        else
            return "NOT_FOUND";
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
class SplashActivity extends MainActivity {

    static {
        // Set settings before the main shell can be created
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Preheat the main root shell in the splash screen
        // so the app can use it afterwards without interrupting
        // application flow (e.g. root permission prompt)
        Shell.getShell(shell -> {
            // The main shell is now constructed and cached
            // Exit splash screen and enter main activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}