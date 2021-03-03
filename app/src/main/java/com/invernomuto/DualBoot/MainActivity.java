package com.invernomuto.DualBoot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFileOutputStream;
import com.yalantis.guillotine.animation.GuillotineAnimation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    private static final long RIPPLE_DURATION = 250;
    private final static String RebootB = "RebootB";
    private final static String RebootA = "RebootA";
    private final static String RebootRB = "RebootRB";
    private final static String RebootRA = "RebootRA";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Switch pwd;
    Switch NoWarn;
    Switch mSys;
    Switch mData;
    Boolean bPwd = false;
    Boolean ErasePwd = false;
    Boolean bNoWarn = false;
    Boolean bSys = false;
    Boolean bData = false;
    String versionName;
    TextView tSlotActive;
    TextView tSlotInactive;
    TextView tSlotActiveTitle;
    TextView tSlotInactiveTitle;
    Button bRA;
    Button bRB;
    Button bSA;
    Button bSB;
    Button bEP;
    Button bBL;
    Button bShared;
    TextView tSlotAtitle;
    TextView tSlotBtitle;
    TextView tSlotA;
    TextView tSlotB;

    Shell ss = Shell.getCachedShell();
    Shell.Job jb;
    String bootctl = "/data/adb/Dualboot/bootctl";
    String initrc_A11 = "/system/etc/init/hw/init.rc";

    String baseMountPath = "/data/adb/Dualboot/";
    String baseSystem = "system_";
    String baseData = "data_";
    String activeSlot = "";
    String inactiveSlot = "";
    Map<String, String> mounts;

    String userSystem = "System_";
    String userSdcard = "SDcard_";
    String userMountPath11 = "/mnt/runtime/full/emulated/0/DualBoot/";
    String userMountPath10 = "/mnt/runtime/full/emulated/0/DualBoot/";
    String userPath = "/sdcard/DualBoot/";
    FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "invernomuto";
    private final String id = "DualBoot";

    @Override
    public void onDestroy() {
        super.onDestroy();
        Boolean im = isMounted(mounts.get("System"));
        if (im) umountBaseInactiveSystem();
        im= isMounted(mounts.get("Data"));
        if (im) umountBaseInactiveData();
        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Log.d(TAG, "Application started");
        //Preference panel
        FrameLayout root = findViewById(R.id.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        View contentHamburger = findViewById(R.id.content_hamburger);
        View guillotineMenu = LayoutInflater.from(this).inflate(R.layout.preferences_layout, null);
        root.addView(guillotineMenu);
        new GuillotineAnimation.GuillotineBuilder(guillotineMenu, guillotineMenu.findViewById(R.id.guillotine_hamburger), contentHamburger)
                .setStartDelay(RIPPLE_DURATION)
                .setActionBarViewForAnimation(toolbar)
                .setClosedOnStart(true)
                .build();
        //GUI
        TextView tVersion = findViewById(R.id.tVersione);
        ImageView iv = findViewById(R.id.iv);
        TextView tLog = findViewById(R.id.tLog);
        Button bRA = findViewById(R.id.bRecoveryA);
        Button bRB = findViewById(R.id.bRecoveryB);
        Button bSA = findViewById(R.id.bSystemA);
        Button bSB = findViewById(R.id.bSystemB);
        Button bEP = findViewById(R.id.bErasePassword);
        Button bBL = findViewById(R.id.bBootLoader);
        Button bShared = findViewById(R.id.bShareApp);
        tSlotActive   = findViewById(R.id.slota);
        tSlotInactive = findViewById(R.id.slotb);
        TextView tRom_sx = findViewById(R.id.tRoma);
        TextView tRom_dx = findViewById(R.id.tRomb);
        TextView tActiveA = findViewById(R.id.tActiveA);
        TextView tActiveB = findViewById(R.id.tActiveB);
        pwd    = findViewById(R.id.pwd);
        NoWarn = findViewById(R.id.NoWarn);
        mSys   = findViewById(R.id.mSys);
        mData  = findViewById(R.id.mData);
        mounts = new HashMap<String, String>();

        mounts.put("System", baseMountPath + baseSystem);
        mounts.put("Data", baseMountPath + baseData);
        mounts.put("uSystem", getCommonDataMount() + userSystem + inactiveSlot);
        mounts.put("uData", getCommonDataMount() + userSdcard + inactiveSlot);
        mounts.put("Error", getCommonDataMount() + "test");

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.guillotine_background_dark));

        //Setup main info
        activeSlot   = getActiveSlot();
        inactiveSlot = getInactiveSlot();
        setDirectory();
        ImageView iSys = findViewById(R.id.iSystem);
        ImageView iSD = findViewById(R.id.iSdcard);

        if (isMounted(mounts.get("uSystem"))) iSys.setImageTintList(ColorStateList.valueOf(Color.GREEN));
        if (isMounted(mounts.get("uData"))) iSD.setImageTintList(ColorStateList.valueOf(Color.GREEN));
        Boolean im = isMounted(mounts.get("System"));
        if (!im) mountBaseInactiveSystem();
        im = isMounted(mounts.get("Data"));
        if (!im) mountBaseInactiveData();

        //SETUP Components
        bRA.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ra, 0, 0, 0);
        bRB.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rb, 0, 0, 0);
        bSA.setCompoundDrawablesWithIntrinsicBounds(R.drawable.a, 0, 0, 0);
        bSB.setCompoundDrawablesWithIntrinsicBounds(R.drawable.b, 0, 0, 0);
        bEP.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_outline_lock_open_47, 0, 0, 0);
        bBL.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_round_android_47, 0, 0, 0);
        bShared.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.star_big_on, 0, 0, 0);

        //Start
        log(getString(R.string.AppStarted));
        copyBootctl();

        //Setup Version Name
        String versionName = BuildConfig.VERSION_NAME;
        tVersion.setText(getString(R.string.version) + versionName);
        log(getString(R.string.version) + versionName);

        //SELINUX CHECK
        Shell.Result result;
        result = Shell.su("getenforce").exec();
        log("Selinux: " + result.getOut().toString());

        //Swap buttons if active slot A
        swapButtons(bEP);

        //Android Version
        log(getString(R.string.system) + getAndroidVersion());

        //CheckLayout and disable SharedApp if a/b
        String sLayout = getLayout();
        if (sLayout.equals("a/b")) {
            bShared.setText(getString(R.string.no_shared_app));
            log(getString(R.string.shared_app_disabled_log));
            disableButton(bShared, "");
        }

        //Preferences
        pref   = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0); // 0 - for private mode
        editor = pref.edit();
        log(getString(R.string.reading_options));
        reloadPref();

        //set all infos
        set_InfoSlotsUI();

        //Check device and setup buttons
        if (!isValidDevice()) {
            log(getString(R.string.Device_not_compatible));
            disableButton(bRB, "B");
            disableButton(bSB, "B");
            disableButton(bRA, "A");
            disableButton(bSA, "A");
            disableButton(bEP, "A");
            disableButton(bBL, "B");
            pwd.setEnabled(false);
            NoWarn.setEnabled(false);
            mSys.setEnabled(false);
            mData.setEnabled(false);

        } else {
            if (activeSlot.contains("a")) {
                log(getString(R.string.Active_Slot) + "A");
                iv.setImageResource(R.drawable.logo_resized_a);
            }
            if (activeSlot.contains("b")) {
                log(getString(R.string.Active_Slot) + "B");
                iv.setImageResource(R.drawable.logo_resized_b);
            }
        }

        //Setup Shortcuts
        String response;
        response = getIntent().getAction();

        //textView.animateText(tLog);
        switch (response) {
            case RebootB:
                RebootTo("_b", 0);
                break;
            case RebootA:
                RebootTo("_a", 0);
                break;
            case RebootRB:
                RebootTo("_b", 1);
                break;
            case RebootRA:
                RebootTo("_a", 1);
                break;
            default:
                break;
        }

        //Setup listeners
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
                if (bbSys) {
                    umountUserInactiveSystem();
                    mountUserInactiveSystem();
                    iSys.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                    log(getString(R.string.inactive_sys_mounted) + userPath + userSdcard + inactiveSlot);
                } else {
                    umountUserInactiveSystem();
                    iSys.setImageTintList(getColorStateList(R.color.invernomuto_300));
                    log(getString(R.string.inactive_sys_unmounted));
                }
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
                if (bbData) {
                    umountUserInactiveData();
                    mountUserInactiveData();
                    iSD.setImageTintList(ColorStateList.valueOf(Color.GREEN));
                    log(getString(R.string.inactive_sdcard_mounted) +userPath + userSdcard + inactiveSlot);
                } else {
                    umountUserInactiveData();
                    iSD.setImageTintList(getColorStateList(R.color.invernomuto_300));
                    log( getString(R.string.inactive_sdcard_dismounted));
                }
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.guillotine_background_dark));
        }

    }

    //OnClickListener
    public void onClickShareApp(View view) {
        // definisco l'intenzione di aprire l'Activity "Page1.java"
        Intent Sapp = new Intent(MainActivity.this, SharedApp.class);
        // passo all'attivazione dell'activity page1.java
        //Button btn = (Button) findViewById(R.id.bShareApp);
        //btn.setEnabled(false);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Shared App Button");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Click");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        startActivity(Sapp);
    }

    public void onClickInfo(View view) {
        // definisco l'intenzione di aprire l'Activity "Page1.java"
        Intent InfoPage = new Intent(MainActivity.this, Info.class);
        // passo all'attivazione dell'activity page1.java
        startActivity(InfoPage);
    }

    public void onClickBootloader(View view) {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();

        if (!bNoWarn) {
            /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(android.R.string.dialog_alert_title);
            builder.setMessage(getString(R.string.dialog_confirmation));
            builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    jb.add("reboot bootloader").to(sRes, sErr).exec();
                }
            });
            builder.setNegativeButton(getString(android.R.string.no), null);
            AlertDialog dialog = builder.create();
            dialog.show();*/
            SweetAlertDialog.DARK_STYLE = true;
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.reboot_to_bootloader))
                    .setContentText(getString(R.string.dialog_confirmation))
                    .setConfirmText(getString(R.string.yes))
                    //.setCustomImage(R.mipmap.ic_launcher_foreground)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            jb.add("reboot bootloader").to(sRes, sErr).exec();
                        }
                    })
                    .setCancelButton(getString(R.string.Cancel), new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                        }
                    })
                    .show();
        } else {
            jb.add("reboot bootloader").to(sRes, sErr).exec();
        }
    }

    public void onClickNo_click(View view) {
    }

    public void onClickRebootA(View view) {
        RebootTo("_b", 0);
    }

    public void onClickRebootB(View view) {
        RebootTo("_a", 0);
    }

    public void onClickRRebootA(View view) {
        RebootTo("_b", 1);
    }

    public void onClickRRebootB(View view) {
        RebootTo("_a", 1);
    }

    public void onClickErasePwdA(View view) {
        String res = erasePassword();
        log(getString(R.string.erasing_password) + res);
    }

    //Useful function

    /**
     * set UI information about system slots
     */
    private void set_InfoSlotsUI(){
        TextView tActiveA = findViewById(R.id.tActiveA);
        TextView tActiveB = findViewById(R.id.tActiveB);
        bSA = findViewById(R.id.bSystemA);
        bSB = findViewById(R.id.bSystemB);
        bRA = findViewById(R.id.bRecoveryA);
        bRB = findViewById(R.id.bRecoveryB);

        if(inactiveSlot.contains("b")) {
            tActiveA.setText("ACTIVE SLOT");
            //tActiveA.setTypeface(tRom_sx.getTypeface(), Typeface.BOLD);
            tActiveB.setText("INACTIVE SLOT");
            tActiveB.setTextColor(getColor(R.color.efab_disabled_text));
            bSB.setCompoundDrawablesWithIntrinsicBounds(R.drawable.bd, 0, 0, 0);
            bRB.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rbd, 0, 0, 0);
            TextView tRom_sx = findViewById(R.id.tRoma);
            TextView tRom_dx = findViewById(R.id.tRomb);

            tRom_dx = findViewById(R.id.tRomb);
            tRom_sx = findViewById(R.id.tRoma);
            tRom_dx.setTextColor(getColor(R.color.efab_disabled_text));

            tSlotAtitle=findViewById(R.id.slot_a_title);
            tSlotBtitle=findViewById(R.id.slot_b_title);
            tSlotAtitle.setText("Slot A");
            tSlotBtitle.setText("Slot B");

            tSlotA = findViewById(R.id.slota);
            tSlotB = findViewById(R.id.slotb);

            List<String> ls = new ArrayList<>();
            ls=getSystemInfo("a");
            tRom_sx.setText(ls.get(ls.size()-1));
            String tmp = "";
            for (String s : ls ) tmp += s + "\n";

            tSlotA.setText(tmp);

            ls = new ArrayList<>();
            tmp="";
            ls=getSystemInfo("b");
            tRom_dx.setText(ls.get(ls.size()-1));
            for (String s : ls ) tmp += s + "\n";

            tSlotB.setText(tmp);


        }
        else if (inactiveSlot.contains("a")) {
            tActiveB.setText("ACTIVE SLOT");
            //tActiveB.setTypeface(tRom_sx.getTypeface(), Typeface.BOLD);
            tActiveA.setText("INACTIVE SLOT");
            tActiveA.setTextColor(getColor(R.color.efab_disabled_text));
            bSA.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ad, 0, 0, 0);
            bRA.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rad, 0, 0, 0);
            TextView tRom_dx = findViewById(R.id.tRoma);
            TextView tRom_sx = findViewById(R.id.tRomb);
            tRom_dx = findViewById(R.id.tRoma);
            tRom_sx = findViewById(R.id.tRomb);
            tRom_dx.setTextColor(getColor(R.color.efab_disabled_text));
            tSlotAtitle = findViewById(R.id.slot_a_title);
            tSlotBtitle = findViewById(R.id.slot_b_title);

            tSlotAtitle.setText("Slot B");
            tSlotBtitle.setText("Slot A");


            tSlotA = findViewById(R.id.slotb);
            tSlotB = findViewById(R.id.slota);

            List<String> ls = new ArrayList<>();
            String tmp="";
            ls=getSystemInfo("a");
            tRom_dx.setText(ls.get(ls.size()-1));
            for (String s : ls ) tmp += s + "\n";

            tSlotA.setText(tmp);

            ls = new ArrayList<>();
            tmp="";
            ls=getSystemInfo("b");
            tRom_sx.setText(ls.get(ls.size()-1));
            for (String s : ls ) tmp += s + "\n";

            tSlotB.setText(tmp);
        }

    }
    /**
     * Reload Preferences panel
     */
    private void reloadPref() {
        if (getFsType(inactiveSlot).contains("Encrypted")) {
            mData.setEnabled(false);
            mData.setText(getString(R.string.data_partition) + inactiveSlot + R.string.data_encrypted);
        } else if (getFsType(inactiveSlot).contains("Unknown")) {
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
        }
        if (pref.contains("mData")) {
            bData = pref.getBoolean("mData", false);
            mData.setChecked(bData);
        }

    }
    /**
     * Swap buttons if is active the slot A
     */
    private void swapButtons(Button bPA) {
        //Swap button if active slot is A
        if (activeSlot.contains("a")) {
            ViewGroup layout = (ViewGroup) bPA.getParent();
            if (null != layout) //for safety only  as you are doing onClick
                layout.removeView(bPA);
            layout.addView(bPA);
        }

    }

    /**
     * Erase the inactive slot password
     * return OK if successful or the error message
     */
    private String erasePassword() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        log("Mount " + getFsType(inactiveSlot) + " - userdata_: /dev/block/by-name/userdata_" + inactiveSlot);
        jb.add("test -f " + baseMountPath + baseData + "/system/locksettings.db").to(sRes, sErr).exec();
        if (sErr.isEmpty()) {
            jb   = ss.newJob();
            sRes = new ArrayList<>();
            sErr = new ArrayList<>();
            jb.add("rm -f " + baseMountPath + baseData + "/system/locksettings.db").to(sRes, sErr).exec();
            log(getText(R.string.remove_locksettings).toString());
            if (sErr.isEmpty()) {
                log(getString(R.string.dismount) + getFsType(inactiveSlot) + " - userdata_: /dev/block/by-name/userdata_" + inactiveSlot);
                return ("OK");
            } else {
                log(getString(R.string.error_to_dismount) + getFsType(inactiveSlot) + " - userdata_: /dev/block/by-name/userdata_" + inactiveSlot);
                return (sErr.toString());
            }
        } else {
            log(getText(R.string.locksettings_not_found).toString());
            return (sErr.toString());
        }
    }

    /**
     * Reboot procedure
     *
     * @param currentSlot - Slot to reboot
     * @param recovery    - 1 to recovery, 0 to system
     */
    private void rebootProc(String currentSlot, int recovery) {
        //TextView textView = (TextView) findViewById(R.id.textview);
        if (currentSlot.contains("_b")) {
            jb.add("/data/adb/Dualboot/bootctl set-active-boot-slot 0").submit();
        }
        if (currentSlot.contains("_a")) {
            jb.add("/data/adb/Dualboot/bootctl set-active-boot-slot 1").submit();
        }
        if (recovery == 0) {
            jb.add("reboot").submit();
        } else {
            jb.add("reboot recovery").submit();
        }
    }

    /**
     * Reboot the phone
     *
     * @param currentSlot Slot to reboot
     * @param recovery    1 to recovery, 0 to system
     */
    private void RebootTo(String currentSlot, int recovery) {

        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();

        Boolean deletepwd = false;
        if (bPwd && currentSlot.contains(inactiveSlot)) {
            String res = erasePassword();
            log(getString(R.string.erasing_password) + res);
        }

        if (!bNoWarn) {
            String rec = getString(R.string.system);
            String _Slot = "A";
            if (currentSlot.contains("a")) _Slot= "B";
            if(recovery == 1) rec = getString(R.string.recovery);
            SweetAlertDialog.DARK_STYLE = true;
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.reboot_to) + " " + rec + " " + _Slot)
                    .setContentText(getString(R.string.dialog_confirmation))
                    .setConfirmText(getString(R.string.yes))
                    //.setCustomImage(R.mipmap.ic_launcher_foreground)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            rebootProc(currentSlot, recovery);
                        }
                    })
                    .setCancelButton(getString(R.string.Cancel), new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                        }
                    })
                    .show();
            /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(android.R.string.dialog_alert_title);
            builder.setMessage(getString(R.string.dialog_confirmation));
            builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    rebootProc(currentSlot, recovery);
                }
            });
            builder.setNegativeButton(getString(android.R.string.no), null);
            AlertDialog dialog = builder.create();
            dialog.show();*/
        } else {
            rebootProc(currentSlot, recovery);
        }
    }

    /**
     * Return a String with the active slot: a or b
     */
    private List<String> getSystemInfo(String slot) {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();

        List<String> ls = new ArrayList<>();
        //tSlotActive.setText("");
        //tSlotInactive.setText("");
        String sSx = "Unknown OS";
        String sDx = "Unknown OS";
        String sVal = "";
        Boolean crDroid;
        crDroid = false;

        if (slot.contains(activeSlot)) {
            jb.add("cat /system/build.prop").to(sRes, sErr).exec();
            if (!sErr.isEmpty()) {
                ls.add("ERROR: " + sErr.toString());
                return ls;
            }
        } else {
            jb.add("cat " + baseMountPath + baseSystem + "/system/build.prop").to(sRes, sErr).exec();
            if (!sErr.isEmpty()) {
                ls.add("ERROR: " + sErr.toString());
                return ls;
            }
        }
        for (int i = 0; i < sRes.size(); i++) {
            sVal = sRes.get(i);
            if (sVal.contains("ro.build.version.security_patch=")) ls.add(getString(R.string.security_patch) + sRes.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.system.build.version.release=")) ls.add(getString(R.string.Release_version) + sRes.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.build.tags=release-keys=")) ls.add(getString(R.string.Release_key) + sRes.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.system.build.date=")) ls.add(getString(R.string.build) + sRes.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.system.build.id=")) ls.add(getString(R.string.build_id) + sRes.get(i).split("(?<==)")[1]);
            else if (sVal.contains("ro.build.flavor=")) ls.add(getString(R.string.Build_flavor) + (sRes.get(i).split("(?<==)")[1]));
            else if (sVal.contains("ro.crdroid")) crDroid = true;

            if (sRes.get(i).contains("ro.build.flavor=")) {
                Log.d(TAG, sVal);
                if (sVal.contains("qssi")) ls.add("OXYGEN OS 11");
                if (sVal.contains("android-user")) ls.add("Android Emulator");
                if (sVal.contains("guacamole-user") || sVal.contains("OnePlus7Pro-user")) ls.add("OXYGEN OS 10");
                if (sVal.contains("descendant")) ls.add("DESCENDANT OS");
                if (sVal.contains("kang")) ls.add("KANG OS");
                if (sVal.contains("evolution")) ls.add("EVO X OS");
                if (sVal.contains("derp")) ls.add("DERPFEST OS");
                if (sVal.contains("havok")) ls.add("HAVOK OS");
                if (sVal.contains("crdroid")) ls.add("CR DROID OS");
                if (sVal.contains("rr_")) ls.add("RES REMIX OS");
                if (sVal.contains("lineage_")) ls.add("LINEAGE OS");
            }

            if (crDroid) sSx = "CRDROID OS";
        }
        return ls;
    }

    private String getActiveSlot() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        jb.add(bootctl + " get-current-slot").to(sRes, sErr).exec();
        if (sRes.contains("0")) return "a";
        else if (sRes.contains("1")) return "b";
        return "n";
    }

    /**
     * Return a String with the inactive slot: a or b
     */
    private String getInactiveSlot() {
        String as = getActiveSlot();
        if (as.contains("a")) return "b";
        else if (as.contains("b")) return "a";
        return "n";
    }

    /**
     * Return a String with Android version: A11 or A10
     */
    private String getAndroidVersion() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();

        jb.add("[ -f " + initrc_A11 + " ] && echo OK || echo KO").to(sRes, sErr).exec();
        Log.d(TAG, "Android: " + sRes.toString());
        if (sRes.toString().contains("OK")) {
            return "Android 11";
        }
        return "Android 10";
    }

    /**
     * Return a String with the path of sdcard
     * there is a difference between Android 10 and Android 11
     */
    private String getCommonDataMount() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        String commondatamount = userMountPath10;

        jb.add("[ -f /system/etc/init/hw/init.rc ] && echo OK || echo KO").to(sRes, sErr).exec();
        if (sRes.toString().contains("OK")) {
            commondatamount = userMountPath11;
        }
        return commondatamount;
    }

    /**
     * Return a String with the current layout of device: a/b or a/b/c
     */
    private String getLayout() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();

        jb.add("test -d /datacommon && echo OK || echo KO").to(sRes, sErr).exec();
        if (sRes.contains("KO")) {
            return ("a/b");
        } else {
            return ("a/b/c");
        }
    }

    /**
     * Remove all temporary directories
     */
    private void removeDirectory() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        jb.add("rmdir " + getCommonDataMount() + userSystem + inactiveSlot).to(sRes, sErr).exec();
        jb.add("rmdir " + getCommonDataMount() + userSdcard + inactiveSlot).to(sRes, sErr).exec();
    }

    /**
     * Create all working directories
     */
    private void setDirectory() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        jb.add("rmdir " + getCommonDataMount() + "*").to(sRes, sErr).exec();
        jb.add("mkdir " + getCommonDataMount()).to(sRes, sErr).exec();
        jb.add("mkdir " + getCommonDataMount() + userSystem + inactiveSlot).to(sRes, sErr).exec();
        jb.add("mkdir " + getCommonDataMount() + userSdcard + inactiveSlot).to(sRes, sErr).exec();
        jb.add("mkdir " + baseMountPath).to(sRes, sErr).exec();
        jb.add("mkdir " + baseMountPath + baseSystem).to(sRes, sErr).exec();
        jb.add("mkdir " + baseMountPath + baseData).to(sRes, sErr).exec();
    }

    /**
     * Check if partition is mounted
     *
     * @param partition - partition to check
     * @return true if mounted, false if not
     */
    private boolean isMounted(String partition) {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        jb.add("mount | grep " + partition).to(sRes, sErr).exec();
        Log.d(TAG,"mount | grep " + partition);
        Log.d(TAG, "RES: " + !sRes.isEmpty());
        return !sRes.isEmpty();
    }

    /**
     * Mount the inactive System to /data/adb/Dualboot/system_
     * Return a String with OK if success of the error
     */
    private String mountBaseInactiveSystem() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        if(!isMounted(mounts.get("System"))) {
            jb.add("mount -t ext4 /dev/block/by-name/system_" + inactiveSlot + " " + baseMountPath + baseSystem).to(sRes, sErr).exec();
            if (!sErr.isEmpty()) {
                return sErr.toString();
            }
        }
        return "OK";
    }

    /**
     * Dismount the inactive System to /data/adb/Dualboot
     * Return a String with OK if success of the error
     */
    private String umountBaseInactiveSystem() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        if(isMounted(mounts.get("System"))) {
            jb.add("umount " + baseMountPath + baseSystem).to(sRes, sErr).exec();
            if (!sErr.isEmpty()) {
                return sErr.toString();
            }
        }
        return "OK";
    }

    /**
     * Bind Mount the inactive System to /sdcard/DualBoot/System_$inactive_slot
     * Return a String with OK if success of the error
     */
    private String mountUserInactiveSystem() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        if(!isMounted(mounts.get("uSystem"))) {
            jb.add("mkdir " + getCommonDataMount() + userSystem + inactiveSlot).to(sRes, sErr).exec();
            sRes = new ArrayList<>();
            jb.add("mount -o bind " + baseMountPath + baseSystem + " "
                    + getCommonDataMount() + userSystem + inactiveSlot).to(sRes, sErr).exec();
            if (!sErr.isEmpty()) {
                return sErr.toString();
            }
        }
        return "OK";
    }

    /**
     * Dismount the inactive System to /sdcard/DualBoot/System_$inactive_slot
     * Return a String with OK if success of the error
     */
    private String umountUserInactiveSystem() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        //jb.add("umount " + getCommonDataMount() + userSystem + inactiveSlot).to(sRes, sErr).exec();
        if(isMounted(mounts.get("uSystem"))) {
            jb.add("umount " + getCommonDataMount() + userSystem + inactiveSlot).to(sRes, sErr).exec();
            Log.d(TAG, sErr.toString());
            if (!sErr.isEmpty()) {
                return sErr.toString();
            }
            jb.add("rmdir " + getCommonDataMount() + userSystem + inactiveSlot).to(sRes, sErr).exec();
        }

        return "OK";
    }

    /**
     * Mount the inactive Userdata to /data/adb/Dualboot/data_
     * Return a String with OK if success of the error
     */
    private String mountBaseInactiveData() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        if(!isMounted(mounts.get("Data"))) {
            jb.add("mount -t " + getFsType(getInactiveSlot()) + " /dev/block/by-name/userdata_" + inactiveSlot + " " + baseMountPath + baseData).to(sRes, sErr).exec();
            if (!sErr.isEmpty()) {
                return sErr.toString();
            }
        }
        return "OK";
    }

    /**
     * Dismount the inactive Userdata to /data/adb/Dualboot/data_
     * Return a String with OK if success of the error
     */
    private String umountBaseInactiveData() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        if(isMounted(mounts.get("Data"))) {
            jb.add("umount " + baseMountPath + baseData).to(sRes, sErr).exec();

            if (!sErr.isEmpty()) {
                return sErr.toString();
            }
        }
        return "OK";
    }

    /**
     * Bind Mount the inactive Sdcard to /sdcard/DualBoot/SDcard_$inactive_slot
     * Return a String with OK if success of the error
     */
    private String mountUserInactiveData() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        if(!isMounted(mounts.get("uData"))) {
            jb.add("mkdir " + getCommonDataMount() + userSdcard + inactiveSlot).to(sRes, sErr).exec();
            sRes = new ArrayList<>();
            sErr = new ArrayList<>();
            jb.add("mount -o bind " + baseMountPath + baseData + "/media/0 "
                    + getCommonDataMount() + userSdcard + inactiveSlot).to(sRes, sErr).exec();
            if (!sErr.isEmpty()) {
                return sErr.toString();
            }
        }
        return "OK";
    }

    /**
     * Dismount the inactive Sdcard to /sdcard/DualBoot/SDcard_$inactive_slot
     * Return a String with OK if success of the error
     */
    private String umountUserInactiveData() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        if(isMounted(mounts.get("uData"))) {
            jb.add("umount "
                    + getCommonDataMount() + userSdcard + inactiveSlot).to(sRes, sErr).exec();
            if (!sErr.isEmpty()) {
                return sErr.toString();
            }
            jb.add("rmdir " + getCommonDataMount() + userSdcard + inactiveSlot).to(sRes, sErr).exec();
        }
        return "OK";
    }

    /**
     * Return a String with the current filestem type:
     * ext4 / f2fs / Encrypted / Unknown
     *
     * @param slot
     */
    private String getFsType(String slot) {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        jb.add("blkid /dev/block/by-name/userdata_" + inactiveSlot).to(sRes, sErr).exec();
        if (sRes.toString().contains("ext4")) {
            return "ext4";
        } else if (sRes.toString().contains("f2fs")) {

            return "f2fs";
        } else if (sErr.isEmpty()) {
            log("blkid: " + sRes.toString());
            return "Encrypted";
        } else {
            log("blkid: " + sErr.toString());
            return "Unknown";
        }
    }

    /**
     * Return if the device is Valid or not
     */
    private Boolean isValidDevice() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        jb.add("/data/adb/Dualboot/bootctl hal-info").to(sRes, sErr).exec();
        return sErr.isEmpty();
    }

    /**
     * Return a String with value of HAL info or the error
     */
    private String getHAL() {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        jb.add(bootctl + " hal-info").to(sRes, sErr).exec();
        if (!sErr.isEmpty()) {
            return (getString(R.string.bootctl_error) + sErr.toString());
        } else if (sRes.isEmpty()) {
            return (getString(R.string.bootctl_empty));
        }
        return sRes.toString();
    }

    /**
     * Copy the bootctl to /data/adb/Dualboot
     */
    private void copyBootctl() {
        try {
            jb = ss.newJob();
            List<String> sRes = new ArrayList<>();
            List<String> sErr = new ArrayList<>();
            jb.add("mv /data/adb/DualBoot /data/adb/Dualboot").to(sRes, sErr).exec();
            InputStream is = getAssets().open("bootctl");
            log(getString(R.string.installing_bootctl));
            // We guarantee that the available method returns the total
            // size of the asset...  of course, this does mean that a single
            // asset can't be more than 2 gigs.
            int size = is.available();
            if (size > 0) {
                try (InputStream in = is;
                     OutputStream outp = SuFileOutputStream.open(bootctl)) {
                    copyFile(is, outp);
                    jb.add("chmod +x " + bootctl).submit();
                    jb.add("chcon u:object_r:system_file:s0 " + bootctl).submit();
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
    }

    /**
     * copyfile
     *
     * @param in
     * @param out
     */
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Disable a button
     *
     * @param btn  the button to disable
     * @param slot the slot
     */
    private void disableButton(Button btn, String slot) {
        btn.setEnabled(false);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        if (slot.contains("A")) btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.disable_a, 0, 0, 0);
        else if (slot.contains("B")) btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.disable_b, 0, 0, 0);
        btn.setTextColor(Color.DKGRAY);
    }

    /**
     * Utility log function
     * print the String to Console Log
     *
     * @param s the String
     */
    public void log(String s) {
        TextView tLog = findViewById(R.id.tLog);
        tLog.append(s + "\n");
        tLog.setMovementMethod(new ScrollingMovementMethod());
        while (tLog.canScrollVertically(1)) {
            tLog.scrollBy(0, 10);
        }

    }
}
