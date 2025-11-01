package com.linktic.producto.service;

import com.linktic.producto.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductoService {
    
    Producto crearProducto(Producto producto);
    
    Producto obtenerProductoPorId(Long id);
    
    Producto actualizarProducto(Long id, Producto productoDetalles);
    
    void eliminarProducto(Long id);
    
    Page<Producto> listarTodosLosProductos(Pageable pageable);
    
}