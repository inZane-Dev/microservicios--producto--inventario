package com.linktic.inventario.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthInterceptor implements HandlerInterceptor {

    @Value("${service.api.key}")
    private String serviceApiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        String providedKey = request.getHeader("SERVICE_API_KEY");

        if (serviceApiKey.equals(providedKey)) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "API Key inválida o faltante");
        return false;
    }
}