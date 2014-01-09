package uk.co.prenderj.trailsrv.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.common.base.Throwables;

/**
 * Handles logging.
 * @author Joshua Prendergast
 */
public class Log {
    private static Logger logger;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm:ssa '('E dd/MM/yy')'");
    
    static {
        logger = Logger.getLogger("trail-srv");
        logger.setLevel(Level.ALL);
        
        // Default behaviour displays a long info line; replace this with a nice timestamp
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord record) {
                Throwable thrown = record.getThrown();
                StringBuilder out = new StringBuilder();
                out.append(dateFormat.format(new Date(record.getMillis()))).append(' ').append(record.getLevel()).append(": ").append(record.getMessage());
                if (thrown != null) {
                    out.append(" <").append(Throwables.getStackTraceAsString(thrown)).append(">");
                }
                out.append(System.lineSeparator());
                return out.toString();
            }
        });
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
    }
    
    public static void w(String message) {
        logger.warning(message);
    }
    
    public static void e(String message) {
        logger.severe(message);
    }
    
    public static void e(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
    
    public static void i(String message) {
        logger.info(message);
    }
    
    public static void v(String message) {
        logger.info(message); // FIXME
    }
}
