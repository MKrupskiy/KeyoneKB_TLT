package by.mkr.blackberry.textlayouttools;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sateda.keyonekb.BuildConfig;
import com.sateda.keyonekb.KeyoneIME;
import com.sateda.keyonekb.R;


public class AppSettings {
    public int version = 3;
    public int selectedCtrlMod;
    public int selectedShortCut;
    public Language autocorrectDirection;
    public InputMethod inputMethod;
    public boolean isEnabled;
    public boolean isKey2EmulationEnabled;
    public boolean isTranslit;
    public boolean isShowInfo;
    public boolean isReplaceAltEnter;
    public boolean isCorrectDoubled;
    public boolean isAutoCorrect;
    public boolean isLogToFile;
    public boolean isSelectReplaced;
    public String[] userDict;

    public VibrationPattern vibrationPatternRus;
    public VibrationPattern vibrationPatternEng;
    public SoundPattern soundInputRus;
    public SoundPattern soundInputEng;
    public SoundPattern soundCorrectRus;
    public SoundPattern soundCorrectEng;
    public NetworkState checkForUpdates;

    public List<CorrectionItem> corrections;

    public transient long whenEnableNotifications;
    public transient UserTempDictionary userTempDict;

    public transient String availableUpdateVersion;
    public transient String updateLink;

    private transient Context _context;


