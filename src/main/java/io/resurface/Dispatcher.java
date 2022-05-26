package io.resurface;

public class Dispatcher extends Thread{

    protected Dispatcher(BaseLogger logger) {
        this.logger = logger;
    }

    public void run() {
        try {
            while (true) {
                String msg = (String) this.logger.msg_queue.take();
                this.logger.submit(msg);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private final BaseLogger logger;
}
