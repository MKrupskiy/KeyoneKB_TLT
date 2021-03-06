package by.mkr.blackberry.textlayouttools;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import static by.mkr.blackberry.textlayouttools.ReplacerService.LOG_TAG;


public class ReplacerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        CharSequence text = intent
                .getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        boolean readonly = intent
                .getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false);


        InputMethod inputMethod = InputMethod.Qwerty;
        AppSettings appSettings = ReplacerService.getAppSettings();
        if (appSettings != null) {
            inputMethod = appSettings.inputMethod;
        }
        /*
        Boolean isEnabled = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.switch_enabled_preference), true);
        */
        boolean isEnabled = true;

        ReplacerService.log((readonly ? "ReadOnly": "Editable") + "; " + (isEnabled ? "Enabled": "Disabled"));

        if (isEnabled) {
            // process the text
            Language textLanguage = LayoutConverter.getTextLanguage(text, inputMethod);
            String replacedText = LayoutConverter.getReplacedText(text, textLanguage, appSettings.corrections);
            ReplacerService.log("original=" + text + "; replaced=" + replacedText);

            if (!readonly) {
                intent.putExtra(Intent.EXTRA_PROCESS_TEXT, replacedText);
                setResult(RESULT_OK, intent);
            } else {
                Toast.makeText(this, replacedText, Toast.LENGTH_LONG).show();
            }
        }

        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }
}
