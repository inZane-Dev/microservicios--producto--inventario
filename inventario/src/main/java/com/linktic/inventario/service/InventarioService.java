package com.linktic.inventario.service;

import com.linktic.inventario.dto.InventarioRequestDto;
import com.linktic.inventario.dto.InventarioResponseDto;
import com.linktic.inventario.model.Inventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventarioService {

    Inventario crearRegistroInventario(InventarioRequestDto requestDto);
    void eliminarRegistroInventarioPorProductoId(Long productoId);
    Inventario obtenerInventarioPorProductoId(Long productoId);
    Inventario procesarCompra(Long productoId, int cantidadComprada);
    Page<InventarioResponseDto> listarInventario(Pageable pageable);
    InventarioResponseDto obtenerInventarioCombinado(Long productoId);
    
}