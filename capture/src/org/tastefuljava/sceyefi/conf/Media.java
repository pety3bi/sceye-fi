package org.tastefuljava.sceyefi.conf;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public File folderForDate(Date creationDate) {
        if (!addDate) {
            return folder;
        } else {
            Date date = dateType == DATE_CREATED ? creationDate : new Date();
            String dir = dateFormat().format(date);
            return new File(folder, dir);
        }
    }

    private DateFormat dateFormat() {
        StringBuilder buf = new StringBuilder();
        String fmt = "%d-%m-%Y";
        if (customDateFormat != null && !customDateFormat.isEmpty()) {
            fmt = customDateFormat;
        }
        char[] chars = fmt.toCharArray();
        boolean escape = false;
        for (char c: chars) {
            if (escape) {
                escape = false;
                switch (c) {
                    case 'Y':
                        buf.append("yyyy");
                        break;
                    case 'm':
                        buf.append("MM");
                        break;
                    case 'b':
                        buf.append("MMM");
                        break;
                    case 'B':
                        buf.append("MMMM");
                        break;
                    case 'd':
                        buf.append("dd");
                        break;
                    default:
                        buf.append(c);
                        break;
                }
            } else if (c == '%') {
                escape = true;
            } else {
                buf.append(c);
            }
        }
        return new SimpleDateFormat(buf.toString());
    }
}
