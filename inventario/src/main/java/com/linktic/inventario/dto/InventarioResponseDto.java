package com.linktic.inventario.dto;

import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioResponseDto {

    @JsonApiId
    private Long inventarioId;

    private int cantidad;

    private ProductoDto producto;
}