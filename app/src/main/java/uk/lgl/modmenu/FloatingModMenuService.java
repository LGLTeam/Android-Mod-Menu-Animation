//TODO
//Text input string

package uk.lgl.modmenu;

import android.animation.ArgbEvaluator;
import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import uk.lgl.animation.AnimationSetupCallback;
import uk.lgl.animation.Titanic;
import uk.lgl.animation.TitanicButton;
import uk.lgl.animation.TitanicTextView;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;

public class FloatingModMenuService extends Service {
    //********** Here you can easly change the menu appearance **********//
    public static final String TAG = "Mod_Menu"; //Tag for logcat
    final int TEXT_COLOR = Color.parseColor("#82CAFD");
    final int TEXT_COLOR_2 = Color.parseColor("#FFFFFF");
    final int BTN_COLOR = Color.parseColor("#1C262D");
    final int MENU_BG_COLOR = Color.parseColor("#DD1C2A35"); //#AARRGGBB
    final int MENU_FEATURE_BG_COLOR = Color.parseColor("#FF171E24"); //#AARRGGBB
    final int MENU_WIDTH = 290;
    final int MENU_HEIGHT = 210;
    final float MENU_CORNER = 20f;
    final int ICON_SIZE = 50; //Change both width and height of image
    final float ICON_ALPHA = 0.7f; //Transparent
    //********************************************************************//

    //Some fields
    GradientDrawable gdMenuBody, gdAnimation = new GradientDrawable();
    RelativeLayout mCollapsed, mRootContainer;
    LinearLayout mExpanded, patches, mSettings;
    LinearLayout.LayoutParams scrlLLExpanded, scrlLL;
    WindowManager mWindowManager;
    WindowManager.LayoutParams params;
    ImageView startimage;
    FrameLayout rootFrame;
    AlertDialog alert;
    EditText edittextvalue;
    ScrollView scrollView;

    //For alert dialog
    TextView inputFieldTextView;
    String inputFieldFeatureName;
    int inputFieldFeatureNum;
    EditTextValue inputFieldTxtValue;

    boolean stopChecking, settingsOpen;

    //initialize methods from the native library
    native String Title();

    native String Heading();

    native String Icon();

    native String IconWebViewData();

    native String[] getFeatureList();

    native String[] settingsList();

    native boolean isGameLibLoaded();

