package org.tastefuljava.sceyefi.repository;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Gallery extends NamedObject {
    private Map<String,Picture> pics = new HashMap<String,Picture>();
    private Dimension basePreviewSize = new Dimension(1280, 1280);
    private List<Dimension> previewSizes = new ArrayList<Dimension>();
}
