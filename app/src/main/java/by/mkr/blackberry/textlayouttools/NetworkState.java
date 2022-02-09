package by.mkr.blackberry.textlayouttools;

public enum NetworkState {
    Any,
    WiFi,
    Mobile,
    None;

    public static NetworkState fromString(String x) {
        switch (x) {
            case "Any":
                return Any;
            case "WiFi":
                return WiFi;
            case "Mobile":
                return Mobile;
            case "None":
                return None;
            default:
                return null;
        }
    }

    public static String getDefault() {
        return WiFi.toString();
    }

    public boolean isOn() {
        return this != None;
    }
}
