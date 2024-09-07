package com.mercadofreejava.mercadofreejava.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mercadofreejava.mercadofreejava.Repositories.UsuarioRepositorio;

import com.mercadofreejava.mercadofreejava.Entities.Usuarios_Java;

@RestController
@RequestMapping("/Usuarios")
public class UsuariosController {
    
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @GetMapping
    public List<Usuarios_Java> obtener_todos_los_Usuarios(){
        return usuarioRepositorio.findAll();
    }

    @PostMapping
    public Usuarios_Java agregar_Usuario(@RequestBody Usuarios_Java usuario_a_agregar){
        return usuarioRepositorio.save(usuario_a_agregar);
    }

    @GetMapping("/{id}")
    public Usuarios_Java buscar_Usuario(@PathVariable Long id){
        return usuarioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("No existe el producto con la id "+id));
    }
    
    @PutMapping("/{id}")
    public Usuarios_Java actualizar_Usuario(@PathVariable Long id, @RequestBody Usuarios_Java datos_del_usuario){
        Usuarios_Java usuario_a_actualizar = usuarioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("No existe el producto con la id "+id));
        usuario_a_actualizar.setNombre_usuario(datos_del_usuario.getNombre_usuario());
        usuario_a_actualizar.setContrasegna_usuario(datos_del_usuario.getContrasegna_usuario());
        usuario_a_actualizar.setEmail_usuario(datos_del_usuario.getEmail_usuario());
        usuario_a_actualizar.setTelefono_usuario(datos_del_usuario.getTelefono_usuario());

        return usuarioRepositorio.save(usuario_a_actualizar);
    }

    @DeleteMapping("/{id}")
    public String eliminar_Usuario(@PathVariable Long id){
        Usuarios_Java usuario_a_eliminar = usuarioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("No existe el producto con la id "+id));
        usuarioRepositorio.delete(usuario_a_eliminar);
        return "Se eliminó el usuario " + usuario_a_eliminar.getNombre_usuario();
    }
}