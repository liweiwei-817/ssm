package fun.lww.seckill.service.impl;

import fun.lww.seckill.dao.SeckillDao;
import fun.lww.seckill.dao.SuccessKilledDao;
import fun.lww.seckill.dto.Exposer;
import fun.lww.seckill.dto.SeckillExecution;
import fun.lww.seckill.entity.Seckill;
import fun.lww.seckill.entity.SuccessKilled;
import fun.lww.seckill.enums.SeckillStateEnum;
import fun.lww.seckill.exception.RepeatKillException;
import fun.lww.seckill.exception.SeckillCloseException;
import fun.lww.seckill.exception.SeckillException;
import fun.lww.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by lww on 18/4/1
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    //md5盐值字符串 用户混淆MD5
    private final String slat = "aaksdjkAKSJDLFKOI@#KRJ)(DISJKLFJadfasdfaO";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 5);
    }

    public Seckill getById(Long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill = seckillDao.queryById(seckillId);
        if(seckill == null) {
            return new Exposer(false, seckillId);
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        //系统时间
        Date nowTime = new Date();
        if(nowTime.getTime() < startTime.getTime()
                || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        //转化特定字符串过程 不可逆
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    /**
     * 使用注解控制事务方法的优点：
     * 1：开发团队达成一致约定 明确标注事务方法的编程风格
     * 2：保证事务方法的执行时间尽可能短 不要穿插其他的网络操作RPC/HTTP请求或者剥离到事务方法外部
     * 3：不是所有的方法都需要事务 如只有一条修改操作或只读操作不需要事务
     */
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {
        if(md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data rewrite");
        }
        //秒杀业务逻辑 减库存 和 记录购买行为
        Date nowTime = new Date();

        try {
            //减库存
            int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
            if (updateCount <= 0) {
                //没有更新到记录
                throw new SeckillCloseException("seckill is closed");
            } else {
                //记录购买行为
                int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
                //唯一 seckillId userPhone
                if (insertCount <= 0) {
                    //重复秒杀
                    throw new RepeatKillException("seckill repeated");
                } else {
                    //秒杀成功
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e) {
            throw  e;
        } catch (RepeatKillException e) {
            throw  e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //所有编译期异常 转化为运行时异常
            throw new SeckillException("seckill inner error:"+e.getMessage());
        }
    }
}