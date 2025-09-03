package com.hmall.api.config;

import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class DefaultFeignLog {
    @Bean
    public Logger.Level feignLogLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new RequestInterceptor() {
            public void apply(RequestTemplate template) {
                Long userInfo = UserContext.getUser();
                if (userInfo != null) {
                    template.header("user-info", userInfo.toString());
                }
            }
        };
    }
}
