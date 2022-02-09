package by.mkr.blackberry.textlayouttools;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;
import com.sateda.keyonekb.R;
import org.jetbrains.annotations.NotNull;
import java.util.Calendar;
import java.util.Date;


public class LanguageNotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_MUTE_SWITCH = "by.mkr.blackberry.textlayouttools.layoutnotification.handlers.action.MUTE_SWITCH";
    public static final String ACTION_MANUAL_SWITCH = "by.mkr.blackberry.textlayouttools.layoutnotification.handlers.action.MANUAL_SWITCH";
    public static final String ACTION_AUTOCORRECT_SWITCH = "by.mkr.blackberry.textlayouttools.layoutnotification.handlers.action.AUTOCORRECT_SWITCH";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        ReplacerService.log("onReceive Action: " + action);

        switch (action) {
            case ACTION_MUTE_SWITCH: {
                boolean soundEnabled = !getIsSoundEnabled();
                updateMuted(context, soundEnabled ? -1 : Integer.MAX_VALUE);
                showOffToUser(context, soundEnabled ? -1 : Integer.MAX_VALUE);
                break;
            }
            case ACTION_MANUAL_SWITCH: {
                boolean isManualChange = !getIsManualChange();
                updateIsManualChange(context, isManualChange);
                showOnOffManualChangeToUser(context, isManualChange);
                break;
            }
            case ACTION_AUTOCORRECT_SWITCH: {
                boolean isAutocorrect = !getIsAutocorrect();
                updateIsAutocorrect(context, isAutocorrect);
                showOnOffAutocorrectToUser(context, isAutocorrect);
                break;
            }
            default: {
                break;
            }
        }
    }


    public static NotificationCompat.Action createNotificationAction(@NotNull Context context, @NotNull String actionString) {
        int requestCode = 1001;
        String text = "";
        int drawableId = R.drawable.ic_open_in_new_24dp;


        switch (actionString) {
            case LanguageNotificationReceiver.ACTION_MUTE_SWITCH: {
                requestCode = 1004;
                text = (getIsSoundEnabled()
                    ? context.getString(R.string.text_btn_on_check)
                    : context.getString(R.string.text_btn_off_check))
                    + " " + context.getString(R.string.text_btn_mute_switch);
                drawableId = R.drawable.ic_open_in_new_24dp;
                break;
            }
            case LanguageNotificationReceiver.ACTION_MANUAL_SWITCH: {
                requestCode = 1005;
                text = (getIsManualChange()
                        ? context.getString(R.string.text_btn_on_check)
                        : context.getString(R.string.text_btn_off_check))
                        + " " + context.getString(R.string.text_btn_manual_change_switch);
                drawableId = R.drawable.ic_open_in_new_24dp;
                break;
            }
            case LanguageNotificationReceiver.ACTION_AUTOCORRECT_SWITCH: {
                requestCode = 1006;
                text = (getIsAutocorrect()
                        ? context.getString(R.string.text_btn_on_check)
                        : context.getString(R.string.text_btn_off_check))
                        + " " + context.getString(R.string.text_btn_autocorrect_switch);
                drawableId = R.drawable.ic_open_in_new_24dp;
                break;
            }
            default: {
                break;
            }
        }
        Intent intent = new Intent(context, LanguageNotificationReceiver.class);
        intent.setAction(actionString);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(drawableId, text, pendingIntent).build();
        return action;
    }


    private static long addHours(int hoursCount) {
        // If in the past
        if (hoursCount < 0) {
            return 0;
        }
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR, hoursCount);
        return now.getTimeInMillis();
    }

    private static void updateMuted(Context context, int hoursCount) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putLong(context.getString(R.string.setting_when_enable_notifications), addHours(hoursCount));
        prefsEditor.apply();
    }

    private static void updateIsAutocorrect(Context context, boolean isActive) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putBoolean(context.getString(R.string.setting_is_auto_correct), isActive);
        prefsEditor.apply();
    }

    private static void updateIsManualChange(Context context, boolean isActive) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putBoolean(context.getString(R.string.setting_shortcut_enabled_key), isActive);
        prefsEditor.apply();
    }

    private static boolean getIsAutocorrect() {
        boolean autocorrectEnabled = true;
        AppSettings appSettings = ReplacerService.getAppSettings();
        if (appSettings != null) {
            autocorrectEnabled = appSettings.isAutoCorrect;
        }
        return autocorrectEnabled;
    }

    private static boolean getIsSoundEnabled() {
        boolean soundEnabled = true;
        AppSettings appSettings = ReplacerService.getAppSettings();
        if (appSettings != null) {
            soundEnabled = appSettings.whenEnableNotifications <= new Date().getTime();
        }
        return soundEnabled;
    }

    private static boolean getIsManualChange() {
        boolean manualChangeEnabled = true;
        AppSettings appSettings = ReplacerService.getAppSettings();
        if (appSettings != null) {
            manualChangeEnabled = appSettings.isEnabled;
        }
        return manualChangeEnabled;
    }

    private static void showOffToUser(Context context, int hours) {
        if (hours <= 0) {
            Toast.makeText(context, context.getString(R.string.text_toast_mute_enable), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, context.getString(R.string.text_toast_mute_disable), Toast.LENGTH_SHORT).show();
        }
    }

    private static void showOnOffAutocorrectToUser(Context context, boolean isActive) {
        String text = isActive
                ? context.getString(R.string.text_toast_autocorrect_enabled)
                : context.getString(R.string.text_toast_autocorrect_disabled);
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    private static void showOnOffManualChangeToUser(Context context, boolean isActive) {
        String text = isActive
                ? context.getString(R.string.text_toast_manual_change_enabled)
                : context.getString(R.string.text_toast_manual_change_disabled);
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
