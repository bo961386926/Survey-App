package com.qhiot.survey.config;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

/**
 * 雪花ID生成器配置
 * 生成19位长整数ID，具有以下特点：
 * 1. 趋势递增，在MySQL InnoDB引擎下插入性能高
 * 2. 不依赖第三方服务，本地生成，效率高
 * 3. 分布式环境下保证唯一性
 * 
 * 雪花ID结构（64位）：
 * - 第1位：符号位，始终为0
 * - 第2-42位：时间戳（41位），约69年
 * - 第43-52位：机器ID（10位），可支持1024个节点
 * - 第53-64位：序列号（12位），每节点每毫秒可生成4096个ID
 */
class SnowflakeIdGenerator implements IdentifierGenerator {

    /**
     * 开始时间戳：2024-01-01 00:00:00
     * 用于计算时间戳差值
     */
    private static final long EPOCH = 1704067200000L;
    
    /**
     * 机器ID位数（5位）
     */
    private static final long WORKER_ID_BITS = 5L;
    
    /**
     * 数据中心ID位数（5位）
     */
    private static final long DATACENTER_ID_BITS = 5L;
    
    /**
     * 最大机器ID（2^5 - 1 = 31）
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    
    /**
     * 最大数据中心ID（2^5 - 1 = 31）
     */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    
    /**
     * 序列号位数（12位）
     */
    private static final long SEQUENCE_BITS = 12L;
    
    /**
     * 机器ID左移位数
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    
    /**
     * 数据中心ID左移位数
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    
    /**
     * 时间戳左移位数
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    
    /**
     * 序列号掩码（2^12 - 1 = 4095）
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    
    /**
     * 工作机器ID（单实例部署，使用0）
     */
    private final long workerId = 0;
    
    /**
     * 数据中心ID（单实例部署，使用0）
     */
    private final long datacenterId = 0;
    
    /**
     * 序列号
     */
    private long sequence = 0L;
    
    /**
     * 上次生成ID时的时间戳
     */
    private long lastTimestamp = -1L;
    
    /**
     * 实例初始化标记
     */
    private boolean initialized = false;

    /**
     * 初始化
     */
    private void init() {
        if (!initialized) {
            initialized = true;
        }
    }

    @Override
    public Number nextId(Object entity) {
        init();
        return nextId();
    }

    /**
     * 生成下一个雪花ID
     * @return 雪花ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上次生成ID的时间，说明系统时钟回拨
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", 
                    lastTimestamp - timestamp));
        }

        // 如果是同一时间生成的，则进行序列号自增
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 序列号溢出（同一毫秒内序列号用完）
            if (sequence == 0) {
                // 阻塞到下一个毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，序列号重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 计算雪花ID
        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }
}
