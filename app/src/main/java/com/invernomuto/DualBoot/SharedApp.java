package com.invernomuto.DualBoot;

import static com.invernomuto.DualBoot.MainActivity.DEBUG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.topjohnwu.superuser.Shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SharedApp extends AppCompatActivity {

    //private static final Boolean DEBUG = false;
    private static final String TAG = "invernomuto";

    String datacommon = "/datacommon/SharedData/";
    String datacommon_debug = "/sdcard/SharedData/";
    String datamount = "datamount.conf";
    String selapplist = "selapplist.conf";
    String apps_date = "apps_date.conf";
    Button btn;
    ImageView lvSave;
    Shell ss = Shell.getCachedShell();
    Shell.Job jb;
    SwipeMenuListView listView;
    int number_of_app;
    List<sApp> appList;
    List<String> SharedDataList;
    //SharedPreferences sharedPreferences;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Handler handler = new Handler();
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.guillotine_background_dark));
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.guillotine_background_dark));
        }
        if(DEBUG) datacommon = datacommon_debug;

        //mExplosionField = ExplosionField.attach2Window(this);
        //sharedPreferences = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0);
        setContentView(R.layout.shared_app);
        btn = findViewById(R.id.bShareApp);
        TextView tTitle = findViewById(R.id.TitleSaveApp);
        appList = new ArrayList<sApp>();
        //ImageView im = (ImageView) findViewById(R.id.SaveApp)
        lvSave = findViewById(R.id.bSaveApp);
        //SharedPreferences pref = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0); // 0 - for private mode
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        List<ApplicationInfo> installedApps = new ArrayList<ApplicationInfo>();

        listView = findViewById(R.id.listapp);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        menuCreator();
        SharedDataListPopulate();
        List<String> list = new ArrayList<String>();
        list = Shell.su("cat " + datacommon + selapplist).exec().getOut();
        tTitle.setText(getText(R.string.shared_app_title) + " (" + list.size() + ")");
        int i = 0;
        String source_path;
        for (ApplicationInfo app : apps) {

            //checks for flags; if flagged, check if updated system app
            // if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            //Not interested in
            //} else

            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0 || app.packageName.contains("com.google.android.apps")) {
                String label = (String) pm.getApplicationLabel(app);
                Drawable icon = pm.getApplicationIcon(app);

                //Set<String> checkedItemsSource = sharedPreferences.getStringSet("checked_items", new HashSet<String>());
                //SparseBooleanArray checkedItems = convertToCheckedItems(checkedItemsSource);
                Boolean checked = false;
                List<String> sRes = new ArrayList<>();
                List<String> sErr = new ArrayList<>();

                for (int ix = 0; ix < list.size(); ix++) {
                    String p = list.get(ix);
                    if (p.contains(app.processName)) {
                        checked = true;
                        i++;
                    }
                }
                source_path = app.sourceDir;
                Boolean ShareData = false;
                for (String sData : SharedDataList) {
                    if (sData.contains(app.processName)) ShareData = true;
                }
                if (!label.contains("DualBoot")) {
                    sApp sapp = new sApp(
                            app.processName,
                            label,
                            app.dataDir,
                            datacommon + app.processName,
                            icon,
                            ShareData,
                            checked);
                    appList.add(sapp);
                }
                //Discard this one
                //in this case, it should be a user-installed app
            } else {
                //installedApps.add(app);
            }
        }
        Collections.sort(appList, new SampleComparator());

        //create an ArrayAdaptar from the String Array
        AppAdapter adapter = new AppAdapter(this, R.layout.application_detail, appList, SharedDataList);
        listView.setAdapter(adapter);
        lvSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView iSave = findViewById(R.id.bSaveApp);
                ImageView iBack = findViewById(R.id.content_hamburger);
                iSave.setEnabled(false);
                iBack.setEnabled(false);
                new asyncSaveApplist().execute();
                //iSave.setEnabled(true);
                //iBack.setEnabled(true);


            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> parent, View item,
                                     int position, long id) {
                sApp sapp = adapter.getItem( position );

            }
        });

        listView.setOnMenuItemClickListener( new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                ListView lv = findViewById(R.id.listapp);
                sApp sapp = (sApp) lv.getAdapter().getItem(position);
                switch (index) {
                    case 0:
                        // Restore
                        SweetAlertDialog.DARK_STYLE = true;
                        new SweetAlertDialog(lv.getContext(), SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(getString(R.string.restore_data))
                                .setContentText(getString(R.string.resore_data_content) + sapp.getAppName() + "<br/>")
                                .setConfirmText(getString(R.string.yes))
                                //.setCustomImage(R.mipmap.ic_launcher_foreground)
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        //jb.add("reboot bootloader").to(sRes, sErr).exec();
                                        Boolean ism = false;
                                        ism = isMounted(sapp.getDataPath());
                                        Shell sa = Shell.getCachedShell();
                                        Shell.Job sb = sa.newJob();
                                        List<String> sRes = new ArrayList<>();
                                        List<String> sErr = new ArrayList<>();
                                        String dbUser = "";
                                        jb.add("stat -c '%U' " + sapp.getCommonPath()).to(sRes,sErr).exec();
                                        dbUser = sRes.get(sRes.size()-1);
                                        if (ism)
                                        {
                                            int i=0;
                                            do {
                                                sb.add("am kill " + sapp.getpName()).submit();
                                                sb.add("umount " + sapp.getDataPath()).submit();
                                                sb = ss.newJob();
                                                sRes = new ArrayList<>();
                                                sErr = new ArrayList<>();
                                                sErr.clear();
                                                i++;
                                                jb.add("mount | grep " + sapp.getpName()).to(sRes, sErr).exec();
                                                //if(i==20) break;
                                            }while (sErr.size() != 0);
                                        }
                                        /*sb.add("rm -rf " + sapp.getCommonPath()).to(sRes, sErr).exec();
                                        if(sErr.size() ==0) {
                                            Log.d(TAG, "Erased : " + sapp.getCommonPath());
                                        }*/
                                        jb = ss.newJob();
                                        sRes = new ArrayList<>();
                                        sRes.clear();
                                        sErr.clear();
                                        String sUser, sGroup, sContext, sPath;
                                        sUser="x";
                                        sPath = sapp.getDataPath();
                                        do {
                                            try {
                                                TimeUnit.SECONDS.sleep(1);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            jb.add("stat -c '%U' " + sPath).to(sRes,sErr).exec();
                                            sUser = sRes.get(sRes.size()-1);
                                        }while (sUser == dbUser);

                                        jb.add("stat -c '%G' " + sPath).to(sRes,sErr).exec();
                                        sGroup = sRes.get(sRes.size()-1);
                                        jb.add("ls -laZ " + sPath).to(sRes,sErr).exec();
                                        String lContext = sRes.get(sRes.size()-1);
                                        String[] lString = lContext.split(" ");
                                        sContext = "";
                                        for(String s : lString)
                                        {
                                            if (s.contains("u:")) sContext = s;
                                        }
                                        jb.add("rm -rf " + sPath).submit();
                                        jb.add("cp -r " + sapp.getCommonPath() + " " + sPath).submit();
                                        jb.add("chown -R " + sUser + ":" + sGroup + " " + sPath).submit();
                                        jb.add("chcon -R " + sContext + " " + sPath).submit();
                                        jb.add("restorecon -Rv " + sPath).submit();


                                        ListView rlv = findViewById(R.id.listapp);
                                        appList.get(position).setSelected(false);
                                        appList.get(position).setShared(false);
                                        String cmd = "sed -i '/"+sapp.getpName()+"/d' " + datacommon + selapplist;
                                        jb.add(cmd).submit();
                                        int x=0;
                                        for (sApp a : appList)
                                        {
                                            if (a.getSelected()) x++;
                                        }
                                        SharedDataListPopulate();
                                        TextView tTitle = findViewById(R.id.TitleSaveApp);
                                        Toast.makeText(getApplicationContext(), getString(R.string.data_app_deleted), Toast.LENGTH_SHORT).show();
                                        tTitle.setText(getText(R.string.shared_app_title) + " (" + x + ")" );

                                        AppAdapter adapter = new AppAdapter(rlv.getContext(), R.layout.application_detail, appList, SharedDataList);
                                        rlv.setAdapter(adapter);
                                        rlv.destroyDrawingCache();
                                        rlv.setVisibility(ListView.INVISIBLE);
                                        rlv.setVisibility(ListView.VISIBLE);
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .setCancelButton(getString(R.string.Cancel), new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                    }
                                })
                                .show();
                        break;
                    case 1:
                        // DELETE
                        SweetAlertDialog.DARK_STYLE = true;
                        new SweetAlertDialog(lv.getContext(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText(getString(R.string.delete_data))
                                .setContentText(getString(R.string.delete_data_content) + sapp.getAppName() + "<br/>")
                                .setConfirmText(getString(R.string.yes))
                                //.setCustomImage(R.mipmap.ic_launcher_foreground)
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        //jb.add("reboot bootloader").to(sRes, sErr).exec();
                                        Boolean ism = false;
                                        ism = isMounted(sapp.getDataPath());
                                        Shell sa = Shell.getCachedShell();
                                        Shell.Job sb = sa.newJob();
                                        List<String> sRes = new ArrayList<>();
                                        List<String> sErr = new ArrayList<>();
                                        if (ism)
                                        {
                                            int i=0;
                                            do {
                                                sb.add("am kill " + sapp.getpName()).submit();
                                                sb.add("umount " + sapp.getDataPath()).submit();
                                                sb = ss.newJob();
                                                sRes = new ArrayList<>();
                                                sErr = new ArrayList<>();
                                                sErr.clear();
                                                i++;
                                                jb.add("ls -la " + sapp.getCommonPath()).to(sRes, sErr).exec();
                                                if(i==20) break;
                                            }while (sErr.size() != 0);
                                        }
                                        sb.add("rm -rf " + sapp.getCommonPath()).to(sRes, sErr).exec();
                                        if(sErr.size() ==0) {
                                            Log.d(TAG, "Erased : " + sapp.getCommonPath());
                                        }
                                        ListView rlv = findViewById(R.id.listapp);
                                        appList.get(position).setSelected(false);
                                        appList.get(position).setShared(false);
                                        String cmd = "sed -i '/"+sapp.getpName()+"/d' " + datacommon + selapplist;
                                        jb.add(cmd).submit();
                                        int x=0;
                                        for (sApp a : appList)
                                        {
                                            if (a.getSelected()) x++;
                                        }
                                        SharedDataListPopulate();
                                        TextView tTitle = findViewById(R.id.TitleSaveApp);
                                        Toast.makeText(getApplicationContext(), getString(R.string.data_app_deleted), Toast.LENGTH_SHORT).show();
                                        tTitle.setText(getText(R.string.shared_app_title) + " (" + x + ")" );

                                        AppAdapter adapter = new AppAdapter(rlv.getContext(), R.layout.application_detail, appList, SharedDataList);
                                        rlv.setAdapter(adapter);
                                        rlv.destroyDrawingCache();
                                        rlv.setVisibility(ListView.INVISIBLE);
                                        rlv.setVisibility(ListView.VISIBLE);
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .setCancelButton(getString(R.string.Cancel), new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                    }
                                })
                                .show();
                        Log.d(TAG, "app ---------" + sapp.getAppName());
                        Log.d(TAG, "common ---------" + sapp.getCommonPath());
                        Log.d(TAG, "data ---------" + sapp.getDataPath());
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }
    private void SharedDataListPopulate(){
        Shell.Job jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        jb.add("cd " + datacommon + " && ls -d */").to(sRes,sErr).exec();
        SharedDataList = new ArrayList<>();
        SharedDataList.clear();
        SharedDataList.addAll(sRes);
    }
    private boolean isMounted(String partition) {
        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        jb.add("mount | grep " + partition).to(sRes, sErr).exec();
        Log.d(TAG,"mount | grep " + partition);
        Log.d(TAG, "RES: " + !sRes.isEmpty());
        return !sRes.isEmpty();
    }
    private void menuCreator(){
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                // Create different menus depending on the view type
                switch (menu.getViewType()) {
                    case 0:
                        SwipeMenuItem rollBack = new SwipeMenuItem(
                                getApplicationContext());
                        // set item background
                        rollBack.setBackground(new ColorDrawable(Color.RED));
                        // set item width
                        rollBack.setWidth(250);
                        // set item title
                        //rollBack.setTitle("Rollback");
                        // set item title fontsize
                        //rollBack.setTitleSize(18);
                        // set item title font color
                        //rollBack.setTitleColor(Color.BLACK);
                        rollBack.setIcon(R.drawable.baseline_restore_red_200_36dp);
                        // add to menu
                        menu.addMenuItem(rollBack);

                        // create "delete" item
                        SwipeMenuItem deleteItem = new SwipeMenuItem(
                                getApplicationContext());
                        // set item background
                        deleteItem.setBackground(new ColorDrawable(Color.BLUE));
                        // set item width
                        deleteItem.setWidth(250);
                        //deleteItem.setTitle("Delete");
                        // set item title fontsize
                        //deleteItem.setTitleSize(18);
                        // set item title font color
                        //deleteItem.setTitleColor(Color.BLACK);
                        // set a icon
                        deleteItem.setIcon(R.drawable.round_delete_forever_blue_200_36dp);
                        // add to menu
                        menu.addMenuItem(deleteItem);
                        break;
                    case 1:
                        menu.getContext();
                        break;
                }
            }
        };
        listView.setMenuCreator(creator);
    }
    private class cInfo{
        int progress;
        String name;
        public cInfo(int progress, String name) {
            this.progress=progress;
            this.name=name;
        }
    }



    private class asyncSaveApplist extends AsyncTask<Void, cInfo, Boolean> {
        NumberProgressBar bar;
        TextView tWork;
        int total;
        @Override
        protected Boolean doInBackground(Void... voids) {
            bar = findViewById(R.id.number_progress_bar);
            tWork = findViewById(R.id.tWork);
            Shell.Result result = Shell.su("rm -f " + datacommon + datamount).exec();
            result = Shell.su("rm -f " + datacommon + selapplist).exec();
            result = Shell.su("rm -f " + datacommon + apps_date).exec();
            Shell.su("mkdir " + datacommon).exec();
            Shell.su("chown 1023:1023 " + datacommon);
            Set<String> stringSet = new HashSet<>();
            number_of_app = 0;
            total = listView.getCount();
            bar.setMax(total);
            //Integer value = 0;
            for (Integer i = 0; i < total; i++) {
                //value = i * 100 / total;
                sApp s = (sApp) listView.getItemAtPosition(i);
                cInfo info = new cInfo(i,s.getAppName());
                publishProgress(info);
                if (s.getSelected()) {
                    String val = s.getpName();
                    stringSet.add(val);
                    doTheMagic(s);
                    number_of_app++;
                    appList.get(i).setShared(true);
                }
            }
            return true;
        }

        protected void onProgressUpdate(cInfo... info) {

            bar.setProgress(info[0].progress);
            tWork.setText(info[0].name);

            //RoundedProgressBar rbp = findViewById(R.id.bar);
            //rbp.setProgressPercentage(progress[0], true);
        }
        @Override
        protected void onPostExecute(Boolean bool)
        {
            //super.onPostExecute(true);
            TextView tTitle = findViewById(R.id.TitleSaveApp);
            tWork.setText(getString(R.string.idle));
            //RoundedProgressBar rbp = findViewById(R.id.bar);
            //rbp.setProgressPercentage(100.0,true);
            bar.setProgress(total);
            SharedDataListPopulate();
            Toast.makeText(getApplicationContext(), getString(R.string.application_list_saved), Toast.LENGTH_SHORT).show();
            tTitle.setText(getText(R.string.shared_app_title) + " (" + number_of_app + ")" );
            ListView rlv = findViewById(R.id.listapp);
            AppAdapter adapter = new AppAdapter(rlv.getContext(), R.layout.application_detail, appList, SharedDataList);
            rlv.setAdapter(adapter);
            rlv.destroyDrawingCache();
            rlv.setVisibility(ListView.INVISIBLE);
            rlv.setVisibility(ListView.VISIBLE);
            adapter.notifyDataSetChanged();
            ImageView iSave = findViewById(R.id.bSaveApp);
            ImageView iBack = findViewById(R.id.content_hamburger);
            iSave.setEnabled(true);
            iBack.setEnabled(true);
        }

    }

    class SampleComparator implements Comparator<sApp> {
        @Override
        public int compare(sApp o1, sApp o2) {

            return o1.getAppName().compareTo(o2.getAppName());
        }
    }

    public void doTheMagic(sApp s)
    {
        Shell.Result result;
        /*Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_MOUNT_MASTER)
                //.setFlags(Shell.ROOT_MOUNT_MASTER)
        );
        Shell su = Shell.*/

        jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        int i=0;
        do {
            jb.add("am kill " + s.getpName()).to(sRes,sErr).exec();
            jb.add("umount " + s.getDataPath()).to(sRes, sErr).exec();
            jb = ss.newJob();
            sRes = new ArrayList<>();
            sErr = new ArrayList<>();
            sErr.clear();
            i++;
            jb.add("mount | grep " + s.getpName()).to(sRes, sErr).exec();
            if(i==20) break;
        }while (sRes.size() != 0);
        sRes.clear();
        jb.add("test -d " + s.getCommonPath() + " && echo OK || echo KO").to(sRes,sErr).exec();
        String res = sRes.toString();
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        //Thread.yield();
        //Toast.makeText(getApplicationContext(), (getString(R.string.working_on) + s.AppName), Toast.LENGTH_SHORT).show();
        Thread.yield();
        if (res.contains("KO")) {
            jb.add("cp -r --preserve=all " + s.getDataPath() + " " + s.getCommonPath()).submit();
            //jb.add("restorecon -R " + s.getDataPath() + " " + s.getCommonPath()).submit();
            jb.add("chmod -R 777 " + s.getCommonPath()).submit();
        }
        //su.newJob().add("mount -o bind " + s.commonPath + " " + s.dataPath).exec();
        //result = su.newJob().exec();
        jb.add("mount -o bind " + s.getCommonPath() + " " + s.getDataPath()).submit();
        //jb.add("mount | grep " + s.pName).to(sRes,sErr).exec();
        jb = ss.newJob();
        sRes.clear();
        jb.add("echo " + s.getCommonPath() + " " + s.getDataPath() + " >> " + datacommon + datamount).submit();
        jb = ss.newJob();
        sRes.clear();
        jb.add("echo " + s.getpName() + " " + ts + " >> " + datacommon + apps_date).submit();
        jb = ss.newJob();
        sRes.clear();
        jb.add("echo " + s.getpName() + " >> " + datacommon + selapplist).submit();
//        result = Shell.su("echo mount -o bind " + s.commonPath + " " + s.dataPath + " >> " + datacommon + datamount + ".sh").exec();
//        result = Shell.su("chmod -R 755 " + datacommon + datamount + ".sh").exec();
//        result = Shell.su("./" + datacommon + datamount + ".sh").exec();
    }
    public void onClickExit(View view) {
        try {
            btn.setEnabled(true);
        }
        catch (Exception e){}
        SharedApp.this.finish();
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

class AppAdapter extends StableArrayAdapter<sApp> {
    List<sApp> sAppList;
    //private Boolean DEBUG = false;
    String datacommon = "/datacommon/SharedData/";
    String datacommon_debug = "/sdcard/SharedData/";
    String datamount = "datamount.conf";
    String selapplist = "selapplist.conf";
    String apps_date = "apps_date.conf";
    List<String> appSelected;
    private ArrayList<String> SharedDataList;

    public AppAdapter(Context context, int textViewResourceId,
                                 List<sApp> objects, List<String> appSelected) {
        super(context, textViewResourceId, objects);
        sAppList = objects;
        this.appSelected = appSelected;
        if (DEBUG) datacommon=datacommon_debug;
    }
    @Override
    public int getViewTypeCount() {
        // menu type count
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        sApp sapp = getItem(position);
        Boolean ShareData = false;
        if (appSelected.toString().contains(sapp.getpName())) ShareData = true;
        if(ShareData)
        {
            return 0;
        }
        return 1;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return getViewOptimize(position, convertView, parent);
    }

    public View getViewOptimize(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        sApp sapp = getItem(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.application_detail, null);
            viewHolder = new ViewHolder();
            viewHolder.icon = convertView.findViewById(R.id.appicons);
            viewHolder.appName = convertView.findViewById(R.id.tappName);
            viewHolder.lData = convertView.findViewById(R.id.lData);
            viewHolder.commonPath = convertView.findViewById(R.id.tcommonPath);
            viewHolder.dataPath = convertView.findViewById(R.id.tdataPath);
            viewHolder.selected = convertView.findViewById(R.id.tchecked);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sAppList.get(position).setSelected(isChecked);
                Shell ss = Shell.getCachedShell();
                Shell.Job jb = ss.newJob();
                List<String> sRes = new ArrayList<>();
                List<String> sErr = new ArrayList<>();
                if(!isChecked) {
                    int i=0;
                    do {
                        jb.add("am kill " + sAppList.get(position).getpName()).to(sRes,sErr).exec();
                        jb.add("umount " + sAppList.get(position).getDataPath()).to(sRes, sErr).exec();
                        jb = ss.newJob();
                        sRes = new ArrayList<>();
                        sErr = new ArrayList<>();
                        sErr.clear();
                        i++;
                        jb.add("ls -la " + sAppList.get(position).getCommonPath()).to(sRes, sErr).exec();
                        if(i==20) break;
                    }while (sErr.size() != 0);
                    jb.add("sed -i '/" + sAppList.get(position).getpName() + "/d' " + datacommon + selapplist).submit();
                    sAppList.get(position).setSelected(false);

                    /*Context context;
                    View v = new View(getContext());

                    ListView rlv = (ListView) v.findViewById(R.id.listapp);
                    sAppList.get(position).setSelected(false);
                    sAppList.get(position).setShared(false);

                    int x=0;
                    for (sApp a : sAppList)
                    {
                        if (a.getSelected()) x++;
                    }
                    SharedDataListPopulate();
                    TextView tTitle = v.findViewById(R.id.TitleSaveApp);
                    tTitle.setText(getText(R.string.shared_app_title) + " (" + x + ")" );

                    AppAdapter adapter = new AppAdapter(rlv.getContext(), R.layout.application_detail, sAppList, SharedDataList);
                    rlv.setAdapter(adapter);
                    rlv.destroyDrawingCache();
                    rlv.setVisibility(ListView.INVISIBLE);
                    rlv.setVisibility(ListView.VISIBLE);
                    adapter.notifyDataSetChanged();*/
                }
            }
        });


        viewHolder.icon.setImageDrawable(sapp.getIcon());
        viewHolder.appName.setText(sapp.getAppName());
        if (sapp.getShared()) viewHolder.lData.setImageTintList(ColorStateList.valueOf(Color.GREEN));
        else viewHolder.lData.setImageTintList(getContext().getColorStateList(R.color.invernomuto_300));
        viewHolder.commonPath.setText(sapp.getCommonPath());
        viewHolder.dataPath.setText(sapp.getDataPath());
        viewHolder.selected.setChecked(sapp.getSelected());
        return convertView;
    }
    
    private void SharedDataListPopulate() {
        Shell ss = Shell.getCachedShell();
        Shell.Job jb = ss.newJob();
        List<String> sRes = new ArrayList<>();
        List<String> sErr = new ArrayList<>();
        jb.add("cd " + datacommon + " && ls -d */").to(sRes, sErr).exec();
        SharedDataList = new ArrayList<>();
        SharedDataList.clear();
        SharedDataList.addAll(sRes);
    }
    private class ViewHolder {
        public ImageView icon;
        public TextView appName;
        public ImageView lData;
        public TextView commonPath;
        public TextView dataPath;
        public CheckBox selected;

    }

    private SparseBooleanArray convertToCheckedItems(Set<String> checkedItems) {
        SparseBooleanArray array = new SparseBooleanArray();
        for(String itemPositionStr : checkedItems) {
            int position = Integer.parseInt(itemPositionStr);
            array.put(position, true);
        }

        return array;
    }

    private Set<String> convertToStringSet(SparseBooleanArray checkedItems) {
        Set<String> result = new HashSet<>();
        for (int i = 0; i < checkedItems.size(); i++) {
            result.add(String.valueOf(checkedItems.keyAt(i)));
        }
        return result;
    }
}

class StableArrayAdapter<T> extends ArrayAdapter<T> {

    public StableArrayAdapter(Context context, int textViewResourceId, List<T> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

