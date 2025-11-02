package com.linktic.producto.service.serviceImpl;

import com.linktic.producto.client.InventarioServiceClient;
import com.linktic.producto.dto.InventarioRequestDto;
import com.linktic.producto.exception.ProductoNotFoundException;
import com.linktic.producto.model.Producto;
import com.linktic.producto.repository.ProductoRepository;
import com.linktic.producto.service.ServiceImpl.ProductoServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal; // Import para BigDecimal
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InventarioServiceClient inventarioServiceClient;

    @InjectMocks
    private ProductoServiceImpl productoService;

    // Prueba para "Creación de productos"
    @Test
    void testCrearProducto_LlamaAInventario() {
        // --- Arrange ---
        Producto productoInput = new Producto();
        productoInput.setNombre("Producto Test");
        productoInput.setPrecio(new BigDecimal("100.0")); // Corregido
        productoInput.setCantidad(10);

        Producto productoGuardado = new Producto();
        productoGuardado.setId(1L);
        productoGuardado.setNombre("Producto Test");
        productoGuardado.setPrecio(new BigDecimal("100.0")); // Corregido
        productoGuardado.setCantidad(10);

        when(productoRepository.save(any(Producto.class))).thenReturn(productoGuardado);

        // --- Act ---
        Producto resultado = productoService.crearProducto(productoInput);

        // --- Assert ---
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(productoRepository, times(1)).save(any(Producto.class)); 
        verify(inventarioServiceClient, times(1)).crearRegistroInventario(any(InventarioRequestDto.class));
    }

    // Prueba para "Manejo de errores" y "consistencia"
    @Test
    void testEliminarProducto_FallaSiInventarioFalla() {
        // --- Arrange ---
        Long productoId = 1L;
        when(productoRepository.findById(productoId)).thenReturn(Optional.of(new Producto())); 
        
        doThrow(new RuntimeException("Error de red simulado"))
            .when(inventarioServiceClient).eliminarRegistroInventario(productoId);

        // --- Act & Assert ---
        assertThrows(RuntimeException.class, () -> {
            productoService.eliminarProducto(productoId);
        });

        // --- Assert (Verificación de Mocks) ---
        verify(inventarioServiceClient, times(1)).eliminarRegistroInventario(productoId);
        verify(productoRepository, times(0)).deleteById(productoId); 
    }
    
    // Prueba para "Manejo de errores (Producto no encontrado)"
    @Test
    void testObtenerProductoPorId_NoEncontrado_LanzaExcepcion() {
        // --- Arrange ---
        Long idNoExistente = 99L;
        when(productoRepository.findById(idNoExistente)).thenReturn(Optional.empty());

        // --- Act & Assert ---
        Exception excepcion = assertThrows(ProductoNotFoundException.class, () -> {
            productoService.obtenerProductoPorId(idNoExistente);
        });

        assertEquals("Producto no encontrado con ID: " + idNoExistente, excepcion.getMessage());
        verify(productoRepository, times(1)).findById(idNoExistente);
    }

    // Prueba para la lógica de Actualización (PUT)
    @Test
    void testActualizarProducto_Exitoso() {
        // --- Arrange ---
        Long productoId = 1L;

        Producto productoExistente = new Producto();
        productoExistente.setId(productoId);
        productoExistente.setNombre("Producto Viejo");
        productoExistente.setPrecio(new BigDecimal("10.0"));
        productoExistente.setCantidad(10);

        Producto productoDetalles = new Producto();
        productoDetalles.setNombre("Producto Nuevo");
        productoDetalles.setPrecio(new BigDecimal("20.0"));
        productoDetalles.setCantidad(50);

        when(productoRepository.findById(productoId)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Act ---
        Producto resultado = productoService.actualizarProducto(productoId, productoDetalles);

        // --- Assert ---
        verify(productoRepository, times(1)).findById(productoId);
        verify(productoRepository, times(1)).save(any(Producto.class));
        assertEquals("Producto Nuevo", resultado.getNombre());
        assertEquals(new BigDecimal("20.0"), resultado.getPrecio());
        assertEquals(50, resultado.getCantidad());
        assertEquals(productoId, resultado.getId());
    }
}