package huawei.iap.helper;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiRHelper;
import org.appcelerator.titanium.view.TiDrawableReference;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import ti.modules.titanium.filesystem.FileProxy;
import ti.modules.titanium.ui.AttributedStringProxy;
import ti.modules.titanium.ui.UIModule;


public class Utils {
    public static int getR(String path) {
        try {
            return TiRHelper.getResource(path);

        } catch (Exception exc) {
            return -1;
        }
    }

    public static Object getObjectOption(KrollDict options, String key) {
        Object value = null;

        if (options.containsKeyAndNotNull(key)) {
            value = options.get(key);
        }

        return value;
    }

    public static Object getObjectOptionDefault(KrollDict options, String key, Object defaultObj) {
        if (options.containsKeyAndNotNull(key)) {
            defaultObj = options.get(key);
        }

        return defaultObj;
    }

    public static String getStringOption(KrollDict options, String key) {
        String value = null;

        if (options.containsKeyAndNotNull(key)) {
            value = TiConvert.toString(options.get(key), "");
            value = value.trim();
        }

        return value;
    }

    public static String getStringOptionDefault(KrollDict options, String key, String defaultStr) {
        if (options.containsKeyAndNotNull(key)) {
            defaultStr = TiConvert.toString(options.get(key), defaultStr);
            defaultStr = defaultStr.trim();
        }

        return defaultStr;
    }

    public static String[] getArrayOption(KrollDict options, String key) {
        if (options.containsKeyAndNotNull(key)) {
            return options.getStringArray(key);
        } else {
            return null;
        }
    }
}




