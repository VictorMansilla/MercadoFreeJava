package com.mercadofreejava.mercadofreejava.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Productos_Java {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productoNombre;
    private Integer productoPrecio;
    private String productoPescripcion;
    private Long productoPsuario;

    public Long getId() {
        return id;
    }

    public String getProducto_nombre() {
        return productoNombre;
    }

    public void setProducto_nombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public Integer getProducto_precio() {
        return productoPrecio;
    }

    public void setProducto_precio(Integer productoPrecio) {
        this.productoPrecio = productoPrecio;
    }

    public String getProducto_descripcion() {
        return productoPescripcion;
    }

    public void setProducto_descripcion(String productoPescripcion) {
        this.productoPescripcion = productoPescripcion;
    }

    public Long getProducto_usuario() {
        return productoPsuario;
    }
    
    public void setProducto_usuario(Long productoPsuario) {
        this.productoPsuario = productoPsuario;
    }

    
}
