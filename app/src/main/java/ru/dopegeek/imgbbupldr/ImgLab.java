package ru.dopegeek.imgbbupldr;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.dopegeek.imgbbupldr.database.DbHelper;
import ru.dopegeek.imgbbupldr.database.DbSchema;
import ru.dopegeek.imgbbupldr.database.ImgCursorWrapper;

public class ImgLab {

    public static ImgLab sImgLab;
    public static File imgFile;
    private final Context mContext;
    private final SQLiteDatabase mDatabase;

    private ImgLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new DbHelper(mContext).getWritableDatabase();
    }

    public static ImgLab get(Context context) {
        if (sImgLab == null) sImgLab = new ImgLab(context);
        return sImgLab;
    }

    private static ContentValues getContentValues(Img img) {
        ContentValues values = new ContentValues();
        values.put(DbSchema.ImgTable.Cols.UUID, img.getId().toString());
        values.put(DbSchema.ImgTable.Cols.DATE, img.getDate() + "");
        values.put(DbSchema.ImgTable.Cols.URL, img.getUrl());
        values.put(DbSchema.ImgTable.Cols.URI, img.getUri().toString());
        values.put(DbSchema.ImgTable.Cols.PATH, img.getPhotoFile().getPath());
        values.put(DbSchema.ImgTable.Cols.TIMER, img.getTimer());
        values.put(DbSchema.ImgTable.Cols.TIMEMILLIS, System.currentTimeMillis());
        return values;
    }

    public void addImg(Img i) {
        ContentValues values = getContentValues(i);
        mDatabase.insert(DbSchema.ImgTable.NAME, null, values);
    }

    public List<Img> getImgs() {
        List<Img> imgs = new ArrayList<>();

        ImgCursorWrapper cursor = queryImgs();
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Img img = cursor.getImg();

                if (img.isNeedToDelete()) ImgLab.get(mContext).deleteImg(img);
                else imgs.add(img);

                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return imgs;
    }


    public void deleteImg(Img img) {

        File photo = new File(img.getPhotoFile().getPath());
        Uri imageUri = FileProvider.getUriForFile(Objects.requireNonNull(mContext), BuildConfig.APPLICATION_ID + ".FileProvider", photo);
        ContentResolver contentResolver = mContext.getContentResolver();
        contentResolver.delete(imageUri, null, null);

        mDatabase.delete(DbSchema.ImgTable.NAME,
                DbSchema.ImgTable.Cols.UUID + "= ?",
                new String[]{String.valueOf(img.getId())});

    }


    private ImgCursorWrapper queryImgs() {
        Cursor cursor = mDatabase.query(
                DbSchema.ImgTable.NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
        return new ImgCursorWrapper(cursor);
    }

    public File getPhotoFile(Img img) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, img.getPhotoFilename());
    }

    public void persistImage(Bitmap bitmap, Img img) {
        imgFile = sImgLab.getPhotoFile(img);
        OutputStream os;
        try {
            os = new FileOutputStream(imgFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }
    }
}
