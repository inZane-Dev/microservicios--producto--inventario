package com.linktic.producto.service.ServiceImpl;

import com.linktic.producto.client.InventarioServiceClient;
import com.linktic.producto.dto.InventarioRequestDto;
import com.linktic.producto.exception.ResourceNotFoundException;
import com.linktic.producto.model.Producto;
import com.linktic.producto.repository.ProductoRepository;
import com.linktic.producto.service.ProductoService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor 
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final InventarioServiceClient inventarioServiceClient;

    @Override
    @Transactional
    public Producto crearProducto(Producto producto) {
        
        Producto productoGuardado = productoRepository.save(producto);
        
        InventarioRequestDto inventarioDto = new InventarioRequestDto();
        inventarioDto.setProductoId(productoGuardado.getId());
        inventarioDto.setCantidad(productoGuardado.getCantidad());

        inventarioServiceClient.crearRegistroInventario(inventarioDto);
        
        return productoGuardado;
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerProductoPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public Producto actualizarProducto(Long id, Producto productoDetalles) {
        Producto producto = obtenerProductoPorId(id);

        producto.setNombre(productoDetalles.getNombre());
        producto.setPrecio(productoDetalles.getPrecio());
        producto.setCantidad(productoDetalles.getCantidad());
        
        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = obtenerProductoPorId(id);

        inventarioServiceClient.eliminarRegistroInventario(id);
        
        productoRepository.delete(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Producto> listarTodosLosProductos(Pageable pageable) {
        return productoRepository.findAll(pageable);
    }
}