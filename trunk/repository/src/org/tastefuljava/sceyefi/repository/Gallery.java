package org.tastefuljava.sceyefi.repository;

import java.util.HashMap;
import java.util.Map;

public class Gallery extends NamedObject {
    private Gallery model;
    private Map<String,Tag> tags = new HashMap<String,Tag>();
    private Map<String,Picture> pictures = new HashMap<String,Picture>();

    public Map<String,Tag> getOwnTags() {
        return new HashMap<String,Tag>(tags);
    }

    public Map<String,Tag> getTags() {
        Map<String,Tag> result = new HashMap<String,Tag>();
        addTagsTo(result);
        return result;
    }

    public Map<String,Picture> getOwnPictures() {
        return new HashMap<String,Picture>(pictures);
    }

    public Map<String,Picture> getPictures() {
        Map<String,Picture> result = new HashMap<String,Picture>();
        addPicsTo(result);
        return result;
    }

    private void addTagsTo(Map<String,Tag> map) {
        if (model != null) {
            model.addTagsTo(map);
        }
        map.putAll(tags);
    }

    private void addPicsTo(Map<String, Picture> result) {
        if (model != null) {
            model.addPicsTo(result);
        }
        result.putAll(pictures);
    }
}
