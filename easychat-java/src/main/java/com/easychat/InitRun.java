package com.easychat;

import com.easychat.redis.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

@Component("initRun")
public class InitRun implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(InitRun.class);

    @Resource
    private DataSource dataSource;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            dataSource.getConnection();
            redisUtils.get("test");
            log.info("服务启动成功");
        } catch (SQLException e) {
            log.error("数据库配置错误，请检查数据库配置");
        } catch (RedisConnectionFailureException e) {
            log.error("Redis配置错误，请检查Redis配置");
        } catch (Exception e) {
            log.error("服务启动失败", e);
        }
    }
}
