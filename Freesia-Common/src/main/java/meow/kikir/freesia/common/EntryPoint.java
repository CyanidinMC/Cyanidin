package meow.kikir.freesia.common;

import org.slf4j.Logger;

public class EntryPoint {
    public static Logger LOGGER_INST;

    public static void initLogger(Logger logger) {
        LOGGER_INST = logger;
    }
}
