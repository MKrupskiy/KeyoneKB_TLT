package by.mkr.blackberry.textlayouttools;

import android.content.Context;
import android.media.AudioManager;
import android.os.Vibrator;


public class VibrationManager {
    // Delay 0ms, Vibrate for 500ms, pause for 500ms, then start again
    private static final long[] NONE_PATTERN = { 0, 0 };
    private static final long[] SINGLE_PATTERN = { 0, 120 };
    private static final long[] SINGLE_SHORT_PATTERN = { 0, 80 };
    private static final long[] SINGLE_LONG_PATTERN = { 0, 200 };
    private static final long[] DOUBLE_PATTERN = { 0, 120, 40, 120 };
    private static final long[] DOUBLE_SHORT_PATTERN = { 0, 80, 40, 80 };
    private static final long[] TRIPLE_PATTERN = { 0, 120, 20, 120, 20, 120 };
    private static final long[] TRIPLE_SHORT_PATTERN = { 0, 80, 20, 80, 20, 80 };
    private static final long[] SHORT_LONG_PATTERN = { 0, 80, 20, 200 };

    private Vibrator _vibrator;
    private Context _context;

    public VibrationManager(Context context) {
        _context = context;
        _vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }


    public void vibrate(VibrationPattern pattern) {
        if (pattern == VibrationPattern.None || !isVibrationOn()) {
            // No need to vibrate
            return;
        }

        //deprecated in API 26
        _vibrator.vibrate(getPatternValues(pattern), -1);
    }


    public boolean hasAmplitudeControl() {
        return false;
    }

    public boolean isVibrationOn() {
        AudioManager audio = (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);
        boolean isVibroOn = false;

        switch( audio.getRingerMode() ){
            case AudioManager.RINGER_MODE_NORMAL:
                isVibroOn = true;
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                isVibroOn = true;
                break;
            case AudioManager.RINGER_MODE_SILENT:
                isVibroOn = false;
                break;
            default:
                break;
        }
        return isVibroOn;
    }

    private long[] getPatternValues(VibrationPattern pattern) {
        switch (pattern) {
            case Single: {
                return SINGLE_PATTERN;
            }
            case SingleShort: {
                return SINGLE_SHORT_PATTERN;
            }
            case SingleLong: {
                return SINGLE_LONG_PATTERN;
            }
            case Double: {
                return DOUBLE_PATTERN;
            }
            case DoubleShort: {
                return DOUBLE_SHORT_PATTERN;
            }
            case Triple: {
                return TRIPLE_PATTERN;
            }
            case TripleShort: {
                return TRIPLE_SHORT_PATTERN;
            }
            case ShortLong: {
                return SHORT_LONG_PATTERN;
            }
            default: {
                return NONE_PATTERN;
            }
        }
    }
}
