package com.chatapp.redis;

import com.chatapp.utils.StringTools;
import io.lettuce.core.RedisConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@Slf4j
public class RedisConfig<V> {

    @Value("${spring.data.redis.host:${spring.redis.host:127.0.0.1}}")
    private String redisHost;

    @Value("${spring.data.redis.port:${spring.redis.port:6379}}")
    private Integer redisPort;

    @Value("${spring.data.redis.password:${spring.redis.password:}}")
    private String redisPassword;

    @Bean(name = "redissonClient", destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        try {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort);
            if (!StringTools.isEmpty(redisPassword)) {
                config.useSingleServer().setPassword(redisPassword);
            }
            return Redisson.create(config);
        } catch (RedisConnectionException e) {
            log.error("redis配置错误，请检查redis配置");
        } catch (Exception e) {
            log.error("Redisson 初始化失败", e);
        }
        return null;
    }

    @Bean("redisTemplate")
    public RedisTemplate<String, V> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, V> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.json());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.json());
        template.afterPropertiesSet();
        return template;
    }
}
