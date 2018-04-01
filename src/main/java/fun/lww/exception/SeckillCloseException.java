package fun.lww.exception;

/**
 * 秒杀关闭异常
 * Create by lww on 18/3/30
 */
public class SeckillCloseException extends SeckillException{

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
