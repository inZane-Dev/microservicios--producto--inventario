package com.linktic.inventario.controller;

import com.linktic.inventario.dto.InventarioRequestDto;
import com.linktic.inventario.dto.InventarioResponseDto;
import com.linktic.inventario.model.Inventario;
import com.linktic.inventario.service.InventarioService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping(value = "/inventarios", produces = "application/vnd.api+json")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;
    private final PagedResourcesAssembler<InventarioResponseDto> pagedResourcesAssembler;
    
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Inventario> crearRegistroInventario(@RequestBody InventarioRequestDto requestDto) {
        Inventario nuevoInventario = inventarioService.crearRegistroInventario(requestDto);
        return new ResponseEntity<>(nuevoInventario, HttpStatus.CREATED);
    }

    @DeleteMapping("/producto/{productoId}")
    public ResponseEntity<Void> eliminarRegistroInventario(@PathVariable Long productoId) {
        inventarioService.eliminarRegistroInventarioPorProductoId(productoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<EntityModel<InventarioResponseDto>> obtenerInventarioCombinado(
            @PathVariable Long productoId) {
        
        InventarioResponseDto inventarioDto = inventarioService.obtenerInventarioCombinado(productoId);
        return ResponseEntity.ok(toModel(inventarioDto));
    }

    @PutMapping("/producto/{productoId}/compra")
    public ResponseEntity<EntityModel<Inventario>> procesarCompra(
            @PathVariable Long productoId,
            @RequestBody CompraDto compraDto) {
                
        Inventario inventarioActualizado = inventarioService.procesarCompra(productoId, compraDto.getCantidad());
        Link selfLink = linkTo(methodOn(InventarioController.class)
                .obtenerInventarioCombinado(inventarioActualizado.getProductoId())).withSelfRel();
        
        return ResponseEntity.ok(EntityModel.of(inventarioActualizado, selfLink));
    }
    
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<InventarioResponseDto>>> listarInventario(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        
        Page<InventarioResponseDto> pagina = inventarioService.listarInventario(pageable);
        
        PagedModel<EntityModel<InventarioResponseDto>> pagedModel = 
            pagedResourcesAssembler.toModel(pagina, this::toModel);
        
        return ResponseEntity.ok(pagedModel);
    }

    @lombok.Data
    static class CompraDto {
        private int cantidad;
    }

    private EntityModel<InventarioResponseDto> toModel(InventarioResponseDto inventarioDto) {
        
        Link selfLink = linkTo(methodOn(InventarioController.class)
                .obtenerInventarioCombinado(inventarioDto.getProducto().getId())).withSelfRel();
        
        return EntityModel.of(inventarioDto, selfLink);
    }
}