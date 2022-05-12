package util;

public final class Logger {
    private Logger() {

    }

    public static void log(Object aMessage) {
        System.out.println(aMessage);
    }
}
