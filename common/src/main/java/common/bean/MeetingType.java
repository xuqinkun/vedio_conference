package common.bean;

public enum MeetingType {
    PERSONAL("Personal"),
    BUSINESS("Business"),
    SPEECH("Speech"),
    ;
    private String value;

    MeetingType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