    //When this Class is called the code in this function will be executed
    @Override
    public void onCreate() {
        super.onCreate();
        Preferences.context = this;

        //Create the menu
        initFloating();
        initAlertDiag();

        //Start the Gradient Animation
        startAnimation();

        //Create a handler for this Class
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            public void run() {
                Thread();
                handler.postDelayed(this, 1000);
            }
        });
    }

    //Here we write the code for our Menu
    // Reference: https://www.androidhive.info/2016/11/android-floating-widget-like-facebook-chat-head/
    private void initFloating() {
        rootFrame = new FrameLayout(this); // Global markup
        rootFrame.setOnTouchListener(onTouchListener());
        mRootContainer = new RelativeLayout(this); // Markup on which two markups of the icon and the menu itself will be placed
        mCollapsed = new RelativeLayout(this); // Markup of the icon (when the menu is minimized)
        mCollapsed.setVisibility(View.VISIBLE);
        mCollapsed.setAlpha(ICON_ALPHA);

        //********** The box of the mod menu **********
        mExpanded = new LinearLayout(this); // Menu markup (when the menu is expanded)
        mExpanded.setVisibility(View.GONE);
        mExpanded.setBackgroundColor(MENU_BG_COLOR);
        mExpanded.setGravity(Gravity.CENTER);
        mExpanded.setOrientation(LinearLayout.VERTICAL);
        // mExpanded.setPadding(1, 1, 1, 1);
        mExpanded.setLayoutParams(new LinearLayout.LayoutParams(dp(MENU_WIDTH), WRAP_CONTENT));
        gdMenuBody = new GradientDrawable();
        gdMenuBody.setCornerRadius(MENU_CORNER); //Set corner
        gdMenuBody.setColor(MENU_BG_COLOR); //Set background color
        //gradientdrawable.setStroke(1, Color.parseColor("#32cb00")); //Set border
        mExpanded.setBackground(Preferences.loadPrefBoolean("Color animation", -1) ? gdAnimation : gdMenuBody); //Apply aninmation to it

        //********** The icon to open mod menu **********
        startimage = new ImageView(this);
        startimage.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        int applyDimension = (int) TypedValue.applyDimension(1, ICON_SIZE, getResources().getDisplayMetrics()); //Icon size
        startimage.getLayoutParams().height = applyDimension;
        startimage.getLayoutParams().width = applyDimension;
        startimage.requestLayout();
        startimage.setScaleType(ImageView.ScaleType.FIT_XY);
        byte[] decode = Base64.decode(Icon(), 0);
        startimage.setImageBitmap(BitmapFactory.decodeByteArray(decode, 0, decode.length));
        ((ViewGroup.MarginLayoutParams) startimage.getLayoutParams()).topMargin = convertDipToPixels(10);
        //Initialize event handlers for buttons, etc.
        startimage.setOnTouchListener(onTouchListener());
        startimage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCollapsed.setVisibility(View.GONE);
                mExpanded.setVisibility(View.VISIBLE);
            }
        });

        //********** The icon in Webview to open mod menu **********
        WebView wView = new WebView(this); //Icon size width=\"50\" height=\"50\"
        wView.loadData("<html>" +
                "<head></head>" +
                "<body style=\"margin: 0; padding: 0\">" +
                "<img src=\"" + IconWebViewData() + "\" width=\"" + ICON_SIZE + "\" height=\"" + ICON_SIZE + "\"" +
                "</body>" +
                "</html>", "text/html", "utf-8");
        wView.setBackgroundColor(0x00000000); //Transparent
        wView.setAlpha(ICON_ALPHA);
        wView.getSettings().setAppCachePath("/data/data/" + getPackageName() + "/cache");
        wView.getSettings().setAppCacheEnabled(true);
        wView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        wView.setOnTouchListener(onTouchListener());
        wView.requestLayout();

        //********** Settings icon **********
        TextView settings = new TextView(this); //Android 5 can't show ⚙, instead show other icon instead
        settings.setText(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ? "⚙" : "\uD83D\uDD27");
        settings.setTextColor(TEXT_COLOR);
        settings.setTypeface(Typeface.DEFAULT_BOLD);
        settings.setTextSize(20.0f);
        RelativeLayout.LayoutParams rlsettings = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        rlsettings.addRule(ALIGN_PARENT_RIGHT);
        settings.setLayoutParams(rlsettings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    localChanges(-6, false);
                } catch (IllegalStateException e) {
                }
            }
        });

        //********** Settings **********
        mSettings = new LinearLayout(this);
        mSettings.setOrientation(LinearLayout.VERTICAL);
        featureList(settingsList(), mSettings);

        //********** Title text **********
        RelativeLayout titleText = new RelativeLayout(this);
        titleText.setPadding(10, 5, 10, 5);
        titleText.setVerticalGravity(16);

        TitanicTextView title = new TitanicTextView(this);
        title.setText(Html.fromHtml(Title()));
        title.setTextColor(TEXT_COLOR);
        title.setTextSize(18.0f);
        title.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
        title.setLayoutParams(rl);
        new Titanic().start(title);

        //********** Heading text **********
        TitanicTextView heading = new TitanicTextView(this);
        heading.setText(Html.fromHtml(Heading()));
        heading.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        heading.setMarqueeRepeatLimit(-1);
        heading.setSingleLine(true);
        heading.setSelected(true);
        heading.setTextColor(TEXT_COLOR);
        heading.setTextSize(10.0f);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(0, 0, 0, 5);
        new Titanic().start(heading);

        //********** Mod menu feature list **********
        scrollView = new ScrollView(this);
        //Auto size. To set size manually, change the width and height example 500, 500
        scrlLL = new LinearLayout.LayoutParams(MATCH_PARENT, dp(MENU_HEIGHT));
        scrlLLExpanded = new LinearLayout.LayoutParams(mExpanded.getLayoutParams());
        scrlLLExpanded.weight = 1.0f;
        scrollView.setLayoutParams(Preferences.loadPrefBoolean("Auto size vertically", -2) ? scrlLLExpanded : scrlLL);
        scrollView.setBackgroundColor(MENU_FEATURE_BG_COLOR);

        patches = new LinearLayout(this);
        patches.setOrientation(LinearLayout.VERTICAL);
        patches.setBackgroundColor(MENU_FEATURE_BG_COLOR);

        //**********  Hide/Kill button **********
        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setPadding(10, 3, 10, 3);
        relativeLayout.setVerticalGravity(Gravity.CENTER);

        TitanicButton hideBtn = new TitanicButton(this);
        hideBtn.setBackgroundColor(Color.TRANSPARENT);
        hideBtn.setText("HIDE/KILL (Hold)");
        hideBtn.setTextColor(TEXT_COLOR);
        // hideBtn.setPadding(0, 17, 0, 17);
        //hideBtn.setTypeface(Typeface.DEFAULT_BOLD);
        hideBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCollapsed.setVisibility(View.VISIBLE);
                mCollapsed.setAlpha(0);
                mExpanded.setVisibility(View.GONE);
                Toast.makeText(view.getContext(), "Icon hidden. Remember the hidden icon position", Toast.LENGTH_LONG).show();
            }
        });
        hideBtn.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                Toast.makeText(view.getContext(), "Menu service killed", Toast.LENGTH_LONG).show();
                FloatingModMenuService.this.stopSelf();
                return false;
            }
        });
        new Titanic().start(hideBtn);

        //********** Close button **********
        Button closeBtn = new Button(this);
        closeBtn.setBackgroundColor(Color.TRANSPARENT);
        closeBtn.setText("MINIMIZE");
        closeBtn.setTextColor(TEXT_COLOR);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCollapsed.setVisibility(View.VISIBLE);
                mCollapsed.setAlpha(ICON_ALPHA);
                mExpanded.setVisibility(View.GONE);
            }
        });
        //new Titanic().start(closeBtn);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        layoutParams.addRule(ALIGN_PARENT_RIGHT);
        closeBtn.setLayoutParams(layoutParams);

        //********** Params **********
        //Variable to check later if the phone supports Draw over other apps permission
        int iparams = Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ? 2038 : 2002;
        params = new WindowManager.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, iparams, 8, -3);
        params.gravity = 51;
        params.x = 0;
        params.y = 100;

        //********** Adding view components **********
        rootFrame.addView(mRootContainer);
        mRootContainer.addView(mCollapsed);
        mRootContainer.addView(mExpanded);
        if (IconWebViewData() != null) {
            mCollapsed.addView(wView);
        } else {
            mCollapsed.addView(startimage);
        }
        titleText.addView(title);
        titleText.addView(settings);
        mExpanded.addView(titleText);
        mExpanded.addView(heading);
        scrollView.addView(patches);
        mExpanded.addView(scrollView);
        relativeLayout.addView(hideBtn);
        relativeLayout.addView(closeBtn);
        mExpanded.addView(relativeLayout);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(rootFrame, params);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            boolean viewLoaded = false;

            @Override
            public void run() {
                //If the save preferences is enabled, it will check if game lib is loaded before starting menu
                //Comment the if-else code out except startService if you want to run the app and test preferences
                if (Preferences.savePref && !isGameLibLoaded() && !stopChecking) {
                    if (!viewLoaded) {
                        patches.addView(Category("Save preferences was been enabled. Waiting for game lib to be loaded...\n\nForce load menu may not apply mods instantly. You would need to reactivate them again"));
                        patches.addView(Button(-100, "Force load menu"));
                        viewLoaded = true;
                    }
                    handler.postDelayed(this, 600);
                } else {
                    patches.removeAllViews();
                    featureList(getFeatureList(), patches);
                }
            }
        }, 500);
    }

    private void featureList(String[] listFT, LinearLayout linearLayout) {
        for (int i = 0; i < listFT.length; i++) {
            String str = listFT[i];
            String[] strSplit = str.split("_");
            if (strSplit[1].equals("Toggle")) {
                linearLayout.addView(Switch(Integer.parseInt(strSplit[0]), strSplit[2]));
            } else if (strSplit[1].equals("SeekBar")) {
                linearLayout.addView(SeekBar(Integer.parseInt(strSplit[0]), strSplit[2], Integer.parseInt(strSplit[3]), Integer.parseInt(strSplit[4])));
            } else if (strSplit[1].equals("Button")) {
                linearLayout.addView(Button(Integer.parseInt(strSplit[0]), strSplit[2]));
            } else if (strSplit[1].equals("ButtonLink")) {
                linearLayout.addView(ButtonLink(strSplit[2], strSplit[3]));
            } else if (strSplit[1].equals("ButtonOnOff")) {
                linearLayout.addView(ButtonOnOff(Integer.parseInt(strSplit[0]), strSplit[2]));
            } else if (strSplit[1].equals("Spinner")) {
                linearLayout.addView(RichTextView(strSplit[2]));
                linearLayout.addView(Spinner(Integer.parseInt(strSplit[0]), strSplit[2], strSplit[3]));
            } else if (strSplit[1].equals("InputValue")) {
                linearLayout.addView(TextField(Integer.parseInt(strSplit[0]), strSplit[2]));
            } else if (strSplit[1].equals("CheckBox")) {
                linearLayout.addView(CheckBox(Integer.parseInt(strSplit[0]), strSplit[2]));
            } else if (strSplit[1].equals("Category")) {
                linearLayout.addView(Category(strSplit[2]));
            } else if (strSplit[1].equals("RichTextView")) {
                linearLayout.addView(RichTextView(strSplit[2]));
            } else if (strSplit[1].equals("RichWebView")) {
                linearLayout.addView(RichWebView(strSplit[2]));
            } else if (strSplit[1].equals("RadioButton")) {
                linearLayout.addView(RadioButton(Integer.parseInt(strSplit[0]), strSplit[2], strSplit[3]));
            }
        }
    }

    private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {
            final View collapsedView = mCollapsed;
            final View expandedView = mExpanded;
            private float initialTouchX, initialTouchY;
            private int initialX, initialY;

            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int rawX = (int) (motionEvent.getRawX() - initialTouchX);
                        int rawY = (int) (motionEvent.getRawY() - initialTouchY);

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (rawX < 10 && rawY < 10 && isViewCollapsed()) {
                            //When user clicks on the image view of the collapsed layout,
                            //visibility of the collapsed layout will be changed to "View.GONE"
                            //and expanded view will become visible.
                            try {
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            } catch (NullPointerException e) {

                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + ((int) (motionEvent.getRawX() - initialTouchX));
                        params.y = initialY + ((int) (motionEvent.getRawY() - initialTouchY));
                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(rootFrame, params);
                        return true;
                    default:
                        return false;
                }
            }
        };
    }

    //Dialog for changing value
    private void initAlertDiag() {
        //LinearLayout
        LinearLayout linearLayout1 = new LinearLayout(this);
        linearLayout1.setPadding(5, 5, 5, 5);
        linearLayout1.setOrientation(LinearLayout.VERTICAL);
        linearLayout1.setBackgroundColor(MENU_FEATURE_BG_COLOR);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //FrameLayout
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(linearLayout);

        //TextView
        final TextView textView = new TextView(this);
        textView.setText("Tap OK to apply changes. Tap outside to cancel");
        textView.setTextColor(TEXT_COLOR_2);

        //Edit text
        edittextvalue = new EditText(this);
        edittextvalue.setMaxLines(1);
        edittextvalue.setWidth(convertDipToPixels(300));
        edittextvalue.setTextColor(TEXT_COLOR_2);
        edittextvalue.setHintTextColor(Color.parseColor("#434d52"));
        edittextvalue.setInputType(InputType.TYPE_CLASS_NUMBER);
        edittextvalue.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));

        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(10);
        edittextvalue.setFilters(FilterArray);

        //Button
        Button button = new Button(this);
        button.setBackgroundColor(BTN_COLOR);
        button.setTextColor(TEXT_COLOR_2);
        button.setText("OK");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputFieldTxtValue.setValue(Integer.parseInt(edittextvalue.getText().toString()));
                inputFieldTextView.setText(Html.fromHtml("<font face='roboto'>" + inputFieldFeatureName + ": <font color='#41c300'>" + edittextvalue.getText().toString() + "</font></font>"));
                alert.dismiss();
                Preferences.changeFeatureInt(inputFieldFeatureName, inputFieldFeatureNum, Integer.parseInt(edittextvalue.getText().toString()));
            }
        });

        alert = new AlertDialog.Builder(this, 2).create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(alert.getWindow()).setType(Build.VERSION.SDK_INT >= 26 ? 2038 : 2002);
        }
        linearLayout1.addView(textView);
        linearLayout1.addView(edittextvalue);
        linearLayout1.addView(button);
        alert.setView(linearLayout1);
    }

    private View Switch(final int featureNum, final String featureName) {
        final Switch switchR = new Switch(this);
        ColorStateList buttonStates = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled},
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        Color.BLUE,
                        Color.GREEN,
                        Color.RED
                }
        );
        //Set colors of the switch. Comment out if you don't like it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switchR.getThumbDrawable().setTintList(buttonStates);
            switchR.getTrackDrawable().setTintList(buttonStates);
        }

        switchR.setText(featureName);
        switchR.setTextColor(TEXT_COLOR_2);
        switchR.setPadding(10, 5, 0, 5);
        switchR.setChecked(Preferences.loadPrefBoolean(featureName, featureNum));
        switchR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                localChanges(featureNum, isChecked);
                Preferences.changeFeatureBoolean(featureName, featureNum, isChecked);
            }
        });
        return switchR;
    }

    private View SeekBar(final int featureNum, final String featureName, final int min, int max) {
        int loadedProg = Preferences.loadPrefInt(featureName, featureNum);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setPadding(10, 5, 0, 5);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);

        final TextView textView = new TextView(this);
        textView.setText(Html.fromHtml("<font face='roboto'>" + featureName + ": <font color='#41c300'>" + ((loadedProg == 0) ? min : loadedProg) + "</font>"));
        textView.setTextColor(TEXT_COLOR_2);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setPadding(25, 10, 35, 10);
        seekBar.setMax(max);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            seekBar.setMin(min); //setMin for Oreo and above
        seekBar.setProgress((loadedProg == 0) ? min : loadedProg);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                //if progress is greater than minimum, don't go below. Else, set progress
                seekBar.setProgress(i < min ? min : i);
                Preferences.changeFeatureInt(featureName, featureNum, i < min ? min : i);
                textView.setText(Html.fromHtml("<font face='roboto'>" + featureName + ": <font color='#41c300'>" + (i < min ? min : i) + "</font>"));
            }
        });
        linearLayout.addView(textView);
        linearLayout.addView(seekBar);

        return linearLayout;
    }

    private View Button(final int featureNum, final String featureName) {
        final Button button = new Button(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.setMargins(7, 5, 7, 5);
        button.setLayoutParams(layoutParams);
        // button.setTextSize(13.0f);
        button.setTextColor(TEXT_COLOR_2);
        button.setGravity(Gravity.CENTER);
        button.setAllCaps(false); //Disable caps to support html
        button.setText(Html.fromHtml(featureName));
        button.setBackgroundColor(BTN_COLOR);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                localChanges(featureNum, false);
                Preferences.changeFeatureInt(featureName, featureNum, 0);
            }
        });

        return button;
    }

    private View ButtonLink(final String featureName, final String url) {
        final Button button = new Button(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.setMargins(7, 5, 7, 5);
        button.setLayoutParams(layoutParams);
        button.setAllCaps(false); //Disable caps to support html
        button.setTextColor(TEXT_COLOR_2);
        button.setGravity(Gravity.CENTER);
        button.setText(featureName);
        button.setBackgroundColor(BTN_COLOR);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        return button;
    }

    private View ButtonOnOff(final int featureNum, String featureName) {
        final Button button = new Button(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.setMargins(7, 5, 7, 5);
        button.setLayoutParams(layoutParams);
        button.setTextColor(TEXT_COLOR_2);
        button.setGravity(Gravity.CENTER);
        button.setAllCaps(false); //Disable caps to support html

        final String finalFeatureName = featureName.replace("OnOff_", "");
        boolean isOn = Preferences.loadPrefBoolean(featureName, featureNum);
        if (isOn) {
            button.setText(finalFeatureName + ": ON");
            button.setBackgroundColor(Color.parseColor("#003300"));
            isOn = false;
        } else {
            button.setText(finalFeatureName + ": OFF");
            button.setBackgroundColor(Color.parseColor("#7f0000"));
            isOn = true;
        }
        final boolean finalIsOn = isOn;
        button.setOnClickListener(new View.OnClickListener() {
            boolean isOn = finalIsOn;

            public void onClick(View v) {
                localChanges(featureNum, isOn);
                Preferences.changeFeatureBoolean(finalFeatureName, featureNum, isOn);
                //Log.d(TAG, finalFeatureName + " " + featureNum + " " + isActive2);
                if (isOn) {
                    button.setText(finalFeatureName + ": ON");
                    button.setBackgroundColor(Color.parseColor("#003300"));
                    isOn = false;
                } else {
                    button.setText(finalFeatureName + ": OFF");
                    button.setBackgroundColor(Color.parseColor("#7f0000"));
                    isOn = true;
                }
            }
        });

        return button;
    }

    private View Spinner(final int featureNum, final String featureName, final String list) {
        final List<String> lists = new LinkedList<>(Arrays.asList(list.split(",")));

        // Create another LinearLayout as a workaround to use it as a background
        // and to keep the 'down' arrow symbol
        // If spinner had the setBackgroundColor set, there would be no arrow symbol
        LinearLayout linearLayout2 = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams2.setMargins(10, 2, 10, 5);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setBackgroundColor(BTN_COLOR);
        linearLayout2.setLayoutParams(layoutParams2);

        final Spinner spinner = new Spinner(this, Spinner.MODE_DROPDOWN);
        spinner.setPadding(5, 10, 5, 8);
        spinner.setLayoutParams(layoutParams2);
        spinner.getBackground().setColorFilter(1, PorterDuff.Mode.SRC_ATOP); //trick to show white down arrow color
        //Creating the ArrayAdapter instance having the list
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, lists);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner'
        spinner.setAdapter(aa);
        spinner.setSelection(Preferences.loadPrefInt(featureName, featureNum));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Preferences.changeFeatureInt(spinner.getSelectedItem().toString(), featureNum, position);
                ((TextView) parentView.getChildAt(0)).setTextColor(TEXT_COLOR_2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        linearLayout2.addView(spinner);
        return linearLayout2;
    }

    private View TextField(final int feature, final String featureName) {
        RelativeLayout relativeLayout2 = new RelativeLayout(this);
        relativeLayout2.setPadding(10, 5, 10, 5);
        relativeLayout2.setVerticalGravity(16);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        layoutParams.topMargin = 15;

        final TextView textView = new TextView(this);
        int num = Preferences.loadPrefInt(featureName, feature);
        textView.setText(Html.fromHtml("<font face='roboto'>" + featureName + ": <font color='#41c300'>" + num + "</font></font>"));
        textView.setTextColor(TEXT_COLOR_2);
        textView.setLayoutParams(layoutParams);

        final EditTextValue edittextval = new EditTextValue();

        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        layoutParams2.addRule(ALIGN_PARENT_RIGHT);

        Button button2 = new Button(this);
        button2.setLayoutParams(layoutParams2);
        button2.setBackgroundColor(BTN_COLOR);
        button2.setText("SET");
        button2.setTextColor(TEXT_COLOR_2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.show();
                inputFieldTextView = textView;
                inputFieldFeatureNum = feature;
                inputFieldFeatureName = featureName;
                inputFieldTxtValue = edittextval;
                edittextvalue.setText(String.valueOf(edittextval.getValue()));
            }
        });

        relativeLayout2.addView(textView);
        relativeLayout2.addView(button2);
        return relativeLayout2;
    }

    private View CheckBox(final int featureNum, final String featureName) {
        final CheckBox checkBox = new CheckBox(this);
        checkBox.setText(featureName);
        checkBox.setTextColor(TEXT_COLOR_2);
        checkBox.setChecked(Preferences.loadPrefBoolean(featureName, featureNum));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkBox.isChecked()) {
                    Preferences.changeFeatureBoolean(featureName, featureNum, isChecked);
                } else {
                    Preferences.changeFeatureBoolean(featureName, featureNum, isChecked);
                }
            }
        });
        return checkBox;
    }

    private View RadioButton(final int featureNum, String featureName, final String list) {
        //Credit: LoraZalora
        final List<String> lists = new LinkedList<>(Arrays.asList(list.split(",")));

        final TextView textView = new TextView(this);
        textView.setText(featureName + ":");
        textView.setTextColor(TEXT_COLOR_2);

        final RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setPadding(10, 5, 10, 5);
        radioGroup.setOrientation(LinearLayout.VERTICAL);
        radioGroup.addView(textView);

        int e = 1;
        int[][] iArr = new int[e][];
        int[] iArr2 = new int[e];
        iArr2[0] = 0x0101009e;
        iArr[0] = iArr2;
        int[] iArr3 = new int[e];
        iArr3[0] = -1;
        ColorStateList colorStateList = new ColorStateList(iArr, iArr3);

        for (int i = 0; i < lists.size(); i++) {
            final RadioButton Radioo = new RadioButton(this);
            final String finalFeatureName = featureName, radioName = lists.get(i);
            View.OnClickListener first_radio_listener = new View.OnClickListener() {
                public void onClick(View v) {
                    textView.setText(finalFeatureName + ": " + radioName);
                    textView.setText(Html.fromHtml("<font face='roboto'>" + finalFeatureName + ": <font color='#41c300'>" + radioName + "</font>"));
                    Preferences.changeFeatureInt(finalFeatureName, featureNum, radioGroup.indexOfChild(Radioo));
                }
            };
            System.out.println(lists.get(i));
            //int index = Preferences.loadPrefInt(featureName, featureNum);
            //Load preferences does not work well with radiobutton. Checked radio won't uncheck
            /*if ((index - 1) == i)
            {
                Radioo.setChecked(true);
                Preferences.changeFeatureInt(finalFeatureName, featureNum, index);
            }*/
            Radioo.setText(lists.get(i));
            Radioo.setTextColor(Color.LTGRAY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                Radioo.setButtonTintList(colorStateList);
            Radioo.setOnClickListener(first_radio_listener);
            radioGroup.addView(Radioo);
        }

        return radioGroup;
    }

    private View Category(String text) {
        TextView textView = new TextView(this);
        textView.setBackgroundColor(Color.parseColor("#2F3D4C"));
        textView.setText(Html.fromHtml(text));
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14.0f);
        textView.setTextColor(TEXT_COLOR_2);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(0, 5, 0, 5);
        return textView;
    }

    private View RichTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(Html.fromHtml(text));
        textView.setTextColor(TEXT_COLOR_2);
        textView.setPadding(10, 5, 10, 5);
        return textView;
    }

    private View RichWebView(String text) {
        WebView wView = new WebView(this);
        wView.loadData(text, "text/html", "utf-8");
        wView.setBackgroundColor(0x00000000); //Transparent
        wView.setPadding(0, 5, 0, 5);
        wView.getSettings().setAppCacheEnabled(false);
        wView.requestLayout();
        return wView;
    }

    private void localChanges(int featureNum, boolean toggle) {
        switch (featureNum) {
            case -1:
                mExpanded.setBackground(toggle ? gdAnimation : gdMenuBody);
                break;
            case -2:
                scrollView.setLayoutParams(toggle ? scrlLLExpanded : scrlLL);
                break;
            case -4:
                Logcat.Save(getApplicationContext());
                break;
            case -5:
                Logcat.Clear(getApplicationContext());
                break;
            case -6:
                settingsOpen = !settingsOpen;
                if (settingsOpen) {
                    scrollView.removeView(patches);
                    scrollView.addView(mSettings);
                } else {
                    scrollView.removeView(mSettings);
                    scrollView.addView(patches);
                }
                break;
            case -100:
                stopChecking = true;
                break;
        }
    }

    private void startAnimation() {
        //Credit: Octowolve
        //https://github.com/Octowolve/Hooking-Template-With-Mod-Menu/blob/27f68f4f7b4f8f40763aa2d2ebf9c85e7ae04fa5/app/src/main/java/com/dark/force/MenuService.java#L505

        final int start = Color.parseColor("#dd00820d");
        // final int middle = Color.parseColor("#0000ffff");
        final int end = Color.parseColor("#dd000282");

        final ArgbEvaluator evaluator = new ArgbEvaluator();
        gdAnimation.setCornerRadius(MENU_CORNER);
        gdAnimation.setOrientation(GradientDrawable.Orientation.TL_BR);
        final GradientDrawable gradient = gdAnimation;

        ValueAnimator animator = TimeAnimator.ofFloat(0.0f, 1.0f);
        animator.setDuration(10000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Float fraction = valueAnimator.getAnimatedFraction();
                int newStart = (int) evaluator.evaluate(fraction, start, end);
                //int newMiddle = (int) evaluator.evaluate(fraction, middle, end);
                int newEnd = (int) evaluator.evaluate(fraction, end, start);
                int[] newArray = {newStart, newEnd};
                gradient.setColors(newArray);
            }
        });

        animator.start();
    }

    //Override our Start Command so the Service doesnt try to recreate itself when the App is closed
    public int onStartCommand(Intent intent, int i, int i2) {
        return Service.START_NOT_STICKY;
    }

    private boolean isViewCollapsed() {
        return rootFrame == null || mCollapsed.getVisibility() == View.VISIBLE;
    }

    //For our image a little converter
    private int convertDipToPixels(int i) {
        return (int) ((((float) i) * getResources().getDisplayMetrics().density) + 0.5f);
    }

    private int dp(int i) {
        return (int) TypedValue.applyDimension(1, (float) i, getResources().getDisplayMetrics());
    }

    //Check if we are still in the game. If now our menu and menu button will dissapear
    private boolean isNotInGame() {
        RunningAppProcessInfo runningAppProcessInfo = new RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(runningAppProcessInfo);
        return runningAppProcessInfo.importance != 100;
    }

    //Destroy our View
    public void onDestroy() {
        super.onDestroy();
        if (rootFrame != null) {
            mWindowManager.removeView(rootFrame);
        }
        stopSelf();
    }

    //Same as above so it wont crash in the background and therefore use alot of Battery life
    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
        stopSelf();
    }

    private void Thread() {
        if (rootFrame == null) {
            return;
        }
        if (isNotInGame()) {
            rootFrame.setVisibility(View.INVISIBLE);
        } else {
            rootFrame.setVisibility(View.VISIBLE);
        }
    }

    private class EditTextValue {
        private int val;

        public void setValue(int i) {
            val = i;
        }

        public int getValue() {
            return val;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}