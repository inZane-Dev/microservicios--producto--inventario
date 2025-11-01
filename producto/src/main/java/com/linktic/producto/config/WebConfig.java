package com.linktic.producto.config;

import com.linktic.producto.interceptor.ApiKeyAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ApiKeyAuthInterceptor apiKeyAuthInterceptor;

@Override
    public void addInterceptors(InterceptorRegistry registry) {
        
        registry.addInterceptor(apiKeyAuthInterceptor)
                .addPathPatterns("/productos/**")
                .excludePathPatterns("/productos"); 
    }
}