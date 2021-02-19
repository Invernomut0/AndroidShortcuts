package com.invernomuto.DualBoot;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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

import com.topjohnwu.superuser.Shell;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class SharedApp extends AppCompatActivity {

    String datacommon = "/datacommon/SharedData/";
    String datamount = "datamount.conf";
    String selapplist = "selapplist.conf";
    Button btn;
    ImageView lvSave;
    //SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.guillotine_background_dark));
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.guillotine_background_dark));
        }
        //mExplosionField = ExplosionField.attach2Window(this);
        //sharedPreferences = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0);
        setContentView(R.layout.shared_app);
        btn = findViewById(R.id.bShareApp);
        ArrayList<sApp> appList = new ArrayList<sApp>();
        //ImageView im = (ImageView) findViewById(R.id.SaveApp)
        lvSave = findViewById(R.id.bSaveApp);
        //SharedPreferences pref = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0); // 0 - for private mode
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        List<ApplicationInfo> installedApps = new ArrayList<ApplicationInfo>();
        ListView listView = findViewById(R.id.listapp);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        List<String> list = new ArrayList<String>();
        list = Shell.su("cat " + datacommon + selapplist).exec().getOut();

        /*BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(datacommon + selapplist));
            String line = reader.readLine();
            while (line != null) {
                list.add(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        int i=0;
        for (ApplicationInfo app : apps) {
            //checks for flags; if flagged, check if updated system app
            if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                //Not interested in
            } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM)== 0) {
                String label = (String) pm.getApplicationLabel(app);
                Drawable icon = pm.getApplicationIcon(app);

                //Set<String> checkedItemsSource = sharedPreferences.getStringSet("checked_items", new HashSet<String>());
                //SparseBooleanArray checkedItems = convertToCheckedItems(checkedItemsSource);
                Boolean checked = false;

                for (int ix = 0; ix < list.size(); ix++) {
                    String p = list.get(ix);
                    if (p.contains(app.processName))
                    {
                        checked = true;
                    }
                }
                i++;
                if(!label.contains("DualBoot"))
                {
                    sApp sapp = new sApp(app.processName, label, app.dataDir, datacommon + app.processName, icon, checked);
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
        AppAdapter adapter = new AppAdapter(this, R.layout.application_detail, appList);
        listView.setAdapter(adapter);

        lvSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Shell.Result result = Shell.su("rm -f " + datacommon + datamount).exec();
                result = Shell.su("rm -f " + datacommon + selapplist).exec();
                Set<String> stringSet = new HashSet<>();
                Shell.su("mkdir " + datacommon).exec();
                Shell.su("chown 1023:1023 " + datacommon);
                for (int i=0; i<listView.getCount(); i++) {
                    sApp s = (sApp) listView.getItemAtPosition(i);
                    if(s.Selected)
                    {
                        String val = s.pName;
                        stringSet.add(val);
                        doTheMagic(s);
                    }
                }
                /*sharedPreferences.edit()
                        .putStringSet("checked_items", stringSet)
                        .apply();
                */
                Toast.makeText(getApplicationContext(), getString(R.string.application_list_saved), Toast.LENGTH_SHORT).show();

            }
        });
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
        result = Shell.su("test -d "+ s.commonPath +" && echo OK || echo KO").exec();
        String res = result.getOut().toString();
        if (res.contains("KO"))
        {
            result = Shell.su("cp -r --preserve=all " + s.dataPath + " " + s.commonPath).exec();
            //result = Shell.su("restorecon -R " +s.dataPath + " " + s.commonPath).exec();
            //result = Shell.su("am kill " + s.pName).exec();
            //result = Shell.su("mount -o bind " + s.commonPath + " " + s.dataPath).exec();
        }
        result = Shell.su("echo " + s.commonPath + " " + s.dataPath + " >> " + datacommon + datamount).exec();
        result = Shell.su("echo " + s.pName + " >> " + datacommon + selapplist).exec();

    }
    public void onClickExit(View view) {
        try {
            btn.setEnabled(true);
        }
        catch (Exception e){}
        SharedApp.this.finish();
    }
}

class AppAdapter extends StableArrayAdapter<sApp> {
    List<sApp> sAppList;

    public AppAdapter(Context context, int textViewResourceId,
                                 List<sApp> objects) {
        super(context, textViewResourceId, objects);
        sAppList = objects;
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
            viewHolder.dataPath = convertView.findViewById(R.id.tdataPath);
            viewHolder.commonPath = convertView.findViewById(R.id.tcommonPath);
            viewHolder.selected = convertView.findViewById(R.id.tchecked);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sAppList.get(position).setSelected(isChecked);
            }
        });


        viewHolder.icon.setImageDrawable(sapp.icon);
        viewHolder.appName.setText(sapp.getAppName());
        viewHolder.dataPath.setText(sapp.getDataPath());
        viewHolder.commonPath.setText(sapp.getCommonPath());
        viewHolder.selected.setChecked(sapp.Selected);
        return convertView;
    }

    private class ViewHolder {
        public ImageView icon;
        public TextView appName;
        public TextView dataPath;
        public TextView commonPath;
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

