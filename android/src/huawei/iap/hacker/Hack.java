package huawei.iap.hacker;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.huawei.hms.support.api.client.Status;

import java.lang.reflect.Field;

public class Hack {
    public static PendingIntent hackPendingIntent(Status status) {
        PendingIntent pendingIntent = null;
        Field[] fields = status.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.getType().getSimpleName().equalsIgnoreCase("PendingIntent")) {
                try {
                    field.setAccessible(true);
                    pendingIntent = (PendingIntent) field.get(status);
                }  catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        return pendingIntent;
    }

    public static Intent hackIntent(Status status) {
        Intent intent = null;
        Field[] fields = status.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (field.getType().getSimpleName().equalsIgnoreCase("Intent")) {
                try {
                    field.setAccessible(true);
                    intent = (Intent) field.get(status);
                }  catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        return intent;
    }
}
