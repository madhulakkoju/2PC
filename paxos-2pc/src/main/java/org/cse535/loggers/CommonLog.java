package org.cse535.loggers;

public class CommonLog {

    public static LogUtils logger = new LogUtils( "common_log", 0);

    public static void log(String message) {
        logger.log(message);
    }

}
