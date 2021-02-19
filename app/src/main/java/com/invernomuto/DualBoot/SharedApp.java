package com.invernomuto.DualBoot;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SharedApp extends AppCompatActivity {

    String datacommon = "/datacommon/SharedData/";
    String datamount = "datamount.conf";
    Button btn;
    ImageView lvSave;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mExplosionField = ExplosionField.attach2Window(this);
        sharedPreferences = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0);
        setContentView(R.layout.shared_app);
        btn = (Button) findViewById(R.id.bShareApp);
        ArrayList<sApp> appList = new ArrayList<sApp>();
        //ImageView im = (ImageView) findViewById(R.id.SaveApp)
        lvSave = (ImageView) findViewById(R.id.bSaveApp);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("DualBoot_prefs", 0); // 0 - for private mode
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        List<ApplicationInfo> installedApps = new ArrayList<ApplicationInfo>();
        ListView listView = (ListView) findViewById(R.id.listapp);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        int i=0;
        for (ApplicationInfo app : apps) {
            //checks for flags; if flagged, check if updated system app
            if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                //Not interested in
            } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM)== 0) {
                String label = (String) pm.getApplicationLabel(app);
                Drawable icon = pm.getApplicationIcon(app);
                Set<String> checkedItemsSource = sharedPreferences.getStringSet("checked_items", new HashSet<String>());
                //SparseBooleanArray checkedItems = convertToCheckedItems(checkedItemsSource);
                Boolean checked = false;
                List<String> list = new ArrayList<String>(checkedItemsSource);

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
        //create an ArrayAdaptar from the String Array
        AppAdapter adapter = new AppAdapter(this, R.layout.application_detail, appList);
        listView.setAdapter(adapter);

        lvSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Shell.Result result = Shell.su("rm -f " + datacommon + datamount).exec();
                Set<String> stringSet = new HashSet<>();
                Shell.su("mkdir " + datacommon).exec();
                for (int i=0; i<listView.getCount(); i++) {
                    sApp s = (sApp) listView.getItemAtPosition(i);
                    if(s.Selected)
                    {
                        String val = s.pName;
                        stringSet.add(val);
                        doTheMagic(s);
                    }
                }
                //SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                //Set<String> stringSet = convertToStringSet(checkedItems);
                sharedPreferences.edit()
                        .putStringSet("checked_items", stringSet)
                        .apply();

                Toast.makeText(getApplicationContext(), getString(R.string.application_list_saved), Toast.LENGTH_SHORT).show();

            }
        });
 /*       Map<String, ?> allEntries = pref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
        }*/
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


    }
    public void onClickExit(View view) {
        try {
            btn.setEnabled(true);
        }
        catch (Exception e){}
        SharedApp.this.finish();
    }

    /*private SparseBooleanArray convertToCheckedItems(Set<String> checkedItems) {
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
     */

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
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.appicons);
            viewHolder.appName = (TextView) convertView.findViewById(R.id.tappName);
            viewHolder.dataPath = (TextView) convertView.findViewById(R.id.tdataPath);
            viewHolder.commonPath = (TextView) convertView.findViewById(R.id.tcommonPath);
            viewHolder.selected = (CheckBox) convertView.findViewById(R.id.tchecked);
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

