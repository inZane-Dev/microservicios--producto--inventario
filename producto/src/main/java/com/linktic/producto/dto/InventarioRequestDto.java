package com.linktic.producto.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InventarioRequestDto {
    private Long productoId;
    private int cantidad;
}