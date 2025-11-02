package com.linktic.producto.client;

import com.linktic.producto.dto.InventarioRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class InventarioServiceClient {

    private final RestTemplate restTemplate;

    @Value("${client.inventario-service.url}")
    private String inventarioServiceUrl;

    @Value("${service.api.key}")
    private String serviceApiKey;

    @Retryable(
        value = { RestClientException.class }, 
        maxAttempts = 2, 
        backoff = @Backoff(delay = 1000)
    )
    @PostMapping(value = "/inventarios", headers = {"${api.key.header}=${api.key.value}"})
    public void crearRegistroInventario(InventarioRequestDto requestDto) {
        String url = inventarioServiceUrl + "/inventarios";
        
        try {
            restTemplate.postForEntity(url, new HttpEntity<>(requestDto, createAuthHeaders()), Void.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al crear inventario (reintentos agotados): " + e.getMessage(), e);
        }
    }

    @Retryable(
        value = { RestClientException.class }, 
        maxAttempts = 2, 
        backoff = @Backoff(delay = 1000)
    )
    @DeleteMapping(value = "/inventarios/producto/{productoId}", headers = {"${api.key.header}=${api.key.value}"})
    public void eliminarRegistroInventario(Long productoId) {
        String url = inventarioServiceUrl + "/inventarios/producto/" + productoId;
        
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(createAuthHeaders()), Void.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al borrar inventario (reintentos agotados): " + e.getMessage(), e);
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("SERVICE_API_KEY", serviceApiKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}