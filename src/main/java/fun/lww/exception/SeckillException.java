package fun.lww.exception;

/**
 * 秒杀相关业务异常
 * Created by lww on 18/3/30
 */
public class SeckillException extends RuntimeException {

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
