package com.sateda.keyonekb;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParser;
import java.io.File;
import java.util.Calendar;

import by.mkr.blackberry.textlayouttools.ActionType;
import by.mkr.blackberry.textlayouttools.AppSettings;
import by.mkr.blackberry.textlayouttools.Language;
import by.mkr.blackberry.textlayouttools.LanguageDetector;
import by.mkr.blackberry.textlayouttools.LanguageNotificationReceiver;
import by.mkr.blackberry.textlayouttools.LayoutConverter;
import by.mkr.blackberry.textlayouttools.ReplaceValues;
import by.mkr.blackberry.textlayouttools.ReplacerService;
import by.mkr.blackberry.textlayouttools.SoundManager;
import by.mkr.blackberry.textlayouttools.SoundPattern;
import by.mkr.blackberry.textlayouttools.VibrationManager;
import by.mkr.blackberry.textlayouttools.VibrationPattern;
import by.mkr.blackberry.textlayouttools.WordsList;
import static android.content.ContentValues.TAG;
import static com.sateda.keyonekb.ICHelper.CHARS_TO_GET;


public class KeyoneIME extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener,
        SpellCheckerSession.SpellCheckerSessionListener,
        View.OnTouchListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int MAX_KEY_COUNT = 50;
    private static final boolean DEBUG = false;

    public static final String APP_PREFERENCES = "kbsettings";
    public static final String APP_PREFERENCES_RU_LANG = "switch_ru_lang";
    public static final String APP_PREFERENCES_TRANSLIT_RU_LANG = "switch_translit_ru_lang";
    public static final String APP_PREFERENCES_UA_LANG = "switch_ua_lang";
    public static final String APP_PREFERENCES_SENS_BOTTON_BAR = "sens_botton_bar";
    public static final String APP_PREFERENCES_SHOW_TOAST = "show_toast";
    public static final String APP_PREFERENCES_ALT_SPACE = "alt_space";
    public static final String APP_PREFERENCES_SYMPAD = "sympad";
    public static final String APP_PREFERENCES_FLAG = "flag";
    public static final String APP_PREFERENCES_CURR_LANG = "pref_curr_lang";


    private static ReplacerService _replacerService;

    private NotificationManager notificationManager;
    private CandidateView mCandidateView;

    private SatedaKeyboardView keyboardView;
    private Keyboard keyboard_empty;
    private Keyboard keyboard_symbol;

    private Boolean fixBbkContact = false; // ?????????????? ?????? ???????????????????? ???????????????? ????????????????
    private Boolean fixBbkLauncher = false;

    private SharedPreferences mSettings;

    private boolean isEnglishKb = false;
    private int langNum = 0;
    private int langCount = 1;
    private int[] langArray;

    private Toast toast;

    private boolean ctrlPressed = false; // ???????????? ???????????? ?????????? ?????????? ??????????????

    private long mShiftPressTime;
    private boolean shiftPressFirstButtonBig; // ???????????? ???????????? ?????????? ?????????? ??????????????
    private boolean shiftPressFirstButtonBigDoublePress; // ???????????? ???????????? ?????????? ?????????? ?????????????? (?????? ???????????????? ?????????????? ???? ???????? ?? ???????? ????????????)
    private boolean shiftAfterPoint; // ?????????????? ?????????? ?????????? ?????????? (?????? ???????????? ???? ?????????? ??????????????????????, ???????????? ?????????? ???????? ?????????? ?????????????? ?????? ????????????????????)
    private boolean shiftPressAllButtonBig; //?????? ?????????????????? ?????????? ?????????? ??????????????
    private boolean shiftPressed; //?????????????? ?????????????? ?? ?????????????? ????????????

    private long mAltPressTime;
    private boolean altPressFirstButtonBig;
    private boolean altPressAllButtonBig;
    private boolean altShift;
    private boolean showSymbol;
    private boolean navigationSymbol;
    private boolean fnSymbol;

    private boolean menuKeyPressed; //?????????????? ?????????????? ?? ?????????????? ????????????

    private boolean altPressed; //?????????????? ?????????????? ?? ?????????????? ????????????
    private boolean altPlusBtn;

    private String langStr = "";
    private String lastPackageName = "";

    private float touchX;
    private float touchY;


    // Props to check if a notification needs update
    NotificationProperties _prevNotificationprops;

    //private android.support.v7.app.NotificationCompat.Builder builder;
    private StringBuilder mComposing = new StringBuilder();


    private int press_code_true; // ?????? ??????????????, ?????? ???????????????????? ?????????????? ???? ?????????? ???????????????????????? ?????????????? ????????????????????
    private int[] scan_code;
    private int[] one_press;
    private int[] one_press_shift;
    private int[] double_press;
    private int[] double_press_shift;
    private int[] shift;
    private int[] alt;
    private String[] alt_popup;
    private int[] alt_shift;
    private String[] alt_shift_popup;

    private int prev_key_press_btn_r0; //repeat 0 - ?????????????????? ?????????????????? ??????????????
    private int prev_key_press_btn_r1; //repeat 1 - ?????????????????? ?????? ?????????????????? ??????????????
    private long prev_key_press_time = 0;

    //settings
    private int sens_botton_bar = 10;
    private boolean show_toast = false;
    private boolean pref_alt_space = true;
    private boolean pref_sympad = false;
    private boolean pref_flag = false;

    private boolean pref_touch_keyboard = false;

    private WordsList _wordsListRu;
    private WordsList _wordsListEn;
    private int _lastAutoReplaced;
    private ActionType _lastActionType;
    Language _currentLanguage;
    AppSettings _appSettings;
    VibrationManager _vibrationManager;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Keyboard started");

        mShiftPressTime = 0;
        shiftPressFirstButtonBig = false;
        shiftPressFirstButtonBigDoublePress = false;
        shiftAfterPoint = false;
        shiftPressAllButtonBig  = false;
        shiftPressed = false;
        mAltPressTime = 0;
        altPressFirstButtonBig = false;
        altPressAllButtonBig = false;
        menuKeyPressed = false;
        altShift = false;
        altPressed = false;
        altPlusBtn = false;
        showSymbol = false;
        navigationSymbol = false;
        fnSymbol = false;
        ctrlPressed = false;

        show_toast = false;
        pref_alt_space = true;

        initKeys();
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        loadSetting();
        // Init Replacer service
        _replacerService = new ReplacerService(getApplicationContext());
        _appSettings = ReplacerService.getAppSettings();

        keyboard_empty = new Keyboard(this, R.xml.space_empty);
        keyboard_symbol = new Keyboard(this, R.xml.symbol);
        keyboardView = (SatedaKeyboardView) getLayoutInflater().inflate(R.layout.keyboard,null);
        keyboardView.setKeyboard(keyboard_empty);
        keyboardView.setOnKeyboardActionListener(this);
        keyboardView.setOnTouchListener(this);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setService(this);
        keyboardView.clearAnimation();
        keyboardView.showFlag(pref_flag);

        initNotificationManager();

        // Init TLT props
        _lastActionType = ActionType.None;
        _wordsListRu = new WordsList(getApplicationContext(), Language.Ru);
        _wordsListEn = new WordsList(getApplicationContext(), Language.En);
        _vibrationManager = new VibrationManager(getApplicationContext());
        // Register settings listener
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);

        // Fix for the first load as the method increases value immediately
        langNum--;
        ChangeLanguage();
    }

    private void initNotificationManager() {
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(getApplicationContext());

        builder.setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_rus_small)
                .setContentTitle("??????????????");
        notificationManager.notify(1, builder.build());

        _prevNotificationprops = getNotificationProps();
        _prevNotificationprops.langNum = -1; // Always update on start
    }

    private NotificationProperties getNotificationProps() {
        return new NotificationProperties(
                navigationSymbol,
                fnSymbol,
                pref_sympad,
                showSymbol,
                altShift,
                altPressAllButtonBig,
                altPressFirstButtonBig,
                shiftPressAllButtonBig,
                langNum,
                pref_touch_keyboard,
                shiftPressFirstButtonBig,

                _appSettings.isEnabled,
                _appSettings.isAutoCorrect,
                _appSettings.whenEnableNotifications
        );
    }
    private boolean notificationPropsHaveChanged(NotificationProperties compareTo) {
        //ReplacerService.log("** new:" + _appSettings.isAutoCorrect + " prev:" + compareTo.isAutoCorrect);
        return !(navigationSymbol == compareTo.navigationSymbol
                && fnSymbol == compareTo.fnSymbol
                && pref_sympad == compareTo.pref_sympad
                && showSymbol == compareTo.showSymbol
                && altShift == compareTo.altShift
                && altPressAllButtonBig == compareTo.altPressAllButtonBig
                && altPressFirstButtonBig == compareTo.altPressFirstButtonBig
                && shiftPressAllButtonBig == compareTo.shiftPressAllButtonBig
                && langNum == compareTo.langNum
                && pref_touch_keyboard == compareTo.pref_touch_keyboard
                && shiftPressFirstButtonBig == compareTo.shiftPressFirstButtonBig

                && _appSettings.isEnabled == compareTo.isEnabled
                && _appSettings.isAutoCorrect == compareTo.isAutoCorrect
                && _appSettings.whenEnableNotifications == compareTo.whenEnableNotifications
        );
    }

    private void loadSetting(){
        langCount = 1;
        show_toast = false;
        sens_botton_bar = 10;

        boolean lang_ru_on = true;
        boolean lang_translit_ru_on = false;
        boolean lang_ua_on = false;
        if(mSettings.contains(APP_PREFERENCES_RU_LANG)) {
            lang_ru_on = mSettings.getBoolean(APP_PREFERENCES_RU_LANG, true);
            if(lang_ru_on) langCount++;
        }else{
            if(lang_ru_on) langCount++;
        }

        if(mSettings.contains(APP_PREFERENCES_TRANSLIT_RU_LANG)) {
            lang_translit_ru_on = mSettings.getBoolean(APP_PREFERENCES_TRANSLIT_RU_LANG, false);
            if(lang_translit_ru_on) langCount++;
        }

        if(mSettings.contains(APP_PREFERENCES_UA_LANG)) {
            lang_ua_on = mSettings.getBoolean(APP_PREFERENCES_UA_LANG, false);
            if(lang_ua_on) langCount++;
        }

        langArray = new int[langCount];
        langArray[0] = R.xml.english_hw;
        langCount = 1;
        if(lang_ru_on){
            langArray[langCount] = R.xml.russian_hw;
            langCount++;
        }
        if(lang_translit_ru_on){
            langArray[langCount] = R.xml.russian_translit_hw;
            langCount++;
        }
        if(lang_ua_on){
            langArray[langCount] = R.xml.ukraine_hw;
            langCount++;
        }
        langCount-=1;
        langNum = 0;
        if(mSettings.contains(APP_PREFERENCES_CURR_LANG)) {
            langNum = mSettings.getInt(APP_PREFERENCES_CURR_LANG, 0);
        }

        if(mSettings.contains(APP_PREFERENCES_SENS_BOTTON_BAR)) {
            sens_botton_bar = mSettings.getInt(APP_PREFERENCES_SENS_BOTTON_BAR, 10);
        }

        if(mSettings.contains(APP_PREFERENCES_SHOW_TOAST)) {
            show_toast = mSettings.getBoolean(APP_PREFERENCES_SHOW_TOAST, false);
        }

        if(mSettings.contains(APP_PREFERENCES_ALT_SPACE)) {
            pref_alt_space = mSettings.getBoolean(APP_PREFERENCES_ALT_SPACE, true);
        }

        if(mSettings.contains(APP_PREFERENCES_SYMPAD)) {
            pref_sympad = mSettings.getBoolean(APP_PREFERENCES_SYMPAD, false);
        }

        if(mSettings.contains(APP_PREFERENCES_FLAG)) {
            pref_flag = mSettings.getBoolean(APP_PREFERENCES_FLAG, false);
        }
    }

    @Override
    public void onPress(int primaryCode) {
        Log.d(TAG, "onPress");
    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override public void onFinishInput() {
        if(showSymbol == true) {
            showSymbol = false;
            altPressFirstButtonBig = false;
            altPressAllButtonBig = false;
            altShift = false;
            UpdateNotify();
        }

        if(lastPackageName.equals("com.sateda.keyonekb")) loadSetting();
        keyboardView.showFlag(pref_flag);

        Log.d(TAG, "onFinishInput ");
    }

    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        Log.d(TAG, "onStartInput "+attribute.packageName+" "+attribute.label);
        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.

        if(attribute.packageName.equals("com.blackberry.contacts")) {
            fixBbkContact = true;
        }else{
            fixBbkContact = false;
        }

        if(attribute.packageName.equals("com.blackberry.blackberrylauncher")) {
            fixBbkLauncher = true;
        }else{
            fixBbkLauncher = false;
        }

        if(!attribute.packageName.equals(lastPackageName))
        {
            lastPackageName = attribute.packageName;
            navigationSymbol = false;
            fnSymbol = false;
            keyboardView.setFnSymbol(fnSymbol);
            if(!keyboardView.isShown()) {
                keyboardView.setVisibility(View.VISIBLE);
            }
        }


        if (!restarting) {
            Log.d(TAG, "onStartInput !restarting");
        }


        // We are now going to initialize our state based on the type of
        // text being edited.
        switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_DATETIME:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                altPressAllButtonBig = true;
                break;

            case InputType.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
                altPressAllButtonBig = true;
                break;

            case InputType.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
                altPressAllButtonBig = false;
                altPressFirstButtonBig = false;

                // We now look for a few special variations of text that will
                // modify our behavior.
                int variation = attribute.inputType & InputType.TYPE_MASK_VARIATION;
                if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Do not display predictions / what the user is typing
                    // when they are entering a password.
                    //mPredictionOn = false;
                }

                if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == InputType.TYPE_TEXT_VARIATION_URI
                        || variation == InputType.TYPE_TEXT_VARIATION_FILTER) {
                    // Our predictions are not useful for e-mail addresses
                    // or URIs.
                    //mPredictionOn = false;
                }

                if ((attribute.inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    // If this is an auto-complete text view, then our predictions
                    // will not be shown and instead we will allow the editor
                    // to supply their own.  We only show the editor's
                    // candidates when in fullscreen mode, otherwise relying
                    // own it displaying its own UI.
                    //mPredictionOn = false;
                    //mCompletionOn = isFullscreenMode();
                }

                // We also want to look at the current state of the editor
                // to decide whether our alphabetic keyboard should start out
                // shifted.
                updateShiftKeyState(attribute);
                break;

            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                altPressAllButtonBig = false;
                altPressFirstButtonBig = false;
                shiftPressFirstButtonBig = false;
                shiftPressAllButtonBig = false;
                updateShiftKeyState(attribute);
        }

        UpdateNotify();
        // Update the label on the enter key, depending on what the application
        // says it will do.
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        keyboardView.hidePopup(false);
        Log.d(TAG, "onKeyDown "+event);
        if(fixBbkLauncher &&  !this.isInputViewShown() && event.getScanCode() != 11){
            Log.d(TAG, "Oh! this fixBbkLauncher "+fixBbkLauncher);
            return super.onKeyDown(keyCode, event);
        }else if(fixBbkLauncher &&  !this.isInputViewShown() && event.getScanCode() == 11 && event.getRepeatCount() == 0 ){
            ChangeLanguage();
            return true;
        }else if(fixBbkLauncher &&  !this.isInputViewShown() && event.getScanCode() == 11 && event.getRepeatCount() == 1 ){
            pref_touch_keyboard = !pref_touch_keyboard;
            ChangeLanguageBack();
            return true;
        }

        long now = System.currentTimeMillis();
        InputConnection ic = getCurrentInputConnection();

        // ?????????????????????? ??????????????, ?????????????? true, ???????? ?????????????????? ??????????????????
        boolean is_double_press = false;
        boolean shift = false;
        boolean alt = false;
        int navigationKeyCode = 0;
        int code = 0;




        // TLT:
        try {
            _lastActionType = ActionType.None;

            // Ctrl+Q:
            if (_appSettings.isEnabled
                    && ctrlPressed && event.getKeyCode() == _appSettings.selectedShortCut
            ) {
                ReplacerService.log("---Ctrl+Q:---");

                CharSequence textSelected = ic.getSelectedText(0);
                ReplacerService.log("textSelected: " + textSelected);

                if (textSelected != null) {
                    // Convert selected text
                    int cursorPos = ICHelper.getCursorPos(ic);
                    ReplacerService.log("cursor pos: " + cursorPos);

                    String newWord = ICHelper.getReplacedWord(textSelected.toString(), _appSettings);

                    ICHelper.PerformReplace(ic,
                            cursorPos + textSelected.length(), // set to the end of selection
                            cursorPos,
                            textSelected.toString(),
                            newWord);

                    _replacerService.addToTempDictionary(newWord, textSelected.toString());
                } else {
                    // nothing selected, get the nearest word
                    int cursorPos = ICHelper.getCursorPos(ic);
                    CharSequence beforeCursorText = ic.getTextBeforeCursor(CHARS_TO_GET, 0);
                    CharSequence cursorText = beforeCursorText + "" + ic.getTextAfterCursor(CHARS_TO_GET, 0);
                    int offset = cursorPos - beforeCursorText.length();
                    ReplacerService.log("cursor pos: " + cursorPos + "; offset: " + offset);
                    ReplacerService.log("cursorText: " + cursorText);

                    // Use detector
                    ReplaceValues replValues = ICHelper.getReplaceValues(cursorText.toString(),
                            cursorPos == 0 ? 0 : (cursorPos - offset),
                            _currentLanguage,
                            _appSettings);

                    int moveCorrection = cursorPos - beforeCursorText.length() + replValues.WordWithBoundaries.Begin;
                    ICHelper.PerformReplace(ic,
                            cursorPos,
                            moveCorrection,
                            replValues.WordWithBoundaries.Word,
                            replValues.NewWord);

                    _replacerService.addToTempDictionary(replValues.NewWord, replValues.WordWithBoundaries.Word);
                }

                _lastActionType = ActionType.ManualChange;

                notify(_currentLanguage, ActionType.ManualChange);

                ReplacerService.log("------");

                return true;
            } // if Ctrl+Q

        } catch (Exception ex) {
            ReplacerService.log(ex.toString());
        }



        if(keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || event.getScanCode() == 110){
            int meta = KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
            if(ic!=null)ic.sendKeyEvent(new KeyEvent(
                    now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CTRL_LEFT, 0, meta));
            ctrlPressed = true;
            //show hide display keyboard
            if(shiftPressed && keyboardView.isShown()) {
                keyboardView.setVisibility(View.GONE);
                updateShiftKeyState(getCurrentInputEditorInfo());
            }else if(shiftPressed && !keyboardView.isShown()) {
                keyboardView.setVisibility(View.VISIBLE);
                updateShiftKeyState(getCurrentInputEditorInfo());
            }
            return true;
        }

        if(ctrlPressed && keyCode != KeyEvent.KEYCODE_SHIFT_RIGHT && event.getScanCode() != 110){
            int meta = KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
            if(ic!=null)ic.sendKeyEvent(new KeyEvent(
                    now, now, KeyEvent.ACTION_DOWN, keyCode, 0, meta));
            updateShiftKeyState(getCurrentInputEditorInfo());
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || (keyCode == KeyEvent.KEYCODE_1 && DEBUG)){
            if(showSymbol == true){
                showSymbol = false;
                altPressFirstButtonBig = false;
                altPressAllButtonBig = false;
                altShift = false;
                updateShiftKeyState(getCurrentInputEditorInfo());
            }else  if(altPressAllButtonBig == false && altPressFirstButtonBig == false){
                altShift = false;
                mAltPressTime = now;
                altPressFirstButtonBig = true;
            } else if (!altPressed && altPressFirstButtonBig == true && mAltPressTime + 1000 > now){
                altPressFirstButtonBig = false;
                altPressAllButtonBig = true;
            } else if (!altPressed && altPressFirstButtonBig == true && mAltPressTime + 1000 < now){
                altPressFirstButtonBig = false;
                altPressAllButtonBig = false;
                altShift = false;
                updateShiftKeyState(getCurrentInputEditorInfo());
            } else if (!altPressed && altPressAllButtonBig == true){
                altPressFirstButtonBig = false;
                altPressAllButtonBig = false;
                altShift = false;
                updateShiftKeyState(getCurrentInputEditorInfo());
            }
            altPressed = true;
            UpdateNotify();
            return true;
        }

        if (event.getScanCode() == 100 && event.getRepeatCount() == 0 || (keyCode == KeyEvent.KEYCODE_3 && DEBUG)){

            if(altPressed){ //?????????? ????????
                menuKeyPressed = true;
                if(ic!=null)ic.sendKeyEvent(new KeyEvent(   KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
                return true;
            }

            if(navigationSymbol) {
                navigationSymbol = false;
                showSymbol = false;
                altPressFirstButtonBig = false;
                altPressAllButtonBig = false;
                updateShiftKeyState(getCurrentInputEditorInfo());
            }else if(!showSymbol){
                showSymbol = true;
                altShift = true;
                altPressAllButtonBig = true;
                altPressFirstButtonBig = false;
            } else if(showSymbol && altShift){
                altShift = false;
            } else if(showSymbol && !altShift) {
                showSymbol = false;
                altPressFirstButtonBig = false;
                altPressAllButtonBig = false;
                updateShiftKeyState(getCurrentInputEditorInfo());
            }

            UpdateNotify();
            return true;
        } else if (event.getScanCode() == 100 && event.getRepeatCount() >= 1 && !navigationSymbol){
            navigationSymbol = true;
            fnSymbol = false;
            keyboardView.setFnSymbol(fnSymbol);
            UpdateNotify();
            return true;
        } else if (event.getScanCode() == 100 && event.getRepeatCount() >= 1 && navigationSymbol){
            return true;
        }


        //?????????????????????????? ??????????????
        if(navigationSymbol &&
                ((event.getScanCode() == 11) ||
                        (event.getScanCode() == 5) ||
                        (event.getScanCode() >= 16 && event.getScanCode() <= 25) ||
                        (event.getScanCode() >= 30 && event.getScanCode() <= 38) ||
                        (event.getScanCode() >= 44 && event.getScanCode() <= 50)))
        {
            if(!pref_sympad) {
                navigationKeyCode = getNavigationCode(event.getScanCode());
            }else{
                navigationKeyCode = getSymPadCode(event.getScanCode());
            }
            Log.d(TAG, "navigationKeyCode "+navigationKeyCode);
            if(navigationKeyCode == -7)
            {
                fnSymbol = !fnSymbol;
                UpdateNotify();
                keyboardView.setFnSymbol(fnSymbol);
                return true;
            }
            if(ic!=null && navigationKeyCode != 0) ic.sendKeyEvent(new KeyEvent(   KeyEvent.ACTION_DOWN, navigationKeyCode));
            return true;
        }

        if(altPressFirstButtonBig || altPressAllButtonBig || altPressed) alt = true;
        //Log.d(TAG, "onKeyDown altPressFirstButtonBig="+altPressFirstButtonBig+" altPressAllButtonBig="+altPressAllButtonBig+" altPressed="+altPressed);

        if (!shiftPressed && (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || (keyCode == KeyEvent.KEYCODE_2 && DEBUG))){
            if(!alt){
                if(shiftPressAllButtonBig == false && shiftPressFirstButtonBig == false){
                    mShiftPressTime = now;
                    shiftPressFirstButtonBig = true;
                } else if (shiftPressFirstButtonBig == true && mShiftPressTime + 1000 > now){
                    shiftPressFirstButtonBig = false;
                    shiftPressAllButtonBig = true;
                } else if (shiftPressFirstButtonBig == true && mShiftPressTime + 1000 < now){
                    shiftPressFirstButtonBig = false;
                    shiftPressAllButtonBig = false;
                } else if (shiftPressAllButtonBig == true){
                    shiftPressFirstButtonBig = false;
                    shiftPressAllButtonBig = false;
                }
            }else{
                altShift = !altShift;
            }
            shiftPressed = true;
            UpdateNotify();
            return true;

        }
        if(shiftPressFirstButtonBig || shiftPressAllButtonBig || shiftPressed) shift = true;
        if(alt) shift = altShift;
        //Log.d(TAG, "onKeyDown shiftPressFirstButtonBig="+shiftPressFirstButtonBig+" shiftPressAllButtonBig="+shiftPressAllButtonBig+" altShift="+altShift);


        if(event.getScanCode() == 11 && event.getRepeatCount() == 0 && alt == false ){
            ChangeLanguage();
            return true;
        }else if(event.getScanCode() == 11 && event.getRepeatCount() == 1 && alt == false ){
            pref_touch_keyboard = !pref_touch_keyboard;
            ChangeLanguageBack();
            return true;
        }else if(event.getScanCode() == 11 && event.getRepeatCount() > 1 && alt == false ){
            return true;
        }else if(event.getScanCode() == 11 && alt == true ){
            if(ic!=null)ic.commitText(String.valueOf((char) 48), 1);
            return true;
        }

        if(event.getRepeatCount() == 0) {
            code = KeyToButton(event.getScanCode(), alt, shift, false);
            //if (!alt && code != 0 && !shiftPressFirstButtonBigDoublePress) shiftPressFirstButtonBigDoublePress = shiftPressFirstButtonBig;
        }else if(event.getRepeatCount() == 1) { //?????????????????? ??????????????
            code = KeyToButton(event.getScanCode(), alt, true, false);
            if(code != 0 && ic!=null)ic.deleteSurroundingText(1,0);
        }else{
            code = KeyToButton(event.getScanCode(), alt, shift, false);
            if(code != 0) return true;
        }
        int code_double_press = 0;
        Log.d(TAG, "onKeyDown prev_key_press_btn_r0="+prev_key_press_btn_r0+" event.getScanCode() ="+event.getScanCode()+" shiftPressFirstButtonBig="+shiftPressFirstButtonBig);
        if(prev_key_press_btn_r0 == event.getScanCode() && event.getRepeatCount() == 0 &&  now < prev_key_press_time+500){
            if(shiftPressFirstButtonBigDoublePress) {
                code_double_press = KeyToButton(event.getScanCode(), alt, shiftPressFirstButtonBigDoublePress, true);
            }else{
                code_double_press = KeyToButton(event.getScanCode(), alt, shift, true);
            }
            if(code != code_double_press && code_double_press != 0){
                is_double_press = true;
                if(ic!=null) {
                    ic.deleteSurroundingText(1,0);
                    // TLT: Mark correction:
                    ic.commitCorrection(new CorrectionInfo(ICHelper.getCursorPos(ic), " ", " "));
                }
                code = code_double_press;
            }
        }else if(prev_key_press_btn_r1 == event.getScanCode() && event.getRepeatCount() == 1){
            is_double_press = true;
            code = KeyToButton(event.getScanCode(), alt, true, true);;
        }

        if(code != 0){
            if(fixBbkContact && !isEnglishKb){
                if(ic!=null){
                    ic.sendKeyEvent(new KeyEvent(   KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SEARCH));
                    ic.sendKeyEvent(new KeyEvent(   KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SEARCH));
                }
                fixBbkContact = false;
            }
            if(event.isAltPressed()) altPlusBtn = true;
            if(pref_alt_space == false && altPressFirstButtonBig == true)  altPressFirstButtonBig = false;
            if(is_double_press || alt == true){
                prev_key_press_btn_r1 = prev_key_press_btn_r0;
                prev_key_press_btn_r0 = 0;
                if (shiftPressFirstButtonBig == true){
                    shiftPressFirstButtonBig = false;
                    UpdateNotify();
                }
                shiftPressFirstButtonBigDoublePress = false;
            }else{
                prev_key_press_time = now;
                prev_key_press_btn_r0 = event.getScanCode();
                prev_key_press_btn_r1 = 0;
                Log.d(TAG, "onKeyDown shiftPressFirstButtonBig="+shiftPressFirstButtonBig);
                if (shiftPressFirstButtonBig == false) shiftPressFirstButtonBigDoublePress = false;
                if (shiftPressFirstButtonBig == true){
                    shiftPressFirstButtonBigDoublePress = true;
                    shiftPressFirstButtonBig = false;
                    UpdateNotify();
                }
            }

            if(ic!=null)
            {
                //ic.commitText(String.valueOf((char) code), 1);
                sendKeyChar((char) code);
                press_code_true = event.getScanCode();
            }
            else
            {
                Log.d(TAG, "onKeyDown ic==null");
            }
            if(!this.isInputViewShown() && ic!=null){
                if(ic.getTextBeforeCursor(1,0).length() > 0) this.showWindow(true);
            }
            //?????? ?????????????????????? ???????????? ???? ??????????. ???? ?????????? ????????????????????
            //if(shiftAfterPoint && isAlphabet(code)) shiftAfterPoint = false;
            //if(!shiftPressAllButtonBig && (code == 46 || code == 63 ||  code == 33 || code == 191)) shiftAfterPoint = true;
            return true;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                prev_key_press_btn_r0 = 0;
                if (altPressFirstButtonBig == true){
                    altPressFirstButtonBig = false;
                    altShift = false;
                    UpdateNotify();
                }
                updateShiftKeyState(getCurrentInputEditorInfo());
                return true;
            case KeyEvent.KEYCODE_SPACE:

                if (altPressFirstButtonBig == true){
                    altPressFirstButtonBig = false;
                    altShift = false;
                    UpdateNotify();
                }
                Log.d(TAG, "KEYCODE_SPACE prev_key_press_btn_r0 "+prev_key_press_btn_r0+" "+keyCode );
                if(this.isInputViewShown() && prev_key_press_btn_r0 == keyCode && now < prev_key_press_time+500 && ic!=null){
                    CharSequence back_letter = ic.getTextBeforeCursor(2,0);
                    Log.d(TAG, "KEYCODE_SPACE back_letter "+back_letter);
                    if(back_letter.length() == 2) {
                        if (Character.isLetterOrDigit(back_letter.charAt(0)) && back_letter.charAt(1) == ' ') {
                            ic.deleteSurroundingText(1, 0);
                            ic.commitText(".", 1);
                        }
                    }
                }
                if(ic!=null)ic.commitText(String.valueOf((char) 32), 1);
                Log.d(TAG, "KEYCODE_SPACE");
                updateShiftKeyState(getCurrentInputEditorInfo());
                prev_key_press_time = now;
                prev_key_press_btn_r0 = keyCode;
                return true;
            case KeyEvent.KEYCODE_DEL:
                Log.d(TAG, "KEYCODE_DEL");
                prev_key_press_btn_r0 = 0;
                if(!shiftPressed) {
                    keyDownUp(KeyEvent.KEYCODE_DEL);
                }else{
                    if(ic!=null)ic.deleteSurroundingText(0,1);
                }
                updateShiftKeyState(getCurrentInputEditorInfo());
                return true;

            default:
                prev_key_press_btn_r0 = 0;
        }

        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp "+event);
        int navigationKeyCode = 0;
        InputConnection ic = getCurrentInputConnection();
        // ?????????????????????? ???????????????????? ??????????????, ?????????????? true, ???????? ?????????????????? ??????????????????
        if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || (keyCode == KeyEvent.KEYCODE_1 && DEBUG)){
            if(menuKeyPressed){ //?????????? ????????
                menuKeyPressed = false;
                ic.sendKeyEvent(new KeyEvent(   KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU));
            }
            altPressed = false;
            if(altPlusBtn){
                altPressFirstButtonBig = false;
                altPressAllButtonBig = false;
                UpdateNotify();
                altPlusBtn = false;
                updateShiftKeyState(getCurrentInputEditorInfo());
            }
        }
        if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || (keyCode == KeyEvent.KEYCODE_2 && DEBUG)){
            shiftPressed = false;
        }

        if(keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || event.getScanCode() == 110){
            int meta = KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
            long now = System.currentTimeMillis();
            getCurrentInputConnection().sendKeyEvent(new KeyEvent(
                    now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CTRL_LEFT, 0, meta));
            ctrlPressed = false;
            return true;
        }

        if(keyCode == 100 && menuKeyPressed){ //?????????? ????????
            menuKeyPressed = false;
            ic.sendKeyEvent(new KeyEvent(   KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU));
        }

        if(navigationSymbol &&
                ((event.getScanCode() == 11) ||
                        (event.getScanCode() == 5) ||
                        (event.getScanCode() >= 16 && event.getScanCode() <= 25) ||
                        (event.getScanCode() >= 30 && event.getScanCode() <= 38) ||
                        (event.getScanCode() >= 44 && event.getScanCode() <= 50)))
        {
            if(!pref_sympad) {
                navigationKeyCode = getNavigationCode(event.getScanCode());
            }else{
                navigationKeyCode = getSymPadCode(event.getScanCode());
            }
            if(navigationKeyCode == -7) return true;
            if(ic!=null && navigationKeyCode != 0) ic.sendKeyEvent(new KeyEvent(   KeyEvent.ACTION_UP, navigationKeyCode));
            return true;
        }



        // TLT Autocorrect:
        try {
            if (!_lastActionType.isManual()
                    && _appSettings.isAutoCorrect
                    && keyCode != KeyEvent.KEYCODE_DEL
                    && keyCode != KeyEvent.KEYCODE_SHIFT_LEFT
                    && keyCode != KeyEvent.KEYCODE_SHIFT_RIGHT
                    && keyCode != KeyEvent.KEYCODE_ALT_LEFT
                    && keyCode != KeyEvent.KEYCODE_META_LEFT
                    && keyCode != KeyEvent.KEYCODE_META_RIGHT) {

                boolean isReplaceable = ICHelper.isReplaceableInput(this.getCurrentInputEditorInfo().inputType);
                boolean isBlockedByUser = _appSettings.autocorrectDirection.isEng() && _currentLanguage.isRus()
                        || _appSettings.autocorrectDirection.isRus() && _currentLanguage.isEng();


                CharSequence LastEnteredChars = ic.getTextBeforeCursor(1, 0);
                if (LastEnteredChars != null && LastEnteredChars.length() == 1) { // There is a char entered
                    ReplacerService.log("---Autocorrect:---");
                    Character lastChar = LastEnteredChars.charAt(0);

                    boolean isWordLetter = LanguageDetector.isWordLetter(lastChar, _currentLanguage);
                    ReplacerService.log("Letter: '" + lastChar + "'; is word: " + isWordLetter);

                    if (isReplaceable && !isBlockedByUser) {
                        if (!isWordLetter) {
                            int cursorPos = ICHelper.getCursorPos(ic);
                            CharSequence beforeCursorText = ic.getTextBeforeCursor(CHARS_TO_GET, 0);
                            int offset = cursorPos - beforeCursorText.length();


                            // Use detector
                            ReplaceValues replValues = ICHelper.getReplaceValues(beforeCursorText.toString(),
                                    cursorPos == 0 ? 0 : (cursorPos - offset),
                                    _currentLanguage,
                                    _appSettings);

                            if (_lastAutoReplaced != replValues.WordWithBoundaries.Begin) {
                                _lastAutoReplaced = replValues.WordWithBoundaries.Begin;
                                Language targetLang = LanguageDetector.getTargetLanguage(replValues.WordWithBoundaries.Word,
                                        _appSettings.inputMethod,
                                        _wordsListRu,
                                        _wordsListEn,
                                        _appSettings.userDict,
                                        _appSettings.corrections);

                                Language textEnteredLang = LayoutConverter.getTextLanguage(replValues.WordWithBoundaries.Word, _appSettings.inputMethod);

                                if (targetLang == textEnteredLang || targetLang == Language.Unknown) {
                                    ReplacerService.log("Same lang: " + targetLang);
                                    // Input language == keyboard language
                                    // Check for doubled capital
                                    if (LayoutConverter.isNeedDoubleCapitalCorrection(replValues.WordWithBoundaries.Word)) {
                                        int moveCorrection = cursorPos - beforeCursorText.length() + replValues.WordWithBoundaries.Begin;
                                        ICHelper.PerformReplace(ic,
                                                cursorPos,
                                                moveCorrection,
                                                replValues.WordWithBoundaries.Word,
                                                LayoutConverter.getTextWithoutDoubledCapital(replValues.WordWithBoundaries.Word));
                                    } else {
                                        // Do nothing
                                    }
                                } else {
                                    // Need to change layout
                                    ReplacerService.log("Change lang: " + textEnteredLang + " => " + targetLang);
                                    int moveCorrection = cursorPos - beforeCursorText.length() + replValues.WordWithBoundaries.Begin;
                                    ICHelper.PerformReplace(ic,
                                            cursorPos,
                                            moveCorrection,
                                            replValues.WordWithBoundaries.Word,
                                            LayoutConverter.getTextWithoutDoubledCapital(replValues.NewWord));

                                    ChangeLanguage(targetLang);

                                    notify(_currentLanguage, ActionType.AutoChange);
                                }
                            } else {
                                // Already corrected
                                ReplacerService.log("Already corrected: " + _lastAutoReplaced);
                            }
                        } else {
                            // if word letter
                            _lastAutoReplaced = -1;
                        }
                    }
                    ReplacerService.log("------");
                }
            } // Autocorrect
        } catch (Exception ex) {
            ReplacerService.log(ex.toString());
        }




        if(keyCode == KeyEvent.KEYCODE_ENTER ||keyCode == KeyEvent.KEYCODE_SPACE || keyCode == KeyEvent.KEYCODE_DEL) return true;

        //???????? ?????????????? ???????????????????? ?????? ??????????????, ???? ?????? ???? ?????????? ???????????????????? ?? ?????? ????????????????????
        if(press_code_true == event.getScanCode()) return true;

        return false;
    }

    public int getSymPadCode(int scanCode) {
        int keyEventCode = 0;
        switch (scanCode){
            case 17: //W
            case 22: //U
                keyEventCode = KeyEvent.KEYCODE_DPAD_UP_LEFT;
                break;
            case 18: //E
            case 23: //I
                keyEventCode = KeyEvent.KEYCODE_DPAD_UP;
                break;
            case 19: //R
            case 24: //O
                keyEventCode = KeyEvent.KEYCODE_DPAD_UP_RIGHT;
                break;

            case 31: //S
            case 36: //J
                keyEventCode = KeyEvent.KEYCODE_DPAD_LEFT;
                break;
            case 32: //D
            case 37: //K
                keyEventCode = KeyEvent.KEYCODE_DPAD_CENTER;
                break;
            case 33: //F
            case 38: //L
                keyEventCode = KeyEvent.KEYCODE_DPAD_RIGHT;
                break;

            case 44: //Z
            case 49: //N
                keyEventCode = KeyEvent.KEYCODE_DPAD_DOWN_LEFT;
                break;
            case 45: //X
            case 50: //M
                keyEventCode = KeyEvent.KEYCODE_DPAD_DOWN;
                break;
            case 46: //C
            case 5:  //$
                keyEventCode = KeyEvent.KEYCODE_DPAD_DOWN_RIGHT;
                break;

            default:
                keyEventCode = 0;
        }

        return keyEventCode;
    }

    public int getNavigationCode(int scanCode) {
        int keyEventCode = 0;
        switch (scanCode){
            case 16: //Q
                keyEventCode = 111; //ESC
                break;
            case 17: //W (1)
                if(fnSymbol) keyEventCode = 131; //F1
                break;
            case 18: //E (2)
                if(fnSymbol) keyEventCode = 132; //F2
                break;
            case 19: //R (3)
                if(fnSymbol) keyEventCode = 133; //F3
                break;
            case 20: //T
                keyEventCode = 0; //-----------------------------------------------------
                break;
            case 21: //Y
                keyEventCode = 122; //Home
                break;
            case 22: //U
                keyEventCode = 19; //Arrow Up
                break;
            case 23: //I
                keyEventCode = 123; //END
                break;
            case 24: //O
                keyEventCode = 92; //Page Up
                break;
            case 25: //P
                keyEventCode = -7; //FN
                break;

            case 30: //A
                keyEventCode = 61; //Tab
                break;
            case 31: //S (4)
                if(fnSymbol) keyEventCode = 134; //F4
                break;
            case 32: //D (5)
                if(fnSymbol) keyEventCode = 135; //F5
                break;
            case 33: //F (6)
                if(fnSymbol) keyEventCode = 136; //F6
                break;
            case 34: //G
                keyEventCode = 0; //-----------------------------------------------------
                break;
            case 35: //H
                keyEventCode = 21; //Arrow Left
                break;
            case 36: //J
                keyEventCode = 20; //Arrow Down
                break;
            case 37: //K
                keyEventCode = 22; //Arrow Right
                break;
            case 38: //L
                keyEventCode = 93; //Arrow Right
                break;

            case 44: //Z (7)
                if(fnSymbol) keyEventCode = 137; //F7
                break;
            case 45: //X (8)
                if(fnSymbol) keyEventCode = 138; //F8
                break;
            case 46: //C (9)
                if(fnSymbol) keyEventCode = 139; //F9
                break;

            case 11: //0
                if(fnSymbol) keyEventCode = 140; //F10
                break;

            default:
                keyEventCode = 0;
        }

        return keyEventCode;
    }

    @Override
    public boolean onKeyLongPress(int keyCode,KeyEvent event){
        Log.d(TAG, "onKeyLongPress "+event);
        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            //Do your stuff here
            return true;
        }
        return onKeyLongPress(keyCode,event);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateInputView() {
        Log.d(TAG, "onCreateInputView");
        //keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard,null);
        //keyboard = new Keyboard(this, R.xml.qwerty);
        //keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);
        return keyboardView;
    }

    @Override
    public void onUpdateSelection(final int oldSelStart, final int oldSelEnd,
                                  final int newSelStart, final int newSelEnd,
                                  final int composingSpanStart, final int composingSpanEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                composingSpanStart, composingSpanEnd);
        if (DEBUG) {
            Log.i(TAG, "onUpdateSelection: oss=" + oldSelStart + ", ose=" + oldSelEnd
                    + ", nss=" + newSelStart + ", nse=" + newSelEnd
                    + ", cs=" + composingSpanStart + ", ce=" + composingSpanEnd);
        }

        // This call happens whether our view is displayed or not, but if it's not then we should
        // not attempt recorrection. This is true even with a hardware keyboard connected: if the
        // view is not displayed we have no means of showing suggestions anyway, and if it is then
        // we want to show suggestions anyway.

        updateShiftKeyState(getCurrentInputEditorInfo());

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        Log.d(TAG, "onKey "+primaryCode);
        InputConnection inputConnection = getCurrentInputConnection();
        playClick(primaryCode);

        switch (primaryCode){

            case 19: //UP
                keyDownUp(KeyEvent.KEYCODE_DPAD_UP);
                updateShiftKeyState(getCurrentInputEditorInfo());
                break;
            case 20: //DOWN
                keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN);
                updateShiftKeyState(getCurrentInputEditorInfo());
                break;

            case 21: //LEFT
                keyDownUp(KeyEvent.KEYCODE_DPAD_LEFT);
                updateShiftKeyState(getCurrentInputEditorInfo());
                break;
            case 22: //RIGHT
                keyDownUp(KeyEvent.KEYCODE_DPAD_RIGHT);
                updateShiftKeyState(getCurrentInputEditorInfo());
                break;

            case 0: //SPACE
            case 32: //SPACE
                break;

            case 111: //ESC
            case 61:  //TAB
            case 122: //HOME
            case 123: //END
            case 92:  //Page UP
            case 93:  //Page DOWN
                keyDownUp(primaryCode);
                break;

            case -7:  //Switch F1-F12
                fnSymbol = !fnSymbol;
                UpdateNotify();
                keyboardView.setFnSymbol(fnSymbol);
                break;

            case Keyboard.KEYCODE_DELETE:
                inputConnection.deleteSurroundingText(1,0);
                updateShiftKeyState(getCurrentInputEditorInfo());
                break;


            case Keyboard.KEYCODE_DONE:
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                //inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER));

                updateShiftKeyState(getCurrentInputEditorInfo());
                break;
