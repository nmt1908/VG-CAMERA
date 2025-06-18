package com.example.vgcamera;

public class LanguageItem {
    private String label;
    private String value;

    public LanguageItem(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    // Quan trọng: Override toString() để spinner hiện label thay vì kiểu object
    @Override
    public String toString() {
        return label;
    }
}

