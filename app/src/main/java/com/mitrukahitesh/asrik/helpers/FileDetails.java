/*
    Used to get the display name of file the user selects
 */

package com.mitrukahitesh.asrik.helpers;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.mitrukahitesh.asrik.models.file.FileMetaData;

import java.io.File;

public class FileDetails {

    /**
     * Takes the Uri of the file and extracts the metadata (name, size and path)
     */
    @SuppressLint("Range")
    public static FileMetaData getFileMetaData(Context context, Uri uri) {
        FileMetaData fileMetaData = new FileMetaData();
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            File file = new File(uri.getPath());
            fileMetaData.displayName = file.getName();
            fileMetaData.size = file.length();
            fileMetaData.path = file.getPath();
            return fileMetaData;
        } else {
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            fileMetaData.mimeType = contentResolver.getType(uri);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    fileMetaData.displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    if (!cursor.isNull(sizeIndex))
                        fileMetaData.size = cursor.getLong(sizeIndex);
                    else
                        fileMetaData.size = -1;
                    try {
                        fileMetaData.path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                    } catch (Exception e) {
                        Log.i("Asrik", e.getMessage());
                    }
                    return fileMetaData;
                }
            } catch (Exception e) {
                Log.i("Asrik", e.getMessage());
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            return null;
        }
    }
}
