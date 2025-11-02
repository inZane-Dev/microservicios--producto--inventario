package com.linktic.inventario.service.ServiceImpl;

import com.linktic.inventario.client.ProductoServiceClient;
import com.linktic.inventario.dto.InventarioResponseDto;
import com.linktic.inventario.dto.ProductoDto;
import com.linktic.inventario.exception.InventarioNotFoundException; 
import com.linktic.inventario.exception.StockInsuficienteException; 
import com.linktic.inventario.model.Inventario;
import com.linktic.inventario.repository.InventarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoServiceClient productoServiceClient;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    @Test
    void testObtenerInventarioCombinado_Exitoso() {

        Inventario inventarioMock = new Inventario();
        inventarioMock.setId(1L);
        inventarioMock.setProductoId(10L);
        inventarioMock.setCantidad(100);
        when(inventarioRepository.findByProductoId(10L)).thenReturn(Optional.of(inventarioMock));

        ProductoDto productoMock = new ProductoDto(10L, "Producto Externo", new BigDecimal("99.99"));
        when(productoServiceClient.obtenerProducto(10L)).thenReturn(productoMock);

        InventarioResponseDto resultado = inventarioService.obtenerInventarioCombinado(10L);

        assertNotNull(resultado);
        assertEquals(100, resultado.getCantidad());
        assertEquals("Producto Externo", resultado.getProducto().getNombre());
    }

    @Test
    void testObtenerInventario_NoEncontrado_LanzaExcepcion() {
        when(inventarioRepository.findByProductoId(99L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(InventarioNotFoundException.class, () -> {
            inventarioService.obtenerInventarioPorProductoId(99L);
        });
        assertTrue(ex.getMessage().contains("Inventario no encontrado"));
    }

    @Test
    void testProcesarCompra_StockSuficiente() {
        Long productoId = 1L;
        int cantidadComprada = 10;

        Inventario inventarioMock = new Inventario();
        inventarioMock.setId(1L);
        inventarioMock.setProductoId(productoId);
        inventarioMock.setCantidad(50);
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.of(inventarioMock));

        inventarioService.procesarCompra(productoId, cantidadComprada);

        verify(inventarioRepository, times(1)).save(any(Inventario.class)); 
        assertEquals(40, inventarioMock.getCantidad()); 
    }

    @Test
    void testProcesarCompra_StockInsuficiente_LanzaExcepcion() {
        Long productoId = 1L;
        int cantidadComprada = 100;
        Inventario inventarioMock = new Inventario(1L, productoId, 50);
        when(inventarioRepository.findByProductoId(productoId)).thenReturn(Optional.of(inventarioMock));

        Exception ex = assertThrows(StockInsuficienteException.class, () -> {
            inventarioService.procesarCompra(productoId, cantidadComprada);
        });

        assertEquals("Cantidad solicitada (100) excede el stock disponible (50).", ex.getMessage());
        verify(inventarioRepository, times(0)).save(any(Inventario.class));
    }
}