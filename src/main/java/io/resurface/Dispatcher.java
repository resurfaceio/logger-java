package io.resurface;

public class Dispatcher extends Thread{

    protected Dispatcher(BaseLogger logger) {
        this(logger, 50 * 1024);
    }

    protected Dispatcher(BaseLogger logger, int threshold) {
        this.logger = logger;
        thresh = threshold;
        buffer = new StringBuilder(thresh);
    }

    public void run() {
        try {
            while (true) {
//                String msg = (String) this.logger.msg_queue.take();
                if ((logger.msg_queue.peek() == null && buffer.length() != 0) || buffer.length() == thresh) {
                    String msg = buffer.toString();
                    buffer = new StringBuilder();
                    logger.dispatch(msg);
                }
                buffer.append(logger.msg_queue.take()).append("\n");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private final BaseLogger logger;
    private StringBuilder buffer;
    private final int thresh;
}
