package com.invernomuto.DualBoot;


import android.graphics.drawable.Drawable;



public class sApp {

    String pName="";
    String AppName="";
    String dataPath="";
    String commonPath="";
    Drawable icon=null;
    Boolean Selected=false;

    public sApp(String pName, String AppName, String dataPath, String commonPath, Drawable icon, Boolean Selected)
    {
        this.pName = pName;
        this.AppName=AppName;
        this.commonPath=commonPath;
        this.dataPath=dataPath;
        this.icon=icon;
        this.Selected=Selected;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public String getAppName() {
        return AppName;
    }

    public void setAppName(String appName) {
        AppName = appName;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getCommonPath() {
        return commonPath;
    }

    public void setCommonPath(String commonPath) {
        this.commonPath = commonPath;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Boolean getSelected() {
        return Selected;
    }

    public void setSelected(Boolean selected) {
        Selected = selected;
    }
}


