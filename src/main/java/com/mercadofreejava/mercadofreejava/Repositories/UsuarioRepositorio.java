package com.mercadofreejava.mercadofreejava.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercadofreejava.mercadofreejava.Entities.Usuarios_Java;

public interface UsuarioRepositorio extends JpaRepository<Usuarios_Java, Long>{

    Optional<Usuarios_Java> findByNombreUsuario(String nombreUsuario);

    Boolean existsByNombreUsuario(String nombreUsuario);

}
