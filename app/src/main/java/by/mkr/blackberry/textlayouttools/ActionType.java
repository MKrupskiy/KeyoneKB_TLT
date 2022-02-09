package by.mkr.blackberry.textlayouttools;

public enum ActionType {
    None,
    AltEnter,
    AltEnterReplace,
    ManualChange,
    AutoChange,
    CtrlSpace;

    public boolean isAuto() {
        if (this == None) return false;
        switch(this) {
            case AutoChange:
                return true;
            default:
                return false;
        }
    }

    public boolean isManual() {
        if (this == None) return false;
        return !isAuto();
    }
}
