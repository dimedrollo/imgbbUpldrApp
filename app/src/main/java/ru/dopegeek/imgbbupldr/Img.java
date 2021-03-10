package ru.dopegeek.imgbbupldr;

import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import static ru.dopegeek.imgbbupldr.parameter.ExpirationTimeFragment.expTimeValue;

public class Img {
    final String TAG = "Img timing: ";
    private final UUID mId;
    private File mPhotoFile;
    private String mUrl;
    private Uri mUri;
    private String mDate;
    private Long mTimer; // сюда сохраняем время удаления
    private boolean needToDelete;

    public Img(String u, Uri b) {
        this.mId = UUID.randomUUID();
        this.mDate = ImageUtil.makeFormattedDate(new Date());
        this.mUrl = u;
        setTimer(expTimeValue * 1000 + System.currentTimeMillis());
        Log.i(TAG, "Таймер - " + this.getTimer() + "Текущее время - " + System.currentTimeMillis());
        this.needToDelete = isNeedToDelete();
        this.mUri = b;
    }

    public Img(UUID id) {
        mId = id;
        mDate = ImageUtil.makeFormattedDate(new Date());
    }

    public void setNeedToDelete() {
        this.needToDelete = mTimer < System.currentTimeMillis();
    }

    public boolean isNeedToDelete() {
        return mTimer < System.currentTimeMillis();
    }

    public Long getTimer() {
        return mTimer;
    }

    public void setTimer(Long timer) {
        mTimer = timer;
    }

    public File getPhotoFile() {
        return mPhotoFile;
    }

    public void setPhotoFile(File file) {
        mPhotoFile = file;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(String uri) {
        mUri = Uri.parse(uri);
    }

    public UUID getId() {
        return mId;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getPhotoFilename() {
        return "IMG_" + getId().toString() + ".jpg";
    }
}




