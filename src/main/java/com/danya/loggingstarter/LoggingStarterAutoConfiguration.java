package com.danya.loggingstarter;

public class LoggingStarterAutoConfiguration {

    public static void println(String input) {
        System.out.println("Из градл стартера: " + input);
    }
}