    public AppSettings(Context context) {
        _context = context;
        bindSettings(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public void bindSettings(SharedPreferences sharedPrefs) {
        String selectedCtrlModStr = sharedPrefs.getString(_context.getString(R.string.setting_control_key), "129");
        selectedCtrlMod = Integer.parseInt(selectedCtrlModStr);
        String selectedShortCutStr = sharedPrefs.getString(_context.getString(R.string.setting_shortcut_key), "" + KeyEvent.KEYCODE_Q);
        selectedShortCut = Integer.parseInt(selectedShortCutStr);
        String autocorrectDirectionStr = sharedPrefs.getString(_context.getString(R.string.setting_autocorrect_direction), Language.getDefault());
        autocorrectDirection = Language.fromString(autocorrectDirectionStr);
        isEnabled = sharedPrefs.getBoolean(_context.getString(R.string.setting_shortcut_enabled_key), true);
        isKey2EmulationEnabled = sharedPrefs.getBoolean(_context.getString(R.string.setting_key2emulation_enabled), false);
        isTranslit = "Translit".equals(sharedPrefs.getString(_context.getString(R.string.setting_input_method), "Translit"));
        isShowInfo = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_show_info), false);
        isReplaceAltEnter = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_replace_alt_enter), true);
        isAutoCorrect = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_auto_correct), true);
        isLogToFile = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_log_to_sd), false);
        isSelectReplaced = sharedPrefs.getBoolean(_context.getString(R.string.setting_select_replaced), false);
        String userDictStr = sharedPrefs.getString(_context.getString(R.string.setting_user_dictionary), "");
        userDict = !TextUtils.isEmpty(userDictStr)
            ? userDictStr
                .toLowerCase()
                .replaceAll("\n{2,}", "\n")
                .replaceAll("\n+$", "")
                .replaceAll("^\n+", "")
                .split("\n")
            : new String[0];

        String userTempDictionaryStr = sharedPrefs.getString(_context.getString(R.string.setting_user_temp_dictionary), "");
        userTempDict = new UserTempDictionary(userTempDictionaryStr);

        String vibrationPatternRusStr = sharedPrefs.getString(_context.getString(R.string.setting_vibration_pattern_rus), VibrationPattern.getDefault());
        vibrationPatternRus = VibrationPattern.fromString(vibrationPatternRusStr);
        String vibrationPatternEngStr = sharedPrefs.getString(_context.getString(R.string.setting_vibration_pattern_eng), "DoubleShort");
        vibrationPatternEng = VibrationPattern.fromString(vibrationPatternEngStr);

        String soundInputRusStr = sharedPrefs.getString(_context.getString(R.string.setting_sound_input_rus), SoundPattern.getDefault());
        soundInputRus = SoundPattern.fromString(soundInputRusStr);
        String soundInputEngStr = sharedPrefs.getString(_context.getString(R.string.setting_sound_input_eng), SoundPattern.getDefault());
        soundInputEng = SoundPattern.fromString(soundInputEngStr);

        String soundCorrectRusStr = sharedPrefs.getString(_context.getString(R.string.setting_sound_correct_rus), "Ru");
        soundCorrectRus = SoundPattern.fromString(soundCorrectRusStr);
        String soundCorrectEngStr = sharedPrefs.getString(_context.getString(R.string.setting_sound_correct_eng), "En");
        soundCorrectEng = SoundPattern.fromString(soundCorrectEngStr);

        whenEnableNotifications = sharedPrefs.getLong(_context.getString(R.string.setting_when_enable_notifications), 0);

        String InputMethodStr = sharedPrefs.getString(_context.getString(R.string.setting_input_method), InputMethod.getDefault());
        inputMethod = InputMethod.fromString(InputMethodStr);

        corrections = getCorrections();

        String checkForUpdatesStr = sharedPrefs.getString(_context.getString(R.string.setting_application_updates_check), NetworkState.getDefault());
        checkForUpdates = NetworkState.fromString(checkForUpdatesStr);

        availableUpdateVersion = sharedPrefs.getString(_context.getString(R.string.setting_application_updates_available_ver), "");
        updateLink = sharedPrefs.getString(_context.getString(R.string.setting_application_updates_link), "");

    }

    public String getString(int resourceId) {
        return _context.getString(resourceId);
    }

    public void updateUserDict(String[] newUserDict) {
        String newUserDictStr = TextUtils
                .join("\n", newUserDict)
                .replaceAll("\n{2,}", "\n");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putString(getString(R.string.setting_user_dictionary), newUserDictStr);
        edit.apply();
    }

    public void updateUserTempDict(UserTempDictionary newUserDict) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putString(getString(R.string.setting_user_temp_dictionary), newUserDict.toString());
        edit.apply();
    }

    public void updateCorrections(List<CorrectionItem> correctionItems) {
        String value = TextUtils.join(CorrectionItem.ITEMS_DELIMITER, correctionItems);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putString(getString(R.string.setting_corrections), value);
        edit.apply();
    }

    public List<CorrectionItem> getCorrections() {
        List<CorrectionItem> corrItems = new ArrayList<CorrectionItem>();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        String value = sharedPrefs.getString(_context.getString(R.string.setting_corrections), CorrectionItem.DEFAULT_VALUE);
        try {
            String[] stringItems = value.split(CorrectionItem.ITEMS_DELIMITER);
            for (int i = 0; i < stringItems.length; i++) {
                corrItems.add(new CorrectionItem(stringItems[i]));
            }
        } catch (Exception ex) {
            ReplacerService.log("getCorrections(): Can't parse saved value: '" + value + "'");
        }
        return corrItems;
    }

    public boolean saveToFile() {
        try {
            Gson gson = new Gson();
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
            String json = gson.toJson(/*sharedPrefs*/this);
            ReplacerService.log("saveToFile: " + json);

            File appFolder = KeyoneIME.createAppFolder();

            File file = new File(appFolder, "settings.json");
            if (!file.exists())
            {
                file.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(file, false);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            osw.write(json);
            osw.flush();
            osw.close();

            return true;
        } catch (Exception ex) {
            ReplacerService.log("Ex saveToFile: " + ex.getMessage());
            return false;
        }
    }

    public boolean loadFromFile(Uri fileUri) {
        try {
            ContentResolver cr = _context.getContentResolver();
            InputStream inputStream = cr.openInputStream(fileUri);
            if (inputStream == null) {
                throw new IOException("Unable to obtain input stream from URI");
            }

            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream)));
            AppSettings data = gson.fromJson(reader, AppSettings.class);
            ReplacerService.log("Loaded settings version: " + data.version);
            if (data.version == 0) {
                throw new Exception("Wrong settings file");
            }

            reader.close();

            return this.replaceSettings(data);
        } catch (Exception ex) {
            ReplacerService.log("Ex loadFromFile: " + ex.getMessage());
            return false;
        }
    }

    public boolean isUpdateAvailable() {
        if (this.availableUpdateVersion == null && this.availableUpdateVersion.length() == 0) {
            return false;
        }
        float currentVer = Float.parseFloat(BuildConfig.VERSION_NAME);
        float newVer = Float.parseFloat(this.availableUpdateVersion);
        ReplacerService.log("Build ver:" + currentVer + " new ver:" + newVer + " isUpd:" + (newVer > currentVer));
        return newVer > currentVer;
    }

    public static void setSetting(int settingId, boolean value, Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putBoolean(context.getString(settingId), value);
        edit.apply();
    }
    public static void setSetting(int settingId, String value, Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putString(context.getString(settingId), value);
        edit.apply();
    }
    public static void setSetting(int settingId, int value, Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putInt(context.getString(settingId), value);
        edit.apply();
    }
    public static boolean getSetting(int settingId, boolean defaultVal, Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(context.getString(settingId), defaultVal);
    }
    public static String getSetting(int settingId, String defaultVal, Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(context.getString(settingId), defaultVal);
    }
    public static int getSetting(int settingId, int defaultVal, Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getInt(context.getString(settingId), defaultVal);
    }


    private boolean replaceSettings(AppSettings newSettings) {
        try {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = sharedPrefs.edit();


            if (newSettings.selectedCtrlMod != 0) {
                edit.putString(getString(R.string.setting_control_key), "" + newSettings.selectedCtrlMod);
            }


            if (newSettings.selectedShortCut != 0) {
                edit.putString(getString(R.string.setting_shortcut_key), "" + newSettings.selectedShortCut);
            }
            if (newSettings.autocorrectDirection != null) {
                edit.putString(getString(R.string.setting_autocorrect_direction), "" + newSettings.autocorrectDirection);
            }
            edit.putBoolean(getString(R.string.setting_shortcut_enabled_key), newSettings.isEnabled);
            edit.putBoolean(getString(R.string.setting_key2emulation_enabled), newSettings.isKey2EmulationEnabled);
            edit.putBoolean(getString(R.string.setting_is_show_info), newSettings.isShowInfo);
            edit.putBoolean(getString(R.string.setting_is_replace_alt_enter), newSettings.isReplaceAltEnter);
            edit.putBoolean(getString(R.string.setting_is_auto_correct), newSettings.isAutoCorrect);
            edit.putBoolean(getString(R.string.setting_is_log_to_sd), newSettings.isLogToFile);
            edit.putBoolean(getString(R.string.setting_select_replaced), newSettings.isSelectReplaced);
            if (newSettings.userDict != null) {
                edit.putString(getString(R.string.setting_user_dictionary), TextUtils.join("\n", newSettings.userDict));
            }
            if (newSettings.vibrationPatternRus != null) {
                edit.putString(getString(R.string.setting_vibration_pattern_rus), "" + newSettings.vibrationPatternRus);
            }
            if (newSettings.vibrationPatternEng != null) {
                edit.putString(getString(R.string.setting_vibration_pattern_eng), "" + newSettings.vibrationPatternEng);
            }
            if (newSettings.soundInputRus != null) {
                edit.putString(getString(R.string.setting_sound_input_rus), "" + newSettings.soundInputRus);
            }
            if (newSettings.soundInputEng != null) {
                edit.putString(getString(R.string.setting_sound_input_eng), "" + newSettings.soundInputEng);
            }
            if (newSettings.soundCorrectRus != null) {
                edit.putString(getString(R.string.setting_sound_correct_rus), "" + newSettings.soundCorrectRus);
            }
            if (newSettings.soundCorrectEng != null) {
                edit.putString(getString(R.string.setting_sound_correct_eng), "" + newSettings.soundCorrectEng);
            }
            // Skip as it's temporary
            //whenEnableNotifications = sharedPrefs.getLong(_context.getString(R.string.setting_when_enable_notifications), 0);
            if (newSettings.inputMethod != null) {
                edit.putString(getString(R.string.setting_input_method), "" + newSettings.inputMethod);
            }
            if (newSettings.checkForUpdates != null) {
                edit.putString(getString(R.string.setting_application_updates_check), "" + newSettings.checkForUpdates);
            }
            if (newSettings.corrections != null) {
                this.updateCorrections(newSettings.corrections);
            }


            edit.apply();
            ReplacerService.log("replaceSettings: " + newSettings.version);

            return true;
        } catch (Exception ex) {
            ReplacerService.log("Ex replaceSettings: " + ex.getMessage());
            return false;
        }
    }

    private void updateLinedStringSetting(int settingId, List<String> newPropArray) {
        String newPropArrayStr = TextUtils
                .join("\n", newPropArray)
                .replaceAll("\n{2,}", "\n");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putString(getString(settingId), newPropArrayStr);
        edit.apply();
    }
}