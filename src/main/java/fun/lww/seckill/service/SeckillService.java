package fun.lww.seckill.service;

import fun.lww.seckill.dto.Exposer;
import fun.lww.seckill.dto.SeckillExecution;
import fun.lww.seckill.entity.Seckill;
import fun.lww.seckill.exception.RepeatKillException;
import fun.lww.seckill.exception.SeckillCloseException;
import fun.lww.seckill.exception.SeckillException;

import java.util.List;

/**
 * 业务接口 站在使用者的角度设计接口
 * 三个方面：方法定义粒度 参数 返回类型（return 类型/异常）
 * created by lww on 18/3/30
 */
public interface SeckillService {

    /**
     * 查询所有秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(Long seckillId);

    /**
     * 秒杀开启时输出秒杀接口地址
     * 否则输出系统时间和秒杀时间
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作
     * @param seckillId
     * @param userPhone
     * @param md5
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseException
     * @return
     */
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
        throws SeckillException, RepeatKillException, SeckillCloseException;
}
