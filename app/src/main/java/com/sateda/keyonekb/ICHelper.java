package com.sateda.keyonekb;

import android.text.InputType;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import by.mkr.blackberry.textlayouttools.AppSettings;
import by.mkr.blackberry.textlayouttools.Language;
import by.mkr.blackberry.textlayouttools.LanguageDetector;
import by.mkr.blackberry.textlayouttools.LayoutConverter;
import by.mkr.blackberry.textlayouttools.ReplaceValues;
import by.mkr.blackberry.textlayouttools.ReplacerService;
import by.mkr.blackberry.textlayouttools.WordWithBoundaries;


public class ICHelper {

    public static final int CHARS_TO_GET = 25;

    private static final int[] _notHandleInputs = {
        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
        InputType.TYPE_CLASS_NUMBER,
        InputType.TYPE_CLASS_DATETIME,
        InputType.TYPE_CLASS_PHONE
    };


    public static int getCursorPos(InputConnection ic) {
        ExtractedText et = ic.getExtractedText(new ExtractedTextRequest(), 0);
        int cursorPos = et.startOffset + et.selectionStart;

        return cursorPos;
    }

    public static void replaceInputText(InputConnection ic, CharSequence fromText, CharSequence toText, int cursorPos, boolean needCorrection) {
        // Replace text
        ic.beginBatchEdit();
        ic.deleteSurroundingText(0, fromText.length());
        ic.commitText(toText, 0);
        int cursorPosCorrected = needCorrection
                ? cursorPos + toText.length() - fromText.length()
                : cursorPos;
        ReplacerService.log("cursorPos:" + cursorPos + "; cursorPosCorrected:" + cursorPosCorrected + "; from:" + fromText.length() + "; to:" + toText.length());
        ic.setSelection(cursorPosCorrected, cursorPosCorrected);
        ic.endBatchEdit();
    }

    public static void markCorrection(InputConnection ic, int startIndex, CharSequence fromText, CharSequence toText) {
        CorrectionInfo ci = new CorrectionInfo(startIndex, fromText, toText);
        ic.commitCorrection(ci);
    }

    public static void setSelection(InputConnection ic, int startIndex, int endIndex) {
        ic.setSelection(startIndex, endIndex);
    }

    public static void setCursor(InputConnection ic, int index) {
        setSelection(ic, index, index);
    }

    public static boolean isReplaceableInput(int inputType) {
        // Check if any of Not Proceeding Types
        for (int notHandling : _notHandleInputs) {
            if (inputType == notHandling) {
                return false;
            }
        }
        return true;
    }

    public static String getReplacedWord(String enteredText, AppSettings appSettings) {
        Language textEnteredLang = LayoutConverter.getTextLanguage(enteredText, appSettings.inputMethod);
        String newWord = LayoutConverter.getReplacedText(enteredText, textEnteredLang, appSettings.corrections);
        ReplacerService.log(enteredText + " => " + newWord);

        return newWord;
    }

    public static ReplaceValues getReplaceValues(String cursorText,
                                                 int cursorPos,
                                                 Language currentLang,
                                                 AppSettings appSettings
    ) {
        int nearestWordEnd = LanguageDetector.getNearestWordEnd(
                cursorText,
                cursorPos,
                currentLang);
        ReplacerService.log("nearestWordEnd: " + nearestWordEnd);
        WordWithBoundaries currentWord = LanguageDetector.getWordAtPosition(cursorText, nearestWordEnd, currentLang);
        ReplacerService.log("word at position: " + currentWord.Word + " [" + currentWord.Begin + "," + currentWord.End + "]");

        String newWord = getReplacedWord(currentWord.Word, appSettings);

        return new ReplaceValues(currentWord, newWord);
    }

    public static void PerformReplace(InputConnection ic,
                                      int cursorPos,
                                      int wordStart,
                                      String currentWord,
                                      String newWord
    ) {
        ReplacerService.log("PerformReplace");

        // Move cursor to beginning of the word
        ICHelper.setCursor(ic, wordStart);

        // Replace text
        boolean needCorrection = (cursorPos > wordStart) && (currentWord.length() != newWord.length()); // if cursor within the word or at the end AND has spared letters
        ICHelper.replaceInputText(ic, currentWord, newWord, cursorPos, needCorrection);

        // Mark correction
        ICHelper.markCorrection(ic, wordStart, currentWord, newWord);
    }
}
