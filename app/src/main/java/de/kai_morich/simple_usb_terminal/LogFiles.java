package de.kai_morich.simple_usb_terminal;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.io.OutputStream;

final class LogFiles {
    private static final String PUBLIC_LOGS_DIR = Environment.DIRECTORY_DOWNLOADS + "/SimpleUsbTerminal/logs";

    private LogFiles() {
    }

    static boolean usesPublicLogs() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    static File getExternalLogsDir(Context context) {
        File baseDir = context.getExternalFilesDir("logs");
        if (baseDir != null && (baseDir.exists() || baseDir.mkdirs())) {
            return baseDir;
        }
        return null;
    }

    static File getLogsDir(Context context) {
        File baseDir = getExternalLogsDir(context);
        if (baseDir == null) {
            baseDir = new File(context.getFilesDir(), "logs");
        }
        if (baseDir.exists() || baseDir.mkdirs()) {
            return baseDir;
        }
        return null;
    }

    static Uri createLogUri(Context context, String fileName) {
        if (!usesPublicLogs()) {
            return null;
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, PUBLIC_LOGS_DIR);
        return context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
    }

    static OutputStream openLogStream(Context context, Uri uri, boolean append) throws java.io.IOException {
        ContentResolver resolver = context.getContentResolver();
        OutputStream stream = resolver.openOutputStream(uri, append ? "wa" : "w");
        if (stream == null) {
            throw new java.io.IOException("open output stream failed");
        }
        return stream;
    }

    static void deleteLogUri(Context context, Uri uri) {
        if (uri != null) {
            context.getContentResolver().delete(uri, null, null);
        }
    }

    static String getLogLocation(Context context, String fileName) {
        if (usesPublicLogs()) {
            return PUBLIC_LOGS_DIR + "/" + fileName;
        }
        File directory = getLogsDir(context);
        if (directory == null) {
            return fileName;
        }
        return new File(directory, fileName).getAbsolutePath();
    }

    static Uri getLogsDirectoryUri() {
        String documentId = "primary:" + PUBLIC_LOGS_DIR.replaceFirst("^" + Environment.DIRECTORY_DOWNLOADS, "Download");
        return DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", documentId);
    }
}