/*
            case 46:
            case 63:
            case 33:
            case 191:
                if(!shiftPressAllButtonBig){
                    shiftAfterPoint = true;
                }

 */
            default:
                char code = (char) primaryCode;
                inputConnection.commitText(String.valueOf(code),1);
        }

    }

    private void playClick(int i){

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch(i){
            case 32:
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;

            case Keyboard.KEYCODE_DONE:
            case 10:
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;

            case Keyboard.KEYCODE_DELETE:
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;

            default:
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }

    }


    @Override
    public void onText(CharSequence text) {
        Log.d(TAG, "onText: "+text);
    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //Log.d(TAG, "onTouch "+motionEvent);
        InputConnection inputConnection = getCurrentInputConnection();
        if(!showSymbol){
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) touchX = motionEvent.getX();
            if(motionEvent.getAction() == MotionEvent.ACTION_MOVE && touchX+(36-sens_botton_bar) < motionEvent.getX()){
                if(this.isInputViewShown()) {
                    CharSequence c = inputConnection.getTextAfterCursor(1, 0);
                    if(c.length() > 0) {
                        keyDownUp(KeyEvent.KEYCODE_DPAD_RIGHT);
                        updateShiftKeyState(getCurrentInputEditorInfo());
                    }
                    touchX = motionEvent.getX();
                    Log.d(TAG, "onTouch KEYCODE_DPAD_RIGHT " + motionEvent);
                }
            }else  if(motionEvent.getAction() == MotionEvent.ACTION_MOVE && touchX-(36-sens_botton_bar) > motionEvent.getX()){
                if(this.isInputViewShown()) {
                    CharSequence c = inputConnection.getTextBeforeCursor(1, 0);
                    if (c.length() > 0) {
                        keyDownUp(KeyEvent.KEYCODE_DPAD_LEFT);
                        updateShiftKeyState(getCurrentInputEditorInfo());
                    }
                    touchX = motionEvent.getX();
                    Log.d(TAG, "onTouch sens_botton_bar "+sens_botton_bar+" KEYCODE_DPAD_LEFT " + motionEvent);
                }
            }
        }else{
            if(motionEvent.getAction() == MotionEvent.ACTION_MOVE)keyboardView.coordsToIndexKey(motionEvent.getX());
            if(motionEvent.getAction() == MotionEvent.ACTION_UP  )keyboardView.hidePopup(true);
        }
        return false;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        /*
        ReplacerService.log("onGenericMotionEvent: " + event.getAction());
        int PRECISION = 16;
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            touchX = event.getX();
            touchY = event.getY();
        }
        if(event.getAction() == MotionEvent.ACTION_MOVE) {
            float curX = event.getX();
            float curY = event.getY();

            if (curX > touchX + PRECISION) {
                // left
                touchX = curX;
                keyDownUp(KeyEvent.KEYCODE_DPAD_LEFT);
            } else if (curX < touchX - PRECISION) {
                // right
                touchX = curX;
                keyDownUp(KeyEvent.KEYCODE_DPAD_RIGHT);
            }

            if (curY > touchY + PRECISION) {
                // up
                touchY = curY;
                keyDownUp(KeyEvent.KEYCODE_DPAD_UP);
            } else if (curY < touchY - PRECISION) {
                // down
                touchY = curY;
                keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN);
            }
        }
        */

        if (DEBUG) Log.v(TAG, "onGenericMotionEvent(): event " + event);
        if(pref_touch_keyboard){
            return false;
        }
        return true;
    }

    private void initKeys()
    {
        press_code_true = 0;
        scan_code = new int[MAX_KEY_COUNT];
        one_press = new int[MAX_KEY_COUNT];
        one_press_shift = new int[MAX_KEY_COUNT];
        double_press = new int[MAX_KEY_COUNT];
        double_press_shift = new int[MAX_KEY_COUNT];
        alt = new int[MAX_KEY_COUNT];
        shift = new int[MAX_KEY_COUNT];
        alt_popup = new String[MAX_KEY_COUNT];
        alt_shift = new int[MAX_KEY_COUNT];
        alt_shift_popup = new String[MAX_KEY_COUNT];

        resetKeys();
    }

    private void resetKeys()
    {
        for(int i = 0; i < MAX_KEY_COUNT; i++) {
            scan_code[i] = 0;
            one_press[i] = 0;
            one_press_shift[i] = 0;
            double_press[i] = 0;
            double_press_shift[i] = 0;
            alt[i] = 0;
            shift[i] = 0;
            alt_shift[i] = 0;
            alt_popup[i] = "";
            alt_shift_popup[i] = "";
        }
    }

    private void LoadLayout(int id)
    {
        resetKeys();
        int count_key = 0;
        int scan_code = 0;
        int one_press = 0;
        int double_press = 0;
        int double_press_shift = 0;
        int alt = 0;
        int shift = 0;
        int alt_shift = 0;
        String alt_popup = "";
        String alt_shift_popup = "";

        try {
            XmlPullParser parser = getResources().getXml(id);

            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                scan_code = 0;
                one_press = 0;
                double_press = 0;
                double_press_shift = 0;
                alt = 0;
                shift = 0;
                alt_shift = 0;
                alt_popup = "";
                alt_shift_popup = "";

                for (int i = 0; i < parser.getAttributeCount(); i++) {
                    if (parser.getAttributeName(i).equals("lang")) langStr = parser.getAttributeValue(i);
                    if (parser.getAttributeName(i).equals("scan_code")) scan_code = Integer.parseInt(parser.getAttributeValue(i));
                    if (parser.getAttributeName(i).equals("one_press")) one_press = Integer.parseInt(parser.getAttributeValue(i));
                    if (parser.getAttributeName(i).equals("double_press")) double_press = Integer.parseInt(parser.getAttributeValue(i));
                    if (parser.getAttributeName(i).equals("double_press_shift")) double_press_shift = Integer.parseInt(parser.getAttributeValue(i));
                    if (parser.getAttributeName(i).equals("alt")) alt = Integer.parseInt(parser.getAttributeValue(i));
                    if (parser.getAttributeName(i).equals("shift")) shift = Integer.parseInt(parser.getAttributeValue(i));
                    if (parser.getAttributeName(i).equals("alt_shift")) alt_shift = Integer.parseInt(parser.getAttributeValue(i));
                    if (parser.getAttributeName(i).equals("alt_popup")) alt_popup = parser.getAttributeValue(i);
                    if (parser.getAttributeName(i).equals("alt_shift_popup")) alt_shift_popup = parser.getAttributeValue(i);
                }

                if(scan_code != 0){
                    this.scan_code[count_key] = scan_code;
                    this.one_press[count_key] = one_press;
                    this.one_press_shift[count_key] = shift;
                    this.double_press[count_key] = double_press;
                    this.double_press_shift[count_key] = double_press_shift;
                    this.alt[count_key] = alt;
                    this.alt_shift[count_key] = alt_shift;
                    this.alt_popup[count_key] = alt_popup;
                    this.alt_shift_popup[count_key] = alt_shift_popup;
                    count_key++;
                }
                parser.next();
            }
        } catch (Throwable t) {
            Toast.makeText(this,
                    "???????????? ?????? ???????????????? XML-??????????????????: " + t.toString(),
                    Toast.LENGTH_LONG).show();
        }

        LoadAltLayout();
    }

    private void LoadAltLayout()
    {
        int scan_code = 0;
        int alt = 0;
        int alt_shift = 0;
        String alt_popup = "";
        String alt_shift_popup = "";

        try {
            XmlPullParser parser = getResources().getXml(R.xml.alt_hw);

            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                scan_code = 0;
                alt = 0;
                alt_shift = 0;
                alt_popup = "";
                alt_shift_popup = "";

                for (int i = 0; i < parser.getAttributeCount(); i++) {
                    if (parser.getAttributeName(i).equals("scan_code")) scan_code = Integer.parseInt(parser.getAttributeValue(i));
                    if (parser.getAttributeName(i).equals("alt")) alt = Integer.parseInt(parser.getAttributeValue(i));
                    if (parser.getAttributeName(i).equals("alt_shift")) alt_shift = Integer.parseInt(parser.getAttributeValue(i));
                    if (parser.getAttributeName(i).equals("alt_popup")) alt_popup = parser.getAttributeValue(i);
                    if (parser.getAttributeName(i).equals("alt_shift_popup")) alt_shift_popup = parser.getAttributeValue(i);
                }

                if(scan_code != 0){
                    for(int i = 0; i < MAX_KEY_COUNT; i++){
                        if(this.scan_code[i] == scan_code && this.alt[i] == 0 && alt != 0){
                            this.alt[i] = alt;
                        }
                        if(this.scan_code[i] == scan_code && this.alt_shift[i] == 0 && alt_shift != 0){
                            this.alt_shift[i] = alt_shift;
                        }
                        if(this.scan_code[i] == scan_code && this.alt_popup[i].equals("") && alt_popup != ""){
                            this.alt_popup[i] = alt_popup;
                        }
                        if(this.scan_code[i] == scan_code && this.alt_shift_popup[i].equals("") && alt_shift_popup != ""){
                            this.alt_shift_popup[i] = alt_shift_popup;
                        }
                    }
                }
                parser.next();
            }
        } catch (Throwable t) {
            Toast.makeText(this,
                    "???????????? ?????? ???????????????? XML-??????????????????: " + t.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private int KeyToButton(int key, boolean alt_press, boolean shift_press, boolean is_double_press)
    {
        int result = 0;

        for(int i = 0; i < MAX_KEY_COUNT; i++) {
            if (this.scan_code[i]  == key) {
                if (alt_press == true && shift_press == true && this.alt_shift[i] != 0) {
                    result = this.alt_shift[i];
                } else if (alt_press == true && this.alt[i] != 0) {
                    result = this.alt[i];
                } else if (is_double_press == true && shift_press == true && this.double_press_shift[i] != 0) {
                    result = this.double_press_shift[i];
                } else if (is_double_press == true && this.double_press[i] != 0) {
                    result = this.double_press[i];
                } else if (shift_press == true && this.one_press_shift[i] != 0) {
                    result = this.one_press_shift[i];
                } else {
                    result = this.one_press[i];
                }
            }
            if(result != 0) return result;
        }
        return result;
    }
    private void UpdateNotify() {
        UpdateNotify(false);
    }

    private void UpdateNotify(boolean forceUpdate) {
        //notificationManager.cancelAll();
        Log.d(TAG, "UpdateNotify shiftPressFirstButtonBig="+shiftPressFirstButtonBig+" shiftPressAllButtonBig="+shiftPressAllButtonBig+" altPressAllButtonBig="+altPressAllButtonBig+" altPressFirstButtonBig="+altPressFirstButtonBig);

        // Check if anything has changed
        if (forceUpdate || notificationPropsHaveChanged(_prevNotificationprops)) {
            // Update state and continue
            _prevNotificationprops = getNotificationProps();
        } else {
            // no need to update notification, exit
            return;
        }


        ReplacerService.log("UpdateNotify");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        if(navigationSymbol){
            if(!fnSymbol)
            {
                builder.setSmallIcon(R.mipmap.ic_kb_nav);
                builder.setContentTitle("??????????????????");
            }
            else
            {
                builder.setSmallIcon(R.mipmap.ic_kb_nav_fn);
                builder.setContentTitle("?????????????????? + F1-F10");
            }
            if(!pref_sympad) {
                keyboard_symbol = new Keyboard(this, R.xml.navigation);
                keyboardView.setKeyboard(keyboard_symbol);
                keyboardView.setNavigationLayer();
            }else{
                keyboardView.setLang("SymPad");
                keyboardView.setKeyboard(keyboard_empty);
                keyboardView.setAlt();
            }
        }else if(showSymbol){
            builder.setSmallIcon(R.mipmap.ic_kb_sym);
            builder.setContentTitle("?????????????? 1-9");
            keyboard_symbol = new Keyboard(this, R.xml.symbol);
            keyboardView.setKeyboard(keyboard_symbol);
            if(altShift) {
                keyboardView.setAltLayer(scan_code, alt_shift, alt_shift_popup);
            }else{
                keyboardView.setAltLayer(scan_code, alt, alt_popup);
            }
            //keyboardView.setAlt();
        }else if(altPressAllButtonBig){
            builder.setSmallIcon(R.mipmap.ic_kb_sym);
            builder.setContentTitle("?????????????? 1-9");
            //keyboard_symbol = new Keyboard(this, R.xml.symbol);
            keyboardView.setKeyboard(keyboard_empty);
            if(altShift) {
                //keyboardView.setAltLayer(scan_code, alt_shift);
                keyboardView.setLang("?????????????? {} [] | / ");
            }else{
                //keyboardView.setAltLayer(scan_code, alt);
                keyboardView.setLang("?????????????? 1-9");
            }
            keyboardView.setAlt();
        }else if(altPressFirstButtonBig){
            builder.setSmallIcon(R.mipmap.ic_kb_sym_one);
            builder.setContentTitle("?????????????? 1-9");
            //keyboard_symbol = new Keyboard(this, R.xml.symbol);
            keyboardView.setKeyboard(keyboard_empty);
            if(altShift) {
                keyboardView.setLang("?????????????? {} [] | / ");
            }else{
                keyboardView.setLang("?????????????? 1-9");
            }
            keyboardView.setAlt();
        }else if(shiftPressAllButtonBig){
            if(langArray[langNum] == R.xml.ukraine_hw) {
                if(pref_touch_keyboard == false) {
                    builder.setSmallIcon(R.mipmap.ic_ukr_shift_all);
                }else{
                    builder.setSmallIcon(R.mipmap.ic_ukr_shift_all_touch);
                }
            }else if(langArray[langNum] == R.xml.russian_hw || langArray[langNum] == R.xml.russian_translit_hw) {
                if(pref_touch_keyboard == false) {
                    builder.setSmallIcon(R.mipmap.ic_rus_shift_all);
                }else{
                    builder.setSmallIcon(R.mipmap.ic_rus_shift_all_touch);
                }
            }else{
                if(pref_touch_keyboard == false) {
                    builder.setSmallIcon(R.mipmap.ic_eng_shift_all);
                }else{
                    builder.setSmallIcon(R.mipmap.ic_eng_shift_all_touch);
                }
            }
            builder.setContentTitle(langStr);
            keyboardView.setLang(langStr);
            keyboardView.setShiftAll();
            keyboardView.setKeyboard(keyboard_empty);
            keyboardView.setLetterKB();
        }else if(shiftPressFirstButtonBig){
            if(langArray[langNum] == R.xml.ukraine_hw) {
                if(pref_touch_keyboard == false) {
                    builder.setSmallIcon(R.mipmap.ic_ukr_shift_first);
                }else{
                    builder.setSmallIcon(R.mipmap.ic_ukr_shift_first_touch);
                }
            }else if(langArray[langNum] == R.xml.russian_hw || langArray[langNum] == R.xml.russian_translit_hw) {
                if(pref_touch_keyboard == false) {
                    builder.setSmallIcon(R.mipmap.ic_rus_shift_first);
                }else{
                    builder.setSmallIcon(R.mipmap.ic_rus_shift_first_touch);
                }
            }else{
                if(pref_touch_keyboard == false) {
                    builder.setSmallIcon(R.mipmap.ic_eng_shift_first);
                }else{
                    builder.setSmallIcon(R.mipmap.ic_eng_shift_first_touch);
                }
            }
            builder.setContentTitle(langStr);
            keyboardView.setLang(langStr);
            keyboardView.setShiftFirst();
            keyboardView.setKeyboard(keyboard_empty);
            keyboardView.setLetterKB();
        }else{
            if(langArray[langNum] == R.xml.ukraine_hw) {
                if(pref_touch_keyboard == false) {
                    builder.setSmallIcon(R.mipmap.ic_ukr_small);
                }else{
                    builder.setSmallIcon(R.mipmap.ic_ukr_small_touch);
                }
            }else if(langArray[langNum] == R.xml.russian_hw || langArray[langNum] == R.xml.russian_translit_hw) {
                if(pref_touch_keyboard == false) {
                    builder.setSmallIcon(R.mipmap.ic_rus_small);
                }else{
                    builder.setSmallIcon(R.mipmap.ic_rus_small_touch);
                }
            }else{
                if(pref_touch_keyboard == false) {
                    builder.setSmallIcon(R.mipmap.ic_eng_small);
                }else{
                    builder.setSmallIcon(R.mipmap.ic_eng_small_touch);
                }
            }
            builder.setContentTitle(langStr);
            keyboardView.notShift();
            keyboardView.setLang(langStr);
            keyboardView.setKeyboard(keyboard_empty);
            keyboardView.setLetterKB();
        }

        // TLT action buttons
        NotificationCompat.Action actionSoundSwitch = LanguageNotificationReceiver
                .createNotificationAction(getApplicationContext(), LanguageNotificationReceiver.ACTION_MUTE_SWITCH);
        NotificationCompat.Action actionManualSwitch = LanguageNotificationReceiver
                .createNotificationAction(getApplicationContext(), LanguageNotificationReceiver.ACTION_MANUAL_SWITCH);
        NotificationCompat.Action actionAutocorrectSwitch = LanguageNotificationReceiver
                .createNotificationAction(getApplicationContext(), LanguageNotificationReceiver.ACTION_AUTOCORRECT_SWITCH);
        PendingIntent actionIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        String subText = null;
        String contentText = null;

        // If update is available
        if (_appSettings != null
                && _appSettings.checkForUpdates.isOn()
                && _appSettings.isUpdateAvailable()
        ) {
            subText = getString(R.string.notification_text_update_available);
            contentText = getString(R.string.notification_text_update_available_desc);
            actionIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    0,
                    new Intent(Intent.ACTION_VIEW, Uri.parse(_appSettings.updateLink)),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        builder.setContentIntent(actionIntent)
                .addAction(actionSoundSwitch)
                .addAction(actionManualSwitch)
                .addAction(actionAutocorrectSwitch)
                .setSubText(subText)
                .setContentText(contentText)

                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setOngoing(true);

        notificationManager.notify(1, builder.build());

    }

    private void ChangeLanguageCommon() {
        if (langNum > langCount) langNum = 0;
        if (langNum < 0) langNum = langCount;
        if (langNum == 0) {
            isEnglishKb = true;
            _currentLanguage = Language.getByInputMethod(Language.En, _appSettings.inputMethod);
        } else {
            isEnglishKb = false;

            if (langNum == 1) {
                _currentLanguage = Language.getByInputMethod(Language.Ru, _appSettings.inputMethod);
            } else {
                _currentLanguage = Language.getByInputMethod(Language.Ukr, _appSettings.inputMethod);
            }
        }
        LoadLayout(langArray[langNum]);
        if (show_toast) {
            toast = Toast.makeText(getApplicationContext(), langStr, Toast.LENGTH_SHORT);
            toast.show();
        }
        UpdateNotify();
        //notify(_currentLanguage, ActionType.AltEnter);
        ReplacerService.log("Change _currentLanguage: " + _currentLanguage);

        // Save selected language
        mSettings.edit().putInt(APP_PREFERENCES_CURR_LANG, langNum).commit();
    }

    private void ChangeLanguage(Language newLang) {
        switch (newLang) {
            case Ru:
            case RuFull:
            case RuTrans:
            case RuQwertz:
            case RuFxtecPro1: {
                langNum = 1; // Russian is 1?
                break;
            }
            case En:
            case EnFull:
            case EnTrans:
            case EnQwertz:
            case EnFxtecPro1: {
                langNum = 0; // English is always 0
                break;
            }
            default: {
                // No change
                break;
            }
        }
        ChangeLanguageCommon();
    }

    private void ChangeLanguage() {
        langNum++;
        ChangeLanguageCommon();
    }

    private void ChangeLanguageBack() {
        langNum--;
        ChangeLanguageCommon();
    }

    private boolean isAlphabet(int code) {
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
    }

    private void keyDownUp(int keyEventCode) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    private void updateShiftKeyState(EditorInfo attr) {
        Log.d(TAG, "updateShiftKeyState attr "+attr.inputType);
        if (attr != null && !altPressFirstButtonBig && !altPressAllButtonBig && !altPressed ) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
                if(caps != 0)Log.d(TAG, "updateShiftKeyState");
            }
            if(caps != 0 && !shiftPressAllButtonBig){
                shiftPressFirstButtonBig = true;
            }else {
                shiftPressFirstButtonBig = false;
            }

            UpdateNotify();
        }
    }

    private void notify(Language lang, ActionType actionType) {
        if (isNotificationsEnabled()) {
            playSound(lang, actionType);
            vibrate(lang, actionType);
        }
    }

    public boolean isNotificationsEnabled() {
        return _appSettings.whenEnableNotifications <= Calendar.getInstance().getTimeInMillis();
    }

    private void playSound(Language lang, ActionType actionType) {
        switch (lang) {
            case Ru:
            case RuTrans:
            case RuFull:
            case RuQwertz:
            case RuFxtecPro1: {
                if (actionType == ActionType.AltEnterReplace) {
                    // Play only if no Input sound
                    if (_appSettings.soundInputRus == SoundPattern.None) {
                        SoundManager.play(this, _appSettings.soundCorrectRus);
                    }
                } else if (actionType == ActionType.AltEnter || actionType == ActionType.CtrlSpace) {
                    SoundManager.play(this, _appSettings.soundInputRus);
                } else {
                    SoundManager.play(this, _appSettings.soundCorrectRus);
                }
                break;
            }
            case En:
            case EnTrans:
            case EnFull:
            case EnQwertz:
            case EnFxtecPro1: {
                if (actionType == ActionType.AltEnterReplace) {
                    // Play only if no Input sound
                    if (_appSettings.soundInputRus == SoundPattern.None) {
                        SoundManager.play(this, _appSettings.soundCorrectEng);
                    }
                } else if (actionType == ActionType.AltEnter || actionType == ActionType.CtrlSpace) {
                    SoundManager.play(this, _appSettings.soundInputEng);
                } else {
                    SoundManager.play(this, _appSettings.soundCorrectEng);
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    private void vibrate(Language lang, ActionType actionType) {
        switch (lang) {
            case Ru:
            case RuTrans:
            case RuFull:
            case RuQwertz:
            case RuFxtecPro1: {
                if (actionType == ActionType.AltEnterReplace || actionType == ActionType.CtrlSpace) {
                    // Vibrate only if no Input vibration
                    if (_appSettings.vibrationPatternRus == VibrationPattern.None) {
                        _vibrationManager.vibrate(_appSettings.vibrationPatternRus);
                    }
                } else {
                    _vibrationManager.vibrate(_appSettings.vibrationPatternRus);
                }
                break;
            }
            case En:
            case EnTrans:
            case EnFull:
            case EnQwertz:
            case EnFxtecPro1: {
                if (actionType == ActionType.AltEnterReplace || actionType == ActionType.CtrlSpace) {
                    // Vibrate only if no Input vibration
                    if (_appSettings.vibrationPatternRus == VibrationPattern.None) {
                        _vibrationManager.vibrate(_appSettings.vibrationPatternEng);
                    }
                } else {
                    _vibrationManager.vibrate(_appSettings.vibrationPatternEng);
                }
                break;
            }
            default: {
                break;
            }
        }
    }


    @Override public View onCreateCandidatesView() {
        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);
        return mCandidateView;
    }

    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) {
        Log.d(TAG, "onGetSuggestions");
    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {
        Log.d(TAG, "onGetSentenceSuggestions");
    }


    public static File createAppFolder() {
        try {
            File f = new File(Environment.getExternalStorageDirectory(), "TextLayoutTools");
            if (!f.exists()) {
                f.mkdirs();
            }
            return f;
        } catch (Exception ex) {
            Log.d(TAG, "Ex createAppFolder: " + ex.getMessage());
        }
        return null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        _appSettings.bindSettings(sharedPreferences);

        if (getString(R.string.setting_is_auto_correct).equals(key)
                || getString(R.string.setting_when_enable_notifications).equals(key)
                || getString(R.string.setting_shortcut_enabled_key).equals(key)
                || getString(R.string.setting_application_updates_available_ver).equals(key)
                || getString(R.string.setting_application_updates_link).equals(key)
        ) {
            ReplacerService.log("Setting changed: " + key);
            UpdateNotify(true);
        }

        // Preview Vibration
        if (getString(R.string.setting_vibration_pattern_rus).equals(key)
        ) {
            //vibrate(Language.Ru, ActionType.AltEnter);
        }
        if (getString(R.string.setting_vibration_pattern_eng).equals(key)) {
            //vibrate(Language.En, ActionType.AltEnter);
        }

        // Preview Input Sound
        if (getString(R.string.setting_sound_input_rus).equals(key)) {
            //playSound(Language.Ru, ActionType.AltEnter);
        }
        if (getString(R.string.setting_sound_input_eng).equals(key)) {
            //playSound(Language.En, ActionType.AltEnter);
        }

        // Preview Correct Sound
        if (getString(R.string.setting_sound_correct_rus).equals(key)) {
            //playSound(Language.Ru, ActionType.ManualChange);
        }
        if (getString(R.string.setting_sound_correct_eng).equals(key)) {
            //playSound(Language.En, ActionType.ManualChange);
        }

        // Updates settings
        if (getString(R.string.setting_application_updates_check).equals(key)) {
            UpdateNotify(true);
            _replacerService.toggleVersionChecker(_appSettings.checkForUpdates.isOn());
        }
    }
}
