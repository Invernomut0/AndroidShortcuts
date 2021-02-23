package com.invernomuto.DualBoot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
import android.view.Window;
import android.view.WindowManager;
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
import androidx.core.content.ContextCompat;


import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFileInputStream;
import com.topjohnwu.superuser.io.SuFileOutputStream;
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
    private String active_slot = "";
    private String inactive_slot= "";
    private final static String ACTION_1 = "action1";
    private final static String ACTION_2 = "action2";
    private final static String ACTION_3 = "action3";
    private final static String ACTION_4 = "action4";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Switch pwd;
    Switch NoWarn;
    Switch mSys;
    Switch mData;
    TextView tLog;
    Boolean bPwd = false;
    Boolean ErasePwd = false;
    Boolean bNoWarn = false;
    Boolean bSys = false;
    Boolean bData = false;
    String commondatamount11 = "/mnt/pass_through/0/emulated/0/DualBoot/";
    String commondatamount10 = "/mnt/pass_through/0/emulated/0/DualBoot/";
    String initrc = "/system/etc/init/hw/init.rc";
    String sdcard = "/sdcard/DualBoot/";
    String mSystemPartition = "System_";
    String mDatapPartition = "UserData_";
    String mSdcard = "SDcard_";
    //String mInactive = "mInactive.conf";

    static {
        // Set settings before the main shell can be created
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setFlags(Shell.ROOT_MOUNT_MASTER)
                .setTimeout(10)
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        String versionName = BuildConfig.VERSION_NAME;
        String cmd;
        List<String> out = new ArrayList<String>();

        FrameLayout root = findViewById(R.id.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        View contentHamburger = findViewById(R.id.content_hamburger);
        View guillotineMenu = LayoutInflater.from(this).inflate(R.layout.preferences_layout, null);
        ImageView iv = findViewById(R.id.iv);
        TextView tLog = findViewById(R.id.tLog);
        Button bRA = findViewById(R.id.button);
        Button bRB = findViewById(R.id.button2);
        Button bSA = findViewById(R.id.button3);
        Button bSB = findViewById(R.id.button4);
        Button bPA = findViewById(R.id.button5);
        Button bPB = findViewById(R.id.button6);
        Button bShared = findViewById(R.id.bShareApp);

        pref = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0); // 0 - for private mode
        editor = pref.edit();
        root.addView(guillotineMenu);
        TextView tVersion = findViewById(R.id.tVersione);
        tVersion.setText("Version: " + versionName);
        new GuillotineAnimation.GuillotineBuilder(guillotineMenu, guillotineMenu.findViewById(R.id.guillotine_hamburger), contentHamburger)
                .setStartDelay(RIPPLE_DURATION)
                .setActionBarViewForAnimation(toolbar)
                .setClosedOnStart(true)
                .build();

        out = Shell.su("getprop ro.boot.slot_suffix").exec().getOut();
        active_slot = out.get(0);
        if (!active_slot.startsWith("\n")) {
            if (active_slot.contains("a")) {
                inactive_slot ="b";
                log(getString(R.string.Active_Slot) + "A");
                iv.setImageResource(R.drawable.logo_resized_a);
            }
            if (active_slot.contains("b")) {
                inactive_slot ="a";
                log(getString(R.string.Active_Slot) + "B");
                iv.setImageResource(R.drawable.logo_resized_b);
            }

        }
        else
        {
            log(getString(R.string.AB_partition_not_found));
            dualboot = false;
            if(active_slot.contains("a"))
            {
                inactive_slot ="b";
                disableButton(bRB,"B");
                disableButton(bSB,"B");
            }
            else
            {
                inactive_slot ="a";
                disableButton(bRA,"A");
                disableButton(bSA,"A");
            }
            disableButton(bPA,"A");
            //disableButton(bPB,"B");
            pwd.setEnabled(false);

        }
        log(getString(R.string.AppStarted));

        //SELINUX CHECK
        Shell.Result result;
        result = Shell.su("getenforce").exec();
        log("Selinux: " + result.getOut().toString());

        pwd = findViewById(R.id.pwd);
        NoWarn = findViewById(R.id.NoWarn);
        mSys = findViewById(R.id.mSys);
        mData = findViewById(R.id.mData);
        log(getString(R.string.reading_options));

        pwd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TextView tLog = findViewById(R.id.tLog);
                Boolean bbPwd = false;
                bbPwd = pwd.isChecked();
                SharedPreferences pref = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0); // 0 - for private mode
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("ErasePwd", bbPwd);
                editor.commit();
                reloadPref();
                //log("Saving preferences: ErasePWD -> " + bPwd.toString());
            }
        });
        NoWarn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TextView tLog = findViewById(R.id.tLog);
                Boolean bbNoWarn = false;
                bbNoWarn = NoWarn.isChecked();
                SharedPreferences pref = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0); // 0 - for private mode
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("NoWarn", bbNoWarn);
                editor.commit();
                reloadPref();
                //log("Saving preferences: NoWarn -> " + bNoWarn.toString());
            }
        });
        mSys.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TextView tLog = findViewById(R.id.tLog);
                Boolean bbSys = false;
                bbSys = mSys.isChecked();
                SharedPreferences pref = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0); // 0 - for private mode
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("mSys", bbSys);
                editor.commit();
                reloadPref();
                mountSetup();
                //log("Saving preferences: NoWarn -> " + bNoWarn.toString());
            }
        });
        mData.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TextView tLog = findViewById(R.id.tLog);
                Boolean bbData = false;
                bbData = mData.isChecked();
                SharedPreferences pref = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0); // 0 - for private mode
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("mData", bbData);
                editor.commit();
                reloadPref();
                mountSetup();
                //log("Saving preferences: NoWarn -> " + bNoWarn.toString());
            }
        });

        reloadPref();

        bRA.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ra, 0, 0, 0);
        bRB.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rb, 0, 0, 0);
        bSA.setCompoundDrawablesWithIntrinsicBounds(R.drawable.a, 0, 0, 0);
        bSB.setCompoundDrawablesWithIntrinsicBounds(R.drawable.b, 0, 0, 0);
        bPA.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_lock_open_47, 0, 0, 0);
        bPB.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_round_android_47, 0, 0, 0);
        bShared.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.star_big_on, 0, 0, 0);

        //Swap button if active slot is A
        if (active_slot.contains("a")) {
            ViewGroup layout = (ViewGroup) bPA.getParent();
            if (null != layout) //for safety only  as you are doing onClick
                layout.removeView(bPA);

            layout.addView(bPA);
        }
        //END
        //LinearLayout ln = (LinearLayout) findViewById(R.id.main);
        //int index = ln.indexOfChild((View) bPB);
        //ln.removeViewAt(index);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.guillotine_background_dark));
        }
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.guillotine_background_dark));

       //ImageView iDB = findViewById(R.id.imageView);

        String os = Build.FINGERPRINT;

        //CHECK LAYOUT
        result = Shell.su("test -d /datacommon && echo OK || echo KO").exec();
        if(result.getOut().toString().contains("KO"))
        {
            bShared.setText(getString(R.string.no_shared_app));
            disableButton(bShared,"");
        }
        //BOOTCTL ---------------------------
        try {
            InputStream is = getAssets().open("bootctl");
            log(getString(R.string.installing_bootctl));
            // We guarantee that the available method returns the total
            // size of the asset...  of course, this does mean that a single
            // asset can't be more than 2 gigs.
            int size = is.available();
            if (size > 0) {
                try (InputStream in = is;
                     OutputStream outp = SuFileOutputStream.open("/data/adb/Dualboot/bootctl")) {
                        copyFile(is,outp);
                        Shell.su("chmod +x /data/adb/Dualboot/bootctl").submit();
                        Shell.su("chcon u:object_r:system_file:s0 /data/adb/Dualboot/bootctl").submit();
                } catch (IOException e) {
                    log(getString(R.string.bootctl_wrong));
                    log("Exception: " + e.getMessage());
                    String flag = "";
                    flag = String.valueOf(Shell.rootAccess());
                    log("Root permission: " + flag);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            log(getString(R.string.bootctl_wrong));
            log("Exception: " + e.getMessage());
            String flag = "";
            flag = String.valueOf(Shell.rootAccess());
            log("Root permission: " + flag);
            // Should never happen!
            throw new RuntimeException(e);
        }
        //BOOTCTL END---------------------------
        //String[] cmdline1 = new String[]{"su", "-c", "/data/adb/Dualboot/bootctl hal-info"};
        //String res = exec_command(cmdline1);
        Shell.su("mv /data/adb/DuaBoot /data/adb/Duaboot").submit();
        out = Shell.su("/data/adb/Dualboot/bootctl hal-info").exec().getOut();

        if (!out.isEmpty()) {
            log(getString(R.string.Android_signature) + os + "\nHAL-info: " + out.get(0));
        }
        else {
            log(getString(R.string.Android_signature) + os + "\nHAL-info: Error");
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

            //iDB.setColorFilter(getResources().getColor(R.color.efab_disabled), PorterDuff.Mode.MULTIPLY);

        }

        /* ANDROID INFO */
        TextView tSlotAtitle = findViewById(R.id.slotatitle);
        TextView tSlotBtitle = findViewById(R.id.slotbtitle);
        TextView tSlotA = findViewById(R.id.slota);
        TextView tSlotB = findViewById(R.id.slotb);
        String sAndroidInfoA = getString(R.string.slota);
        String sAndroidInfoB = getString(R.string.slotb);
        String partition="";
        TextView tRom_sx = findViewById(R.id.tRoma);
        TextView tRom_dx = findViewById(R.id.tRomb);
        TextView tActiveA = findViewById(R.id.tActiveA);
        TextView tActiveB = findViewById(R.id.tActiveB);
        //tActiveA.setTextColor(getColor(R.color.SlotA));
        //tActiveB.setTextColor(getColor(R.color.SlotB));
        //Shell.Result result;

        result = Shell.su("mkdir /data/adb/Dualboot/system_").exec();
        result = Shell.su("mkdir /data/adb/Dualboot/data_").exec();

        if (active_slot.contains("a")) {
            tActiveA.setText("ACTIVE SLOT");
            //tActiveA.setTypeface(tRom_sx.getTypeface(), Typeface.BOLD);
            tActiveB.setText("INACTIVE SLOT");
            tActiveB.setTextColor(getColor(R.color.efab_disabled_text));
            bSB.setCompoundDrawablesWithIntrinsicBounds(R.drawable.bd, 0, 0, 0);
            bRB.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rbd, 0, 0, 0);
            //Mount system_b
            //out = Shell.su("ls -la /dev/block/by-name/ | grep system_b").exec().getOut();
            //partition = matcher_partitions(out.toString());
            //log("Mount system_b: /dev/block/" + partition);
            result = Shell.su("blkid /dev/block/by-name/system_b").exec();
            //log("System_b info: " + result.getOut().toString() + "\nError Code: " + result.getErr().toString());
            result  = Shell.su("umount /dev/block/by-name/system_b").exec();
            result  = Shell.su("mount -t ext4 /dev/block/by-name/system_b /data/adb/Dualboot/system_").exec();
            tRom_sx = findViewById(R.id.tRoma);
            tRom_dx = findViewById(R.id.tRomb);
            tSlotAtitle.setText("Slot A");
            tSlotBtitle.setText("Slot B");
            //tRom_sx.setTextColor(getColor(R.color.SlotA));
            //tRom_dx.setTextColor(getColor(R.color.SlotB));
            //tRom_dx.setTypeface(tRom_dx.getTypeface(), Typeface.BOLD_ITALIC);


        } else if (active_slot.contains("b")) {
            tActiveB.setText("ACTIVE SLOT");
            //tActiveB.setTypeface(tRom_sx.getTypeface(), Typeface.BOLD);
            tActiveA.setText("INACTIVE SLOT");
            tActiveA.setTextColor(getColor(R.color.efab_disabled_text));
            bSA.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ad, 0, 0, 0);
            bRA.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rad, 0, 0, 0);
            //Mount system_b
            result = Shell.su("blkid /dev/block/by-name/system_a").exec();
            // log("System_a info: " + result.getOut().toString() + "\nError Code: " + result.getErr().toString());
            result = Shell.su("umount /dev/block/by-name/system_a").exec();

            result = Shell.su("mount -t ext4 /dev/block/by-name/system_a /data/adb/Dualboot/system_").exec();
            tSlotBtitle.setText("Slot A");
            tSlotAtitle.setText("Slot B");
            tRom_dx = findViewById(R.id.tRoma);
            tRom_sx = findViewById(R.id.tRomb);
            //tRom_sx.setTypeface(tRom_sx.getTypeface(), Typeface.BOLD);
            //tRom_dx.setTextColor(getColor(R.color.SlotA));
            //tRom_sx.setTextColor(getColor(R.color.SlotB));
        }
        tSlotA.setText("");
        tSlotB.setText("");
        String sSx="Unknown OS";
        String sDx="Unknown OS";
        String sVal="";
        Boolean crDroid=false;
        out = Shell.su("cat /system/build.prop").exec().getOut();
        for (int i = 0; i < out.size(); i++)
        {
            sVal=out.get(i);
            if (sVal.contains("ro.build.version.security_patch=")) tSlotA.append("\n" + getString(R.string.security_patch) + out.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.system.build.version.release=")) tSlotA.append("\n" + getString(R.string.Release_version) + out.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.build.tags=release-keys=")) tSlotA.append("\n" + getString(R.string.Release_key) + out.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.system.build.date=")) tSlotA.append("\n"+getString(R.string.build) + out.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.system.build.id=")) tSlotA.append("\n" + getString(R.string.build_id) + out.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.build.flavor=")) tSlotA.append("\n" + getString(R.string.Build_flavor) + (out.get(i).split("(?<==)")[1]));
            else if (sVal.contains("ro.crdroid")) crDroid = true;

            if (out.get(i).contains("ro.build.flavor="))
            {
               if(sVal.contains("qssi")) sSx="OXYGEN OS 11";
               if(sVal.contains("android-user")) sDx="Android Emulator";
               if(sVal.contains("guacamole-user") || out.get(i).contains("OnePlus7Pro-user")) sSx="OXYGEN OS 10";
               if(sVal.contains("descendant")) sSx="DESCENDANT OS";
               if(sVal.contains("kang")) sSx="KANG OS";
               if(sVal.contains("evolution")) sSx="EVO X OS";
               if(sVal.contains("derp")) sSx="DERPFEST OS";
               if(sVal.contains("havok")) sSx="HAVOK OS";
               if(sVal.contains("crdroid")) sSx="CR DROID OS";
               if(sVal.contains("rr_")) sSx="RES REMIX OS";
               if(sVal.contains("lineage_")) sSx="LINEAGE OS";
            }
        }
        if (crDroid) sSx = "CRDROID OS";
        crDroid=false;
        out = Shell.su("cat /data/adb/Dualboot/system_/system/build.prop").exec().getOut();
        for (int i = 0; i < out.size(); i++)
        {
            sVal=out.get(i);
            if (sVal.contains("ro.build.version.security_patch=")) tSlotB.append("\n" + getString(R.string.security_patch) + out.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.system.build.version.release=")) tSlotB.append("\n" + getString(R.string.Release_version) + out.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.build.tags=release-keys=")) tSlotB.append("\n" + getString(R.string.Release_key) + out.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.system.build.date=")) tSlotB.append("\n"+getString(R.string.build) + out.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.system.build.id=")) tSlotB.append("\n" + getString(R.string.build_id) + out.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.build.flavor=")) tSlotB.append("\n" + getString(R.string.Build_flavor) + out.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.crdroid")) crDroid = true;
            if (out.get(i).contains("ro.build.flavor="))
            {
                if(sVal.contains("qssi")) sDx="OXYGEN OS 11";
                if(sVal.contains("android-user")) sDx="Android Emulator";
                if(sVal.contains("guacamole-user") || out.get(i).contains("OnePlus7Pro-user")) sDx="OXYGEN OS 10";
                if(sVal.contains("descendant")) sDx="DESCENDANT OS";
                if(sVal.contains("kang")) sDx="KANG OS";
                if(sVal.contains("evolution")) sDx="EVO X OS";
                if(sVal.contains("derp")) sDx="DERPFEST X OS";
                if(sVal.contains("havok")) sDx="HAVOK OS";
                if(sVal.contains("crdroid")) sDx="CR DROID OS";
                if(sVal.contains("rr_")) sDx="RES REMIX OS";
                if(sVal.contains("lineage_")) sDx="LINEAGE OS";
            }
        }
        if (crDroid) sDx = "CRDROID OS";

        tRom_sx.setText(sSx);
        tRom_dx.setText(sDx);
        //log("OK");
        /*END ANDROID INFO */

        String response;
        response = getIntent().getAction();
        //textView.animateText(tLog);
        switch (response) {
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
    @SuppressLint("SetTextI18n")
    private void mountSetup()
    {
        Shell.Result result;
        Shell ss = Shell.getCachedShell();
        Integer i = ss.getStatus();
        Shell.Job jb;
        List<String> res = new ArrayList<>();
        String commondatamount = commondatamount10;
        result = Shell.su("test -d " + initrc + " && echo OK || echo KO").exec();
        if(result.getOut().toString().contains("KO"))
        {
            commondatamount = commondatamount11;
        }
        Shell.sh("mkdir -p " + commondatamount).submit();
        //result = Shell.su("rm -f " + commondatamount + mInactive).exec();

        if (bSys)
        {
            jb = ss.newJob();
            //Shell.su("mkdir -p " + datamount + mSystemPartition).submit();
            jb.add("mkdir -p " + commondatamount + mSystemPartition + inactive_slot);
            jb.add("mount -t ext4 /dev/block/by-name/system_"+ inactive_slot + " " + commondatamount + mSystemPartition + inactive_slot);
            result = jb.exec();
            mSys.setText(getString(R.string.mount_inactive_system) + "\n" + sdcard + mSystemPartition + inactive_slot);
        }
        else
        {
            jb = ss.newJob();
            jb.add("umount " + commondatamount + mSystemPartition + inactive_slot);
            jb.add("rmdir " + commondatamount + mSystemPartition + inactive_slot);
            result = jb.exec();
            mSys.setText(getString(R.string.mount_inactive_system));
        }
        if (bData)
        {
            //Shell.su("mkdir -p " + datamount + mDatapPartition).submit();
            //Shell.su("mkdir -p " + commondatamount + mDatapPartition).submit();
            //result = Shell.su("echo data >> " + commondatamount + mInactive).exec();
            //result = Shell.su("mount -t " + fsdata(inactive_slot) + " /dev/block/by-name/userdata_"+ inactive_slot +" /data/adb/Dualboot/data_").exec();
            jb = ss.newJob();
            //jb.add("mkdir -p " + commondatamount + mDatapPartition + inactive_slot);
            jb.add("mkdir -p " + commondatamount + mSdcard + inactive_slot);
            result = jb.to(res).exec();
            //jb.add("echo data >> " + commondatamount + mInactive);
            jb.add("mount -t " + fsdata(inactive_slot) + " /dev/block/by-name/userdata_"+ inactive_slot + " /data/adb/Dualboot/data_");
            //jb.add("mount -o bind /data/adb/Dualboot/data_ " + commondatamount + mDatapPartition + inactive_slot);
            jb.add("mount -o bind /data/adb/Dualboot/data_/media/0 " + commondatamount + mSdcard + inactive_slot);
            result = jb.to(res).exec();
            mData.setText(getString(R.string.mount_inactive_data) + "\n"
                    //        + sdcard + mSystemPartition + inactive_slot + "\n"
                    + sdcard + mSdcard + inactive_slot);

        }
        else
        {
            jb = ss.newJob();
            //jb.add("umount " + commondatamount + mDatapPartition + inactive_slot);
            jb.add("umount /data/adb/Dualboot/data_");
            //jb.add("umount " + commondatamount + mDatapPartition + inactive_slot);
            jb.add("umount " + commondatamount + mSdcard + inactive_slot);
            result = jb.to(res).exec();
            //jb.add("rmdir " + commondatamount + mDatapPartition + inactive_slot);
            jb.add("rmdir " + commondatamount + mSdcard + inactive_slot);
            result = jb.to(res).exec();
            mData.setText(getString(R.string.mount_inactive_data));
        }
    }
    private void reloadPref()
    {
        if (fsdata(inactive_slot).contains("Encrypted"))
        {
            mData.setEnabled(false);
            mData.setText(getString(R.string.data_partition) + inactive_slot + R.string.data_encrypted);
        }
        else if (fsdata(inactive_slot).contains("Unknown")) {
            mData.setEnabled(false);
            mData.setText(R.string.unknown_fiesystem);
        }

        if (pref.contains("ErasePwd")) {
            ErasePwd = pref.getBoolean("ErasePwd", false);
            pwd.setChecked(ErasePwd);
        }
        if (pref.contains("NoWarn")) {
            bNoWarn = pref.getBoolean("NoWarn", false);
            NoWarn.setChecked(bNoWarn);
        }
        if (pref.contains("mSys")) {
            bSys = pref.getBoolean("mSys", false);
            mSys.setChecked(bSys);
            if(bSys){
                mSys.setText(getString(R.string.mount_inactive_system) + "\n"
                    + sdcard + mSystemPartition + inactive_slot);
                mountSetup();
            }
        }
        if (pref.contains("mData")) {
            bData = pref.getBoolean("mData", false);
            mData.setChecked(bData);
            if(bData) {
                mData.setText(getString(R.string.mount_inactive_data) + "\n"
                        + sdcard + mSdcard + inactive_slot);
                mountSetup();
            }
        }

    }

    public void switchSlot(MainActivity view, final String currentSlot, final int recovery) {

        Boolean deletepwd = false;
        if(bPwd && !currentSlot.contains(active_slot))
        {
            Button bp = findViewById(R.id.button5);
            bp.performClick();
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
                            Runtime.getRuntime().exec("su -c /data/adb/Dualboot/bootctl set-active-boot-slot 0");
                        }
                        if (currentSlot.contains("_a")) {
                            Runtime.getRuntime().exec("su -c /data/adb/Dualboot/bootctl set-active-boot-slot 1");
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
                    Runtime.getRuntime().exec("su -c /data/adb/Dualboot/bootctl set-active-boot-slot 0");
                }
                if (currentSlot.contains("_a")) {
                    Runtime.getRuntime().exec("su -c /data/adb/Dualboot/bootctl set-active-boot-slot 1");
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
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    //
    public void onClickInfo(View view) {
        // definisco l'intenzione di aprire l'Activity "Page1.java"
        Intent InfoPage = new Intent(MainActivity.this,Info.class);
        // passo all'attivazione dell'activity page1.java
        startActivity(InfoPage);
    }

    public void onClickNo_click(View view) {}

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
                if (res.getCode() == 2) log(getString(R.string.partition_encrypted));
                else log(getString(R.string.error_userdata_b));
                return;
            }
            out = Shell.su("umount /dev/block/by-name/userdata_b" + partition).exec().getOut();
            log("Mount " + formato + " - userdata_b: /dev/block/by-name/userdata_b");
            out =  Shell.su("mount -t " + formato + " /dev/block/by-name/userdata_b /data/adb/Dualboot/data_").exec().getOut();
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
                if (res.getCode() == 2) log(getString(R.string.partition_encrypted));
                else log(getString(R.string.error_userdata_a));
                return;
            }
            out = Shell.su("umount /dev/block//by-name/userdata_a").exec().getOut();
            log("Mount " + formato + " - userdata_a: /dev/block/by-name/userdata_a");
            out =  Shell.su("mount -t " + formato + " /dev/block//by-name/userdata_a /data/adb/Dualboot/data_").exec().getOut();
            //log("mount res: " + out.toString());
        }
        else return;

        res = Shell.su("test -f /data/adb/Dualboot/data_/system/locksettings.db").exec();
        if (res.isSuccess())
        {
            res = Shell.su("rm -f /data/adb/Dualboot/data_/system/locksettings.db").exec();
            log(getText(R.string.remove_locksettings).toString());
        }
        else
        {
            log(getText(R.string.locksettings_not_found).toString());
        }

        /*
        out = Shell.su("ls -la /data/adb/Dualboot/data_/system/lock_settings.db").exec().getOut();
        //log("lock: " + out.toString());
        if(out.toString().contains("locksettings.db"))
        {
            log(getString(R.string.remove_locksettings));
            out = Shell.su("rm -f /data/adb/Dualboot/data_/system/locksettings.db").exec().getOut();
            //log("res: " + out);
        }
        else {
            log(getString(R.string.locksettings_not_found));
        }
        */
    }
    public String fsdata(String slot)
    {
        Shell.Result res = Shell.su("blkid /dev/block/by-name/userdata_" + slot).exec();
        if(res.getOut().toString().contains("ext4"))
        {
            return "ext4";
        }
        else if(res.getOut().toString().contains("f2fs"))
        {

            return "f2fs";
        }
        else
        {
            log("blkid: " + res.getOut().toString());
            if (res.getCode() == 2) return "Encrypted";
            else return "Unknown";
        }
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
        TextView tLog = findViewById(R.id.tLog);
        tLog.append(s + "\n");
        tLog.setMovementMethod(new ScrollingMovementMethod());
        while (tLog.canScrollVertically(1)) {
            tLog.scrollBy(0, 10);
        }

    }

    public void disableButton(Button btn, String slot)
    {

    //TODO - Remove icon from password Button disable

        btn.setEnabled(false);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        if (slot.contains("A")) btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.disable_a, 0, 0, 0);
        else if (slot.contains("B"))  btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.disable_b, 0, 0, 0);
        btn.setTextColor(Color.DKGRAY);
    }

    public void onClickShareApp(View view) {
        // definisco l'intenzione di aprire l'Activity "Page1.java"
        Intent Sapp = new Intent(MainActivity.this, SharedApp.class);
        // passo all'attivazione dell'activity page1.java
        //Button btn = (Button) findViewById(R.id.bShareApp);
        //btn.setEnabled(false);
        startActivity(Sapp);

        }
}

/*
class SplashActivity extends MainActivity {

    static {
        // Set settings before the main shell can be created
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setFlags(Shell.ROOT_MOUNT_MASTER)
                .setTimeout(10)
        );
    }

}*/