package org.zjh.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zjh
 * @description: TODO
 **/
public class DateUtil {
    public static Date get(String pattern) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date parse = sdf.parse(pattern);
            return parse;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
