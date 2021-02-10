package uk.lgl.modmenu;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

//TODO
//Write up android logcat on readme

public class Preferences {
    private static SharedPreferences.Editor editor;
    public static Context context;
    public static boolean savePref = false, animation = false, expanded = false;

    public static native void Changes(Context context, int feature, int value, boolean bool, String str);

    public static void changeFeatureInt(String feature, int featureNum, int value) {
        Changes(context, featureNum, value, false, feature);
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
        editor.putInt(String.valueOf(featureNum), value).apply();
    }

    public static void changeFeatureBoolean(String feature, int featureNum, boolean value) {
        if (featureNum == -1)
            animation = value;
        if (featureNum == -2)
            expanded = value;
        if (featureNum == -3)
            savePref = value;
        Changes(context, featureNum, 0, value, feature);
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
        editor.putBoolean(String.valueOf(featureNum), value).apply();
    }

    //TODO: changeFeatureString

    public static int loadPrefInt(String featureName, int featureNum) {
        try{
            if (savePref) {
                SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
                int i = preferences.getInt(String.valueOf(featureNum), 0);
                Changes(context, featureNum, i, false, featureName);
                return i;
            }
        }
        catch (ClassCastException e){
            Log.e(FloatingModMenuService.TAG, e.getMessage());
        }
        return 0;
    }

    public static boolean loadPrefBoolean(String featureName, int featureNum) {
        try{
            SharedPreferences preferences = getDefaultSharedPreferences(context);
            if (featureNum == -3) {
                savePref = preferences.getBoolean(String.valueOf(featureNum), false);
            }
            if (savePref || featureNum <= 0) {
                //if (featureNum == 1001 && !preferences.contains("1001"))
                //    return true;
                boolean bool = preferences.getBoolean(String.valueOf(featureNum), false);
                Changes(context, featureNum, 0, bool, featureName);
                return bool;
            }
        }
        catch (ClassCastException e){
            Log.e(FloatingModMenuService.TAG, e.getMessage());
        }
        return false;
    }
}