package by.mkr.blackberry.textlayouttools;

public enum VibrationPattern {
    None,
    Single,
    SingleShort,
    SingleLong,
    Double,
    DoubleShort,
    Triple,
    TripleShort,
    ShortLong;

    public static VibrationPattern fromString(String x) {
        switch (x) {
            case "Single":
                return Single;
            case "SingleShort":
                return SingleShort;
            case "SingleLong":
                return SingleLong;
            case "Double":
                return Double;
            case "DoubleShort":
                return DoubleShort;
            case "TripleShort":
                return TripleShort;
            case "Triple":
                return Triple;
            case "ShortLong":
                return ShortLong;
            default:
                return None;
        }
    }

    public static String getDefault() {
        return SingleShort.toString();
    }
}
