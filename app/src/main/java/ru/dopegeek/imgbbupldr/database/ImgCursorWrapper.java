package ru.dopegeek.imgbbupldr.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.io.File;
import java.util.UUID;

import ru.dopegeek.imgbbupldr.Img;

public class ImgCursorWrapper extends CursorWrapper {

    public ImgCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Img getImg() {

        String uuidString = getString(getColumnIndex(DbSchema.ImgTable.Cols.UUID));
        String date = getString(getColumnIndex(DbSchema.ImgTable.Cols.DATE));
        String url = getString(getColumnIndex(DbSchema.ImgTable.Cols.URL));
        String uri = getString(getColumnIndex(DbSchema.ImgTable.Cols.URI));
        String path = getString(getColumnIndex(DbSchema.ImgTable.Cols.PATH));
        String timer = getString(getColumnIndex(DbSchema.ImgTable.Cols.TIMER));
        long timemillis = getLong(getColumnIndex(DbSchema.ImgTable.Cols.TIMEMILLIS));

        Img img = new Img(UUID.fromString(uuidString));
        img.setDate(date);
        img.setUrl(url);
        img.setUri(uri);
        img.setPhotoFile(new File(path));
        img.setTimer(Long.valueOf(timer));
        img.setNeedToDelete();
        return img;
    }
}
