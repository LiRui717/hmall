package com.hmall.trade;

import com.hmall.api.config.DefaultFeignLog;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@EnableFeignClients(value = "com.hmall.api.client",defaultConfiguration = DefaultFeignLog.class)
@MapperScan("com.hmall.trade.mapper")
@SpringBootApplication
public class TradeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeServiceApplication.class, args);
    }

}
