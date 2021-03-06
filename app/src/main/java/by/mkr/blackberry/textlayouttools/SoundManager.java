package by.mkr.blackberry.textlayouttools;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.sateda.keyonekb.R;


public class SoundManager {
    private Context _context;

    public SoundManager(Context context) {
        _context = context;
    }

    public void play(SoundPattern pattern) {
        if (pattern == SoundPattern.None || !isSoundOn()) {
            // No need to play sound
            return;
        }
        play(getPatternValue(pattern));
    }

    public void play(int soundResId) {
        SoundManager.play(_context, soundResId);
    }

    public boolean isSoundOn() {
        AudioManager audio = (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);
        boolean isSoundOn = false;

        switch( audio.getRingerMode() ){
            case AudioManager.RINGER_MODE_NORMAL:
                isSoundOn = true;
                //Log.d(LOG_TAG, "Vibration ON: RINGER_MODE_NORMAL");
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                isSoundOn = false;
                //Log.d(LOG_TAG, "Vibration ON: RINGER_MODE_VIBRATE");
                break;
            case AudioManager.RINGER_MODE_SILENT:
                isSoundOn = false;
                //Log.d(LOG_TAG, "Vibration ON: RINGER_MODE_SILENT");
                break;
            default: {
                //Log.d(LOG_TAG, "Vibration ON: UNKNOWN");
                break;
            }
        }
        //Log.d(LOG_TAG, "Sound ON: " + isSoundOn);
        return isSoundOn;
    }

    private static int getPatternValue(SoundPattern pattern) {
        switch (pattern) {
            case En: {
                return R.raw.sound_en_ps;
            }
            case Ru: {
                return R.raw.sound_ru_ps;
            }
            case Switch: {
                return R.raw.sound_switch_ps;
            }
            case Reverse: {
                return R.raw.sound_reverse_ps;
            }
            case Misprint: {
                return R.raw.sound_misprint_ps;
            }
            case ClickXP: {
                return R.raw.sound_click_xp;
            }
            case Exclamation: {
                return R.raw.sound_exclamation;
            }
            case Stop: {
                return R.raw.sound_stop;
            }
            default: {
                return R.raw.sound_switch_ps;
            }
        }
    }

    public static void play(Context ctx, SoundPattern pattern) {
        SoundManager.play(ctx, getPatternValue(pattern));
    }

    public static void play(Context ctx, int soundResId) {
        MediaPlayer mp;
        mp = MediaPlayer.create(ctx, soundResId);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                mp.release();
                mp = null;
            }
        });
        mp.start();
    }
}
