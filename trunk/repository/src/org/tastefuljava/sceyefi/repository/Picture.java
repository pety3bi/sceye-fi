package org.tastefuljava.sceyefi.repository;

import java.util.HashSet;
import java.util.Set;

public class Picture extends NamedObject {
    private Set<Tag> tags = new HashSet<Tag>();
}
