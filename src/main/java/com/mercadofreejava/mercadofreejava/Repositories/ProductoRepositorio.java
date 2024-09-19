package com.mercadofreejava.mercadofreejava.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadofreejava.mercadofreejava.Entities.Productos_Java;

public interface ProductoRepositorio extends JpaRepository<Productos_Java, Long>{
/*     @Query(value = "SELECT * FROM de Productos_Java WHERE LOWER (nombre_usuario) LIKE LOWER (CONcAT('%', :nombre_usuario, '%'))", nativeQuery = true)
    List<Productos_Java> buscar_Productos(@Param("nombre_usuario")String nombre_usuario); */
}
