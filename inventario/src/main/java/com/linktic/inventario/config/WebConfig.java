package com.linktic.inventario.config; // Asegúrate que el paquete sea correcto

import com.linktic.inventario.interceptor.ApiKeyAuthInterceptor; // Importa tu interceptor
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
                .addPathPatterns("/inventarios")
                .addPathPatterns("/inventarios/producto/**");
    }
}