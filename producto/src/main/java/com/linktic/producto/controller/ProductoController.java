package com.linktic.producto.controller;

import com.linktic.producto.model.Producto;
import com.linktic.producto.service.ProductoService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/productos", produces = "application/vnd.api+json")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final PagedResourcesAssembler<Producto> pagedResourcesAssembler;

    @PostMapping
    public ResponseEntity<EntityModel<Producto>> crearProducto(@RequestBody Producto producto) {
        Producto nuevoProducto = productoService.crearProducto(producto);
        return new ResponseEntity<>(toModel(nuevoProducto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> obtenerProductoPorId(@PathVariable Long id) {
        Producto producto = productoService.obtenerProductoPorId(id);
        return ResponseEntity.ok(toModel(producto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> actualizarProducto(@PathVariable Long id, @RequestBody Producto productoDetalles) {
        Producto productoActualizado = productoService.actualizarProducto(id, productoDetalles);
        return ResponseEntity.ok(toModel(productoActualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Producto>>> listarProductos(
        @PageableDefault(size = 20) Pageable pageable) {      

        Page<Producto> pagina = productoService.listarTodosLosProductos(pageable);        
        PagedModel<EntityModel<Producto>> pagedModel = pagedResourcesAssembler.toModel(pagina, this::toModel);
        
        return ResponseEntity.ok(pagedModel);
    }

    private EntityModel<Producto> toModel(Producto producto) {
        Link selfLink = linkTo(methodOn(ProductoController.class)
                .obtenerProductoPorId(producto.getId())).withSelfRel();
        
        return EntityModel.of(producto, selfLink);
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<Producto> obtenerProductoInterno(@PathVariable Long id) {
        Producto producto = productoService.obtenerProductoPorId(id);
        return ResponseEntity.ok(producto);
    }
}