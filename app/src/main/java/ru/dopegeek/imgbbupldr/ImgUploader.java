package ru.dopegeek.imgbbupldr;


import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

import ru.dopegeek.imgbbupldr.parameter.UploadParameters;
import ru.dopegeek.imgbbupldr.response.OptionalResponse;

public class ImgUploader {

    public static final String TAG = "ImgUploader";
    private static final String API_KEY = "fcf64353684651f2f17b71755fb57b6b";

    private static final String API_URL = "https://api.imgbb.com/1/upload";
    private static final String USER_AGENT = "imgbbUpldr";
    private static final int TIMEOUT = 50000;

    /**
     * Uploads an image using received {@link UploadParameters} instance.
     *
     * @param parameters a parameters to do image uploading.
     * @return The {@link OptionalResponse} instance as response of uploading operation.
     * @throws RuntimeException when anyone I/O exception catches.
     */
    public static OptionalResponse upload(UploadParameters parameters) {
        try {
            Connection.Response response = Jsoup.connect(API_URL)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.POST)
                    .data(parameters.toMap())
                    .timeout(TIMEOUT)
                    .userAgent(USER_AGENT)
                    .execute();
            return OptionalResponse.of(response);
        } catch (IOException ex) {
            throw new RuntimeException("I/O exception was catched while try to upload image!", ex);
        }
    }
}
