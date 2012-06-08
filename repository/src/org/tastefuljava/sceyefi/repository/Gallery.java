package org.tastefuljava.sceyefi.repository;

import java.util.HashMap;
import java.util.Map;

public class Gallery extends NamedObject {
    private Map<String,Picture> pics = new HashMap<String,Picture>();
}
