/*  Diese App stellt die Beladung von BOS Fahrzeugen in digitaler Form dar.
    Copyright (C) 2017  David Schlossarczyk

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    For the full license visit https://www.gnu.org/licenses/gpl-3.0.*/

package dresden.de.digitaleTaschenkarteBeladung.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import dresden.de.digitaleTaschenkarteBeladung.BuildConfig;
import dresden.de.digitaleTaschenkarteBeladung.MainActivity;

/**
 * Der PreferencesManager ist für das Laden der Einstellungen aus den PREFS zuständig. Er gewährleistet dabei die Kompatibilität zu alten App-Versionen
 */
public class PreferencesManager {
    private MainActivity parent;
    private SharedPreferences.Editor editor;

    private int dbVersion;
    private String url;

    //Diese Variable gibt an ob ein veralteter PREF Satz gefunden wurde. Beim Speichern müssen dann entsprechende Maßnahmen ergriffen werden
    private boolean outdatedPref;


    static final String PREFS_NAME="dresden.de.digitaleTaschenkarteBeladung";
    private static final String PREFS_URL="url";
    private static final String PREFS_DBVERSION="dbversion";
    private static final String PREFS_SORT="sort";
    private static final String PREFS_VERSION="version";


    public PreferencesManager(Activity parent) {
        this.parent = (MainActivity) parent;
        outdatedPref = false;
    }

    //=================================================================
    //========================HAUPTMETHODEN==========================
    //=================================================================

    public void load() {

        dbVersion = parent.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(PREFS_VERSION,11);

        switch (dbVersion) {
            case 11:
                //Version 0.4.3
                loadv11();
                break;
            case 12:
                //Version 0.5
                loadv11();
                break;
        }


    }

    public void save() {

        editor = parent.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE).edit();

        if (outdatedPref) {
            //Alte Prefs komplett löschen
            delete();
        }

        editor.putString(PREFS_URL, url);
        editor.putInt(PREFS_DBVERSION, dbVersion);
        editor.putInt(PREFS_VERSION, BuildConfig.VERSION_CODE);

        switch (parent.liveSort.getValue()) {
            case PRESET:
                editor.putInt(PREFS_SORT,0);
                break;
            case AZ:
                editor.putInt(PREFS_SORT,1);
                break;
            case ZA:
                editor.putInt(PREFS_SORT,2);
                break;
        }

        editor.apply();

    }

    public void delete() {
        editor.clear();
        editor.apply();
    }

    public void reset() {
        delete();
        dbVersion = -1;
        url = "NO_URL_FOUND";
        parent.liveNetDBVersion.setValue(0);
        parent.dbState = Util.DbState.CLEAN;
    }

    //=================================================================
    //========================GETTER & SETTER==========================
    //=================================================================

    public int getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    //=================================================================
    //======================Kompatibilitätsmethoden====================
    //=================================================================

    private void loadv11() {

        SharedPreferences preferences = parent.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        dbVersion = preferences.getInt(PREFS_DBVERSION,-1);
        url = preferences.getString(PREFS_URL,"NO_URL_FOUND");

        int pref = preferences.getInt(PREFS_SORT, 0);
        switch (pref) {
            default:
                parent.liveSort.setValue(Util.Sort.PRESET);
                break;
            case 1:
                parent.liveSort.setValue(Util.Sort.AZ);
                break;
            case 2:
                parent.liveSort.setValue(Util.Sort.ZA);
                break;
        }

    }


}
