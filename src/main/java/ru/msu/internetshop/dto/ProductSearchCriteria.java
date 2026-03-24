package ru.msu.internetshop.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProductSearchCriteria {

    private String categoryName;
    private String manufacturerName;
    private Map<String, String> attributes = new LinkedHashMap<>();

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = normalize(categoryName);
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = normalize(manufacturerName);
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = new LinkedHashMap<>();
        if (attributes == null) {
            return;
        }
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            addAttribute(entry.getKey(), entry.getValue());
        }
    }

    public void addAttribute(String name, String value) {
        String normalizedName = normalize(name);
        String normalizedValue = normalize(value);
        if (normalizedName == null || normalizedValue == null) {
            throw new IllegalArgumentException("Attribute filter must contain non-empty name and value");
        }
        attributes.put(normalizedName, normalizedValue);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
