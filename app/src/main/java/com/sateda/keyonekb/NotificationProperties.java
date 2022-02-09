package com.sateda.keyonekb;

public class NotificationProperties {
    public boolean navigationSymbol;
    public boolean fnSymbol;
    public boolean pref_sympad;
    public boolean showSymbol;
    public boolean altShift;
    public boolean altPressAllButtonBig;
    public boolean altPressFirstButtonBig;
    public boolean shiftPressAllButtonBig;
    public int langNum;
    public boolean pref_touch_keyboard;
    public boolean shiftPressFirstButtonBig;

    public boolean isEnabled;
    public boolean isAutoCorrect;
    public long whenEnableNotifications;

    public NotificationProperties(
            boolean _navigationSymbol,
            boolean _fnSymbol,
            boolean _pref_sympad,
            boolean _showSymbol,
            boolean _altShift,
            boolean _altPressAllButtonBig,
            boolean _altPressFirstButtonBig,
            boolean _shiftPressAllButtonBig,
            int _langNum,
            boolean _pref_touch_keyboard,
            boolean _shiftPressFirstButtonBig,

            boolean _isEnabled,
            boolean _isAutoCorrect,
            long _whenEnableNotifications
    ) {
        navigationSymbol = _navigationSymbol;
        fnSymbol = _fnSymbol;
        pref_sympad = _pref_sympad;
        showSymbol = _showSymbol;
        altShift = _altShift;
        altPressAllButtonBig = _altPressAllButtonBig;
        altPressFirstButtonBig = _altPressFirstButtonBig;
        shiftPressAllButtonBig = _shiftPressAllButtonBig;
        langNum = _langNum;
        pref_touch_keyboard = _pref_touch_keyboard;
        shiftPressFirstButtonBig = _shiftPressFirstButtonBig;

        isEnabled = _isEnabled;
        isAutoCorrect = _isAutoCorrect;
        whenEnableNotifications = _whenEnableNotifications;
    }
}
