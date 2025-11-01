package com.linktic.inventario.service.ServiceImpl;

import com.linktic.inventario.client.ProductoServiceClient;
import com.linktic.inventario.dto.InventarioRequestDto;
import com.linktic.inventario.dto.InventarioResponseDto;
import com.linktic.inventario.dto.ProductoDto;
import com.linktic.inventario.exception.ResourceNotFoundException;
import com.linktic.inventario.model.Inventario;
import com.linktic.inventario.repository.InventarioRepository;
import com.linktic.inventario.service.InventarioService;

import lombok.RequiredArgsConstructor;
import net.logstash.logback.argument.StructuredArguments;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoServiceClient productoServiceClient;
    
    private static final Logger log = LoggerFactory.getLogger(InventarioServiceImpl.class);

    @Override
    @Transactional(readOnly = true)
    public InventarioResponseDto obtenerInventarioCombinado(Long productoId) {
        
        Inventario inventario = obtenerInventarioPorProductoId(productoId);
        
        ProductoDto productoDto;
        try {
            productoDto = productoServiceClient.obtenerProducto(productoId);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("No se pudo obtener la información del producto con id: " + productoId);
        }

        return new InventarioResponseDto(
            inventario.getId(), 
            inventario.getCantidad(), 
            productoDto
        );
    }

    @Override
    @Transactional
    public Inventario crearRegistroInventario(InventarioRequestDto requestDto) {

        Inventario inventario = new Inventario();
        inventario.setProductoId(requestDto.getProductoId());
        inventario.setCantidad(requestDto.getCantidad());

        log.info(
            "Evento de inventario", 
            StructuredArguments.entries(Map.of(
                "productoId", inventario.getProductoId(),
                "nuevaCantidad", inventario.getCantidad()
            ))
        );
        return inventarioRepository.save(inventario);
    }

    @Override
    @Transactional
    public void eliminarRegistroInventarioPorProductoId(Long productoId) {
        Inventario inventario = obtenerInventarioPorProductoId(productoId);
        inventarioRepository.deleteByProductoId(productoId);
        emitirEventoInventario("Inventario Eliminado", inventario);
    }

    @Override
    @Transactional(readOnly = true)
    public Inventario obtenerInventarioPorProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado para el productoId: " + productoId));
    }

    @Override
    @Transactional
    public Inventario procesarCompra(Long productoId, int cantidadComprada) {
        Inventario inventario = obtenerInventarioPorProductoId(productoId);
        return inventarioRepository.save(inventario);
    }

@Override
    @Transactional(readOnly = true)
    public Page<InventarioResponseDto> listarInventario(Pageable pageable) {
        Page<Inventario> paginaInventarios = inventarioRepository.findAll(pageable);
        List<InventarioResponseDto> listaCombinada = paginaInventarios.getContent().stream()
                .map(inventario -> {
                    ProductoDto productoDto = null;
                    try {
                        productoDto = productoServiceClient.obtenerProducto(inventario.getProductoId());
                    } catch (RuntimeException e) {
                        productoDto = new ProductoDto();
                        productoDto.setId(inventario.getProductoId());
                        productoDto.setNombre("Producto no disponible");
                    }
                    
                    return new InventarioResponseDto(
                        inventario.getId(), 
                        inventario.getCantidad(), 
                        productoDto
                    );
                })
                .collect(Collectors.toList());
        return new PageImpl<>(listaCombinada, pageable, paginaInventarios.getTotalElements());
    }
    
    private void emitirEventoInventario(String tipoEvento, Inventario inventario) {}
}