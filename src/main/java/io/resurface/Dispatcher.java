package io.resurface;

public class Dispatcher extends Thread{

    protected Dispatcher(BaseLogger logger) {
        this(logger, 100);
    }

    protected Dispatcher(BaseLogger logger, int threshold) {
        this.logger = logger;
        this.thresh = threshold * 2;
        this.buffer = new StringBuilder(this.thresh);
    }

    public void run() {
        try {
            while (true) {
//                String msg = (String) this.logger.msg_queue.take();
                this.buffer.append(this.logger.msg_queue.take());
                this.buffer.append("\n");
                if (this.buffer.length() == thresh) {
                    String msg = this.buffer.toString();
                    this.buffer = new StringBuilder();
                    this.logger.dispatch(msg);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private final BaseLogger logger;
    private StringBuilder buffer;
    private final int thresh;
}
