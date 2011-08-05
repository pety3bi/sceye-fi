package org.tastefuljava.sceyefi.conf;

import java.io.File;
import org.jdom.Element;

public class Media {
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_MOVIE = 2;
    public static final int TYPE_RAW = 4;

    public static final int DATE_UPLOAD = 0;
    public static final int DATE_CREATED = 1;

    private int type;
    private File folder;
    private boolean addDate;
    private int dateType;
    private String customDateFormat;

    Media(Element elm) {
        type = Integer.parseInt(elm.getAttributeValue("Type"));
        folder = new File(elm.getChildText("Folder"));
        addDate = 0 != Integer.parseInt(elm.getChildText("AddDate"));
        dateType = Integer.parseInt(elm.getChildText("DateType"));
        customDateFormat = elm.getChildText("CustomDateFormat");
    }

    public int getType() {
        return type;
    }

    public File getFolder() {
        return folder;
    }

    public boolean getAddDate() {
        return addDate;
    }

    public int getDateType() {
        return dateType;
    }

    public String getCustomDateFormat() {
        return customDateFormat;
    }
}
