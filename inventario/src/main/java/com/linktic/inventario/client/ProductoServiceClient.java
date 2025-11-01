package com.linktic.inventario.client;

import com.linktic.inventario.dto.ProductoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ProductoServiceClient {

    private final RestTemplate restTemplate;

    @Value("${client.producto-service.url}")
    private String productoServiceUrl;

    @Value("${service.api.key}")
    private String serviceApiKey;

    @Retryable(
        value = { RestClientException.class }, 
        maxAttempts = 2, 
        backoff = @Backoff(delay = 1000)
    )
    public ProductoDto obtenerProducto(Long productoId) {
        String url = productoServiceUrl + "/productos/internal/" + productoId;

        try {
            ResponseEntity<ProductoDto> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                new HttpEntity<>(createAuthHeaders()), 
                ProductoDto.class
            );
            return response.getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("Error al obtener producto (reintentos agotados): " + e.getMessage(), e);
        }
    }
    
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("SERVICE_API_KEY", serviceApiKey);
        return headers;
    }
}