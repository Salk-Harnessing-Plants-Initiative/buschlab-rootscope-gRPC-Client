package gui.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public class LogHandler extends Handler {
    private Log logQueue;

    public LogHandler(Log logQueue) {
        LogManager manager = LogManager.getLogManager();
        String className = this.getClass().getName();
        String level = manager.getProperty(className + ".level");
        setLevel(level != null ? Level.parse(level) : Level.INFO);

        this.logQueue = logQueue;
    }

    public Log getLogQueue(){
        return logQueue;
    }

    @Override
    public void setLevel(Level level){
        super.setLevel(level);
    }

    @Override
    public void publish(final LogRecord logRecord) {
        logRecord.getSourceClassName();
        logRecord.getSourceMethodName();
        if (isLoggable(logRecord))
            logQueue.offer(logRecord);
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}
}
