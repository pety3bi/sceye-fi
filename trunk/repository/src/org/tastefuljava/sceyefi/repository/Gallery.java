package org.tastefuljava.sceyefi.repository;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Gallery extends NamedObject {
    private Map<String,Picture> pics = new HashMap<String,Picture>();
    private Set<Dimension> imageDimensions = new HashSet<Dimension>();
}
