package com.linktic.producto.controller;

import com.linktic.producto.model.Producto;
import com.linktic.producto.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping(value = "/productos")
@RequiredArgsConstructor
@Tag(name = "API de Productos", description = "Endpoints para el CRUD de productos")
public class ProductoController {

    private final ProductoService productoService;
    private final PagedResourcesAssembler<Producto> pagedResourcesAssembler;

    @Operation(summary = "Crear un nuevo producto",
               description = "Crea un producto y, de forma síncrona, crea su registro de inventario con la cantidad inicial.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. datos faltantes)")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Producto>> crearProducto(@RequestBody Producto producto) {
        Producto nuevoProducto = productoService.crearProducto(producto);
        return new ResponseEntity<>(toModel(nuevoProducto), HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener un producto por su ID", 
               description = "Busca un producto por su ID. Protegido por API Key (si se configuró en WebConfig).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> obtenerProductoPorId(
            @Parameter(description = "ID del producto a buscar")
            @PathVariable Long id) {
        
        Producto producto = productoService.obtenerProductoPorId(id);
        return ResponseEntity.ok(toModel(producto));
    }

    @Operation(summary = "Actualizar un producto existente",
               description = "Actualiza los detalles de un producto por su ID. Protegido por API Key (si se configuró en WebConfig).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> actualizarProducto(
            @Parameter(description = "ID del producto a actualizar")
            @PathVariable Long id, 
            @RequestBody Producto productoDetalles) {
        
        Producto productoActualizado = productoService.actualizarProducto(id, productoDetalles);
        return ResponseEntity.ok(toModel(productoActualizado));
    }

    @Operation(summary = "Eliminar un producto (Transaccional)",
               description = "Elimina un producto y, de forma síncrona, elimina su registro de inventario. Protegido por API Key (si se configuró en WebConfig).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto e inventario eliminados"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno (ej. no se pudo contactar al servicio de inventario)"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "ID del producto a eliminar")
            @PathVariable Long id) {
        
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Listar todos los productos (paginado)",
               description = "Devuelve una lista paginada de todos los productos.")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Producto>>> listarProductos(
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {    

        Page<Producto> pagina = productoService.listarTodosLosProductos(pageable);      
        PagedModel<EntityModel<Producto>> pagedModel = pagedResourcesAssembler.toModel(pagina, this::toModel);
        
        return ResponseEntity.ok(pagedModel);
    }

    private EntityModel<Producto> toModel(Producto producto) {
        Link selfLink = linkTo(methodOn(ProductoController.class)
                .obtenerProductoPorId(producto.getId())).withSelfRel();
        
        return EntityModel.of(producto, selfLink);
    }

    @Operation(summary = "Obtener datos crudos del producto (Interno)",
               description = "Endpoint interno llamado por inventario-service. No usar directamente. Requiere API Key.",
               hidden = true)
    @GetMapping("/internal/{id}")
    public ResponseEntity<Producto> obtenerProductoInterno(
            @Parameter(description = "ID del producto a buscar")
            @PathVariable Long id) {
        
        Producto producto = productoService.obtenerProductoPorId(id);
        return ResponseEntity.ok(producto);
    }
}