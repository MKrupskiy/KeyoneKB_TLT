package by.mkr.blackberry.textlayouttools;

public enum SoundPattern {
    None,
    En,
    Ru,
    Switch,
    Reverse,
    Misprint,
    ClickXP,
    Exclamation,
    Stop;

    public static SoundPattern fromString(String x) {
        switch (x) {
            case "En":
                return En;
            case "Ru":
                return Ru;
            case "Switch":
                return Switch;
            case "Reverse":
                return Reverse;
            case "Misprint":
                return Misprint;
            case "ClickXP":
                return ClickXP;
            case "Exclamation":
                return Exclamation;
            case "Stop":
                return Stop;
            default:
                return None;
        }
    }

    public static String getDefault() {
        return Switch.toString();
    }
}
