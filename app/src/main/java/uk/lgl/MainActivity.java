package uk.lgl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

import uk.lgl.modmenu.FloatingModMenuService;

import static uk.lgl.modmenu.Preferences.context;

public class MainActivity extends Activity {

    public String GameActivity = "com.unity3d.player.UnityPlayerActivity";

    public static native void Toast(Context context);

    //Load lib
    static {
        // When you change the lib name, change also on Android.mk file
        // Both must have same name
        System.loadLibrary("MyLibName");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        //To launch mod menu. If you don't use mod menu, remove or comment out Start(this);
        Start(this);

        //Use getApplicationContext() to fix dark background of Toast message
        CallToast(getApplicationContext());

        //To launch game activity
        try {
            //Start service
            MainActivity.this.startActivity(new Intent(MainActivity.this, Class.forName(MainActivity.this.GameActivity)));
        } catch (ClassNotFoundException e) {
            //Uncomment this if you are following METHOD 2 of CHANGING FILES
            //Toast.makeText(MainActivity.this, "Error. Game's main activity does not exist", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }

    }

    //Load mod menu
    public static void Start(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Toast.makeText(context.getApplicationContext(), "Overlay permission is required in order to show mod menu", Toast.LENGTH_LONG).show();
            Toast.makeText(context.getApplicationContext(), "Restart the game after you allow permission", Toast.LENGTH_LONG).show();
            context.startActivity(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION",
                    Uri.parse("package:" + context.getPackageName())));
        } else {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    context.startService(new Intent(context, FloatingModMenuService.class));
                }
            }, 500);
        }
    }

    public static void CallToast(final Context context) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Toast(context);
            }
        }, 500);
    }
}
