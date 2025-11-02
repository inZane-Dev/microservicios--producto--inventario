package com.linktic.producto.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linktic.producto.exception.ProductoNotFoundException;
import com.linktic.producto.model.Producto;
import com.linktic.producto.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal; // Import para BigDecimal

// Imports estáticos para los métodos
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

    // Prueba para POST /productos (Crear)
    @Test
    void testCrearProductoEndpoint() throws Exception {
        // --- Arrange ---
        Producto productoInput = new Producto();
        productoInput.setNombre("Test Controller");
        productoInput.setPrecio(new BigDecimal("120.0")); // Corregido
        productoInput.setCantidad(50);

        Producto productoCreado = new Producto();
        productoCreado.setId(1L);
        productoCreado.setNombre("Test Controller");
        productoCreado.setPrecio(new BigDecimal("120.0")); // Corregido
        productoCreado.setCantidad(50);

        when(productoService.crearProducto(any(Producto.class))).thenReturn(productoCreado);

        // --- Act & Assert ---
        mockMvc.perform(post("/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoInput)))
            
            .andExpect(status().isCreated())
            .andExpect(content().contentType("application/hal+json")) // Corregido
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.nombre").value("Test Controller"));
    }

    // Prueba para GET /productos/{id} (Error 404)
    @Test
    void testObtenerProductoPorId_NoEncontrado_Retorna404NotFound() throws Exception {
        // --- Arrange ---
        Long idNoExistente = 99L;
        when(productoService.obtenerProductoPorId(idNoExistente))
            .thenThrow(new ProductoNotFoundException("Producto no encontrado con ID: " + idNoExistente));

        // --- Act & Assert ---
        mockMvc.perform(get("/productos/{id}", idNoExistente))
            .andExpect(status().isNotFound());
    }

    // Prueba para PUT /productos/{id} (Éxito)
    @Test
    void testActualizarProducto_ConApiKey_Retorna200OK() throws Exception {
        // --- Arrange ---
        Long productoId = 1L;

        Producto productoDetalles = new Producto();
        productoDetalles.setNombre("Producto Nuevo");
        productoDetalles.setPrecio(new BigDecimal("123.45"));
        productoDetalles.setCantidad(5);

        Producto productoActualizado = new Producto();
        productoActualizado.setId(productoId);
        productoActualizado.setNombre("Producto Nuevo");
        productoActualizado.setPrecio(new BigDecimal("123.45"));
        productoActualizado.setCantidad(5);

        when(productoService.actualizarProducto(eq(productoId), any(Producto.class))).thenReturn(productoActualizado);
        
        // --- Act & Assert ---
        mockMvc.perform(put("/productos/{id}", productoId)
                .header("SERVICE_API_KEY", "MI_API_KEY_SECRETA_COMPARTIDA") // Con API Key
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoDetalles)))
            
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/hal+json"))
            .andExpect(jsonPath("$.id").value(productoId))
            .andExpect(jsonPath("$.nombre").value("Producto Nuevo"))
            .andExpect(jsonPath("$.precio").value(123.45));
    }

    // Prueba para PUT /productos/{id} (Seguridad 401)
    @Test
    void testActualizarProducto_SinApiKey_Retorna401Unauthorized() throws Exception {
        // --- Arrange ---
        Producto productoDetalles = new Producto();
        productoDetalles.setNombre("Test");
        productoDetalles.setPrecio(new BigDecimal("1.0"));

        // --- Act & Assert ---
        mockMvc.perform(put("/productos/1") // Sin header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoDetalles)))
            
            .andExpect(status().isUnauthorized());
    }

    // Prueba para DELETE /productos/{id} (Seguridad 401)
    @Test
    void testEliminarProducto_SinApiKey_Retorna401Unauthorized() throws Exception {
        // --- Act & Assert ---
        mockMvc.perform(delete("/productos/1")) // Sin header
            .andExpect(status().isUnauthorized());
    }
}