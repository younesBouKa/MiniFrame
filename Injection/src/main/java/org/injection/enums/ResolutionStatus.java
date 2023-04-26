package org.injection.enums;

public enum ResolutionStatus {
    START("▶️"),
    SEARCHING("♻️"),
    RESOLVED("✅"),
    REJECTED("❌"),
    NOT_FOUND("🔴");
    private final String emoticon;
    ResolutionStatus(String emoticon){
        this.emoticon = emoticon;
    }
    public String getEmoticon() {
        return emoticon;
    }

    @Override
    public String toString() {
        return getEmoticon()+" "+this.name();
    }
}