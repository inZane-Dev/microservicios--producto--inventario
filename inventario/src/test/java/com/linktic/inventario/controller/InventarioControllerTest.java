package com.linktic.inventario.controller;

import com.linktic.inventario.config.WebConfig;
import com.linktic.inventario.dto.InventarioResponseDto;
import com.linktic.inventario.dto.ProductoDto;
import com.linktic.inventario.interceptor.ApiKeyAuthInterceptor;
import com.linktic.inventario.service.InventarioService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linktic.inventario.controller.InventarioController.CompraDto;
import com.linktic.inventario.exception.InventarioNotFoundException;
import com.linktic.inventario.model.Inventario;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put; // Import para PUT
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InventarioController.class)
@Import({WebConfig.class, ApiKeyAuthInterceptor.class}) 
class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc; 
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private InventarioService inventarioService;
    
    @Test
    void testObtenerInventarioCombinado_ConApiKey_Retorna200OK() throws Exception {
        Long productoId = 1L;
        ProductoDto p = new ProductoDto(productoId, "Test Producto", new BigDecimal("10.0"));
        InventarioResponseDto dtoMock = new InventarioResponseDto(50, p);
        
        when(inventarioService.obtenerInventarioCombinado(productoId)).thenReturn(dtoMock);

        mockMvc.perform(get("/inventarios/producto/{id}", productoId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("SERVICE_API_KEY", "MI_API_KEY_SECRETA_COMPARTIDA"))
            
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("$.cantidad").value(50)) 
            .andExpect(jsonPath("$.producto.nombre").value("Test Producto"));
    }

    @Test
    void testObtenerInventarioCombinado_SinApiKey_Retorna401Unauthorized() throws Exception {
        mockMvc.perform(get("/inventarios/producto/1")
                .contentType(MediaType.APPLICATION_JSON))
            
            .andExpect(status().isUnauthorized()); 
    }
    
    @Test
    void testProcesarCompra_ConApiKey_Retorna200OK() throws Exception {
        Long productoId = 1L;
        CompraDto bodyRequest = new CompraDto();
        bodyRequest.setCantidad(10);

        ProductoDto p = new ProductoDto(productoId, "Test Producto", new BigDecimal("10.0"));
        InventarioResponseDto dtoMock = new InventarioResponseDto(40, p); 

        when(inventarioService.procesarCompra(anyLong(), anyInt())).thenReturn(new Inventario());
        when(inventarioService.obtenerInventarioCombinado(productoId)).thenReturn(dtoMock);

        mockMvc.perform(put("/inventarios/producto/{id}/compra", productoId)
                .header("SERVICE_API_KEY", "MI_API_KEY_SECRETA_COMPARTIDA")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bodyRequest)))
            
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("$.cantidad").value(40))
            .andExpect(jsonPath("$.producto.nombre").value("Test Producto"));
    }

    @Test
    void testProcesarCompra_SinApiKey_Retorna401Unauthorized() throws Exception {
        CompraDto bodyRequest = new CompraDto();
        bodyRequest.setCantidad(10);

        mockMvc.perform(put("/inventarios/producto/1/compra")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bodyRequest)))
            
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testObtenerInventarioCombinado_NoEncontrado_Retorna404NotFound() throws Exception {
        Long idNoExistente = 99L;

        when(inventarioService.obtenerInventarioCombinado(idNoExistente))
            .thenThrow(new InventarioNotFoundException("Inventario no encontrado"));

        mockMvc.perform(get("/inventarios/producto/{id}", idNoExistente)
                .header("SERVICE_API_KEY", "MI_API_KEY_SECRETA_COMPARTIDA"))
            
            .andExpect(status().isNotFound());
    }
}