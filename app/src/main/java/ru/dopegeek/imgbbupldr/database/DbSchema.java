package ru.dopegeek.imgbbupldr.database;

public class DbSchema {

    public static final class ImgTable {
        public static final String NAME = "images";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String DATE = "date";
            public static final String URL = "url";
            public static final String URI = "uri";
            public static final String PATH = "path";
            public static final String TIMER = "timer";
            public static final String TIMEMILLIS = "timemillis";
        }
    }
}
