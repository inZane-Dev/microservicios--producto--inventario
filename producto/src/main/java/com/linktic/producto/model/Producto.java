package com.linktic.producto.model;

import com.toedter.spring.hateoas.jsonapi.JsonApiId;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
@Data 
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonApiId 
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, precision = 20, scale = 5) 
    private BigDecimal precio;
    
    @Column(nullable = false)
    private int cantidad; 
}