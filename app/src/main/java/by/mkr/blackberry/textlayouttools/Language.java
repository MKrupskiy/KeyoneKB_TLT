package by.mkr.blackberry.textlayouttools;

public enum Language {
    Unknown,
    En,
    Ru,
    RuTrans,
    EnTrans,
    RuFull,
    EnFull,
    RuQwertz,
    EnQwertz,
    Ukr,
    RuFxtecPro1,
    EnFxtecPro1;

    public static String getDefault() {
        return Unknown.toString();
    }

    public static Language fromString(String x) {
        switch (x) {
            case "Unknown":
                return Unknown;
            case "En":
                return En;
            case "Ru":
                return Ru;
            case "EnTrans":
                return EnTrans;
            case "RuTrans":
                return RuTrans;
            case "EnFull":
                return EnFull;
            case "RuFull":
                return RuFull;
            case "EnQwertz":
                return EnQwertz;
            case "RuQwertz":
                return RuQwertz;
            case "Ukr":
                return Ukr;
            case "RuFxtecPro1":
                return RuFxtecPro1;
            case "EnFxtecPro1":
                return EnFxtecPro1;
        }
        return null;
    }

    public static Language getByInputMethod(Language lang /*Ru/En*/, InputMethod inputMethod) {
        switch (lang) {
            case Ru:
            case RuTrans:
            case RuFull:
            case RuQwertz:
            case RuFxtecPro1: {
                switch (inputMethod) {
                    case Qwerty:
                        return Ru;
                    case Translit:
                        return RuTrans;
                    case UsbKb:
                        return RuFull;
                    case Qwertz:
                        return RuQwertz;
                    case FxtecPro1:
                        return RuFxtecPro1;
                }
            }
            case En:
            case EnTrans:
            case EnFull:
            case EnQwertz:
            case EnFxtecPro1: {
                switch (inputMethod) {
                    case Qwerty:
                        return En;
                    case Translit:
                        return EnTrans;
                    case UsbKb:
                        return EnFull;
                    case Qwertz:
                        return EnQwertz;
                    case FxtecPro1:
                        return EnFxtecPro1;
                }
            }
        }
        return Unknown;
    }

    public boolean isRus() {
        return this == Language.Ru || this == Language.RuTrans || this == Language.RuFull || this == Language.RuQwertz;
    }
    public boolean isEng() {
        return this == Language.En || this == Language.EnTrans || this == Language.EnFull || this == Language.EnQwertz;
    }
    public Language getOpposite() {
        switch (this) {
            case En:
                return Ru;
            case Ru:
                return En;
            case EnTrans:
                return RuTrans;
            case RuTrans:
                return EnTrans;
            case EnFull:
                return RuFull;
            case RuFull:
                return EnFull;
            case EnQwertz:
                return RuQwertz;
            case RuQwertz:
                return EnQwertz;
            default:
                return Unknown;
        }
    }
}
