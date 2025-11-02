package com.linktic.inventario.controller;

import com.linktic.inventario.dto.InventarioRequestDto;
import com.linktic.inventario.dto.InventarioResponseDto;
import com.linktic.inventario.model.Inventario;
import com.linktic.inventario.service.InventarioService;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@RequestMapping(value = "/inventarios")
@RequiredArgsConstructor
@Tag(name = "API de Inventario", description = "Endpoints para la gestión de inventario")
public class InventarioController {

    private final InventarioService inventarioService;
    private final PagedResourcesAssembler<InventarioResponseDto> pagedResourcesAssembler;

    @Operation(summary = "Crear un registro de inventario (Interno)",
               description = "Endpoint interno llamado por producto-service cuando se crea un producto. Requiere API Key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inventario creado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Inventario> crearRegistroInventario(@RequestBody InventarioRequestDto requestDto) {
        Inventario nuevoInventario = inventarioService.crearRegistroInventario(requestDto);
        return new ResponseEntity<>(nuevoInventario, HttpStatus.CREATED);
    }

    @Operation(summary = "Eliminar un registro de inventario (Interno)",
               description = "Endpoint interno llamado por producto-service durante el borrado de un producto. Requiere API Key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Inventario eliminado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/producto/{productoId}")
    public ResponseEntity<Void> eliminarRegistroInventario(
            @Parameter(description = "ID del producto cuyo inventario se eliminará")
            @PathVariable Long productoId) {
        
        inventarioService.eliminarRegistroInventarioPorProductoId(productoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener inventario (con datos del producto)",
               description = "Consulta la cantidad de inventario y la combina con los detalles del producto. Requiere API Key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado (API Key inválida)"),
            @ApiResponse(responseCode = "404", description = "Inventario no encontrado")
    })
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<EntityModel<InventarioResponseDto>> obtenerInventarioCombinado(
            @Parameter(description = "ID del producto a consultar")
            @PathVariable Long productoId) {
        
        InventarioResponseDto inventarioDto = inventarioService.obtenerInventarioCombinado(productoId);
        return ResponseEntity.ok(toModel(inventarioDto));
    }

    @Operation(summary = "Procesar una 'compra' de un producto",
               description = "Descuenta una cantidad del inventario de un producto específico. Requiere API Key.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compra procesada, devuelve el estado actualizado del inventario"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "400", description = "Cantidad insuficiente en inventario")
    })
    @PutMapping("/producto/{productoId}/compra")
    public ResponseEntity<EntityModel<InventarioResponseDto>> procesarCompra(
            @Parameter(description = "ID del producto que se está comprando")
            @PathVariable Long productoId,
            @RequestBody CompraDto compraDto) {
                
        inventarioService.procesarCompra(productoId, compraDto.getCantidad());
        
        InventarioResponseDto inventarioActualizadoDto = inventarioService.obtenerInventarioCombinado(productoId);

        return ResponseEntity.ok(toModel(inventarioActualizadoDto));
    }
    
    @Operation(summary = "Listar todo el inventario (paginado)",
               description = "Devuelve una lista paginada de todos los registros de inventario. Requiere API Key.")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<InventarioResponseDto>>> listarInventario(
            @Parameter(hidden = true)
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
        
        Long productoId = (inventarioDto.getProducto() != null) ? inventarioDto.getProducto().getId() : 0L;

        Link selfLink = linkTo(methodOn(InventarioController.class)
                .obtenerInventarioCombinado(productoId)).withSelfRel();
        
        return EntityModel.of(inventarioDto, selfLink);
    }
}