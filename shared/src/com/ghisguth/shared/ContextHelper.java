package com.ghisguth.shared;

import android.content.Context;
import android.os.Build;

public class ContextHelper {
    public static Context getDeviceProtectedContext(Context context) {
        return getDeviceProtectedContext(context, Build.VERSION.SDK_INT);
    }

    // Visible for testing
    static Context getDeviceProtectedContext(Context context, int sdkInt) {
        if (sdkInt >= Build.VERSION_CODES.N) {
            return context.createDeviceProtectedStorageContext();
        }
        return context;
    }
}
