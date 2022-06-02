package io.resurface;

public class Dispatcher implements Runnable {

    /**
     * Initialize dispatcher with default NDJSON buffer size (100 kiB).
     * @param logger Resurface logger.
     */
    public Dispatcher(BaseLogger logger) {
        this(logger, 50 * 1024);
    }

    /**
     * Initialize dispatcher using buffer size.
     * @param logger Resurface logger.
     * @param threshold NDJSON buffer max size - flushAndDispatch will be triggered after reaching this point.
     */
    public Dispatcher(BaseLogger logger, int threshold) {
        this.logger = logger;
        this.batchingThreshold = threshold;
        this.buffer = new StringBuilder(this.batchingThreshold + 5 * 1024);
    }

    public void run() {
        try {
            while (true) {
                if (buffer.length() >= batchingThreshold || logger.msg_queue.peek() == null) {
                    flushAndDispatch();
                }
                String msg = (String) logger.msg_queue.take();
                if (msg.equals("POISON PILL")) {
                    flushAndDispatch();
                    break;
                }
                buffer.append(msg).append("\n");
            }
        } catch (InterruptedException e) {
                flushAndDispatch();
        }
    }

    /**
     * Builds message as an NDJSON-formatted string, and dispatches it. Buffer is reset.
     */
    private void flushAndDispatch() {
        if (buffer.length() != 0) {
            String msg = buffer.toString();
            buffer = new StringBuilder();
            logger.dispatch(msg);
        }
    }

    public BaseLogger getLogger() {
        return logger;
    }

    public StringBuilder getNDJSONBuffer() {
        return buffer;
    }

    public int getBatchingThreshold() {
        return batchingThreshold;
    }

    private final BaseLogger logger;
    private StringBuilder buffer;
    private final int batchingThreshold;
}
