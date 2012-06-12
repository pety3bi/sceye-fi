package org.tastefuljava.sceyefi.repository;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Gallery extends NamedObject {
    private Gallery model;
    private Map<String,Tag> tags = new HashMap<String,Tag>();
    private Map<String,Picture> pics = new HashMap<String,Picture>();
    private Dimension basePreviewSize = new Dimension(1280, 1280);
    private List<Dimension> previewSizes = new ArrayList<Dimension>();

    public Map<String,Tag> getTags() {
        return new HashMap<String,Tag>(tags);
    }

    public Map<String,Tag> getAllTags() {
        Map<String,Tag> result = new HashMap<String,Tag>();
        addTagsTo(result);
        return result;
    }

    public Map<String,Picture> getPictures() {
        return new HashMap<String,Picture>(pics);
    }

    public Map<String,Picture> getAllPictures() {
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
        result.putAll(pics);
    }
}