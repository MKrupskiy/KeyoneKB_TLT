package by.mkr.blackberry.textlayouttools;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import static by.mkr.blackberry.textlayouttools.ReplacerService.LOG_TAG;

public class WordsList {

    private Language _lang;
    private HashMap<Integer, ArrayList<String>> _words;

    public WordsList(Context ctx, Language lang) {
        _words = new HashMap<Integer, ArrayList<String>>();
        _lang = lang;

        AssetManager assetManager = ctx.getAssets();
        // To get names of all files inside the "Files" folder
        try {
            String[] files = assetManager.list("Files");

            for (int i=0; i<files.length; i++) {
                Log.d(LOG_TAG, files[i]);
            }

            String fileToOpen;
            switch (lang) {
                case Ru:
                case RuTrans:
                case RuFull:
                case RuQwertz:
                case RuFxtecPro1: {
                    fileToOpen = "Files/Ru.txt";
                    break;
                }
                case En:
                case EnTrans:
                case EnFull:
                case EnQwertz:
                case EnFxtecPro1: {
                    fileToOpen = "Files/En.txt";
                    break;
                }
                default: {
                    fileToOpen = "";
                    Log.d(LOG_TAG, "No such dictionary: " + lang);
                    break;
                }
            }

            InputStream file = assetManager.open(fileToOpen);
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            String line = reader.readLine();
            // Get Version
            if (line != null) {
                Log.d(LOG_TAG, "   Version: " + line + "; " + fileToOpen);
            }
            while (line != null) {
                //Log.d(LOG_TAG, "   Word: " + line);
                if (!_words.containsKey(line.length())) {
                    _words.put(line.length(), new ArrayList<String>());
                }
                _words.get(line.length()).add(line.toLowerCase());
                line = reader.readLine();
            }

            // List words' count by length
            for (int i : _words.keySet()) {
                Log.d(LOG_TAG, "--- Words in [" + i + "]:" + _words.get(i).size());
            }
        } catch (Exception ex) {
            Log.d(LOG_TAG, "   EX: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public HashMap<Integer, ArrayList<String>> getWords() {
        return _words;
    }

    public boolean contains(String text) {
        Log.d(LOG_TAG, "--- Check Contains " + _lang);
        boolean isFound = false;
        for (int i = text.length(); i > 0; i--) {
            isFound = contains(text, i);
            if (isFound) break;
        }

        return isFound;
    }

    public boolean contains(String text, int length) {
        Log.d(LOG_TAG, "--- Check Contains [" + length + "] " + _lang);
        boolean isFound = false;
        if (_words.get(length) == null) {
            // No words with that length
            return false;
        }
        for (String word : _words.get(length)) {
            if (text.contains(word)) {
                Log.d(LOG_TAG, "--- Word:" + word);
                isFound = true;
                break;
            }
        }

        return isFound;
    }

    public boolean startsWith(String text) {
        Log.d(LOG_TAG, "--- Check Starts With " + _lang);
        boolean isFound = false;
        for (int i = text.length(); i > 0; i--) {
            isFound = startsWith(text, i);
            if (isFound) break;
        }

        return isFound;
    }

    public boolean startsWith(String text, int length) {
        Log.d(LOG_TAG, "--- Check Starts With [" + length + "] " + _lang);
        boolean isFound = false;
        if (_words.get(length) == null) {
            // No words with that length
            return false;
        }
        for (String word : _words.get(length)) {
            if (text.startsWith(word)) {
                Log.d(LOG_TAG, "--- Word:" + word);
                isFound = true;
                break;
            }
        }

        return isFound;
    }
}
