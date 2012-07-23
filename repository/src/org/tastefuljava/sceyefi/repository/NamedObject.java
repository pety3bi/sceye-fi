package org.tastefuljava.sceyefi.repository;

import java.util.HashMap;
import java.util.Map;

public abstract class NamedObject {
    private int id;
    private String code;
    private Map<String,String> titles = new HashMap<String,String>();
    private Map<String,String> descriptions = new HashMap<String,String>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, String> getTitles() {
        return new HashMap<String,String>(titles);
    }

    public void setTitles(Map<String, String> newValue) {
        titles.clear();
        titles.putAll(newValue);
    }

    public String getTitle(String language) {
        return titles.get(language);
    }

    public String setTitle(String language, String newValue) {
        if (newValue == null) {
            return titles.remove(language);
        } else {
            return titles.put(language, newValue);
        }
    }

    public String removeTitle(String language) {
        return titles.remove(language);
    }

    public Map<String, String> getDescriptions() {
        return new HashMap<String,String>(descriptions);
    }

    public void setDescriptions(Map<String, String> newValue) {
        descriptions.clear();
        descriptions.putAll(newValue);
    }

    public String getDescription(String language) {
        return descriptions.get(language);
    }

    public String setDescription(String language, String newValue) {
        if (newValue == null) {
            return descriptions.remove(language);
        } else {
            return descriptions.put(language, newValue);
        }
    }

    public String removeDescription(String language) {
        return descriptions.remove(language);
    }
}
