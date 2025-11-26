package com.chatapp;

import com.chatapp.entity.config.AppConfig;
import com.chatapp.entity.constants.Constants;
import com.chatapp.spring.ApplicationContextProvider;
import jakarta.servlet.MultipartConfigElement;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.chatapp"})
@MapperScan(basePackages = {"com.chatapp.mappers"})
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class ChatAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatAppApplication.class, args);
    }

    /**
     * 创建并配置MultipartConfigElement bean，用于处理文件上传配置
     *
     * @return MultipartConfigElement 配置好的多部分配置元素
     */
    @Bean
    @DependsOn("applicationContextProvider")
    public MultipartConfigElement multipartConfigElement() {
        // 获取应用配置信息
        AppConfig appConfig = (AppConfig) ApplicationContextProvider.getBean("appConfig");
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 设置临时文件存储位置
        factory.setLocation(appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP);
        return factory.createMultipartConfig();
    }

}
