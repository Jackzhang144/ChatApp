package com.chatapp;

import com.chatapp.redis.RedisUtils;
import com.chatapp.websocket.netty.NettyWebSocketStarter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

@Slf4j
@Component("initRun")
public class InitRun implements ApplicationRunner {

    @Resource
    private DataSource dataSource;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private NettyWebSocketStarter nettyWebSocketStarter;

    @Override
    public void run(ApplicationArguments args) {
        try {
            dataSource.getConnection().close();
            redisUtils.get("test");
            new Thread(nettyWebSocketStarter, "netty-ws-starter").start();
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
