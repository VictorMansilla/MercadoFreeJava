package com.mercadofreejava.mercadofreejava.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.mercadofreejava.mercadofreejava.Entities.Usuarios_Java;
import com.mercadofreejava.mercadofreejava.Repositories.UsuarioRepositorio;
import com.mercadofreejava.mercadofreejava.tokenjwt.tokenjwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;

@RestController
@RequestMapping("/Usuarios")
public class UsuariosController {
    
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @GetMapping
    public List<Usuarios_Java> obtener_todos_los_usuarios() {
        return usuarioRepositorio.findAll();
    }



    @PostMapping
    public ResponseEntity<?> agregar_usuario(@RequestBody Map<String, Object> datos_usuario_a_agregar) {
        try{
            String nombreUsuario = (String)datos_usuario_a_agregar.get("nombreUsuario");
            String contrasegnaUsuario = (String)datos_usuario_a_agregar.get("contrasegnaUsuario");
            String emailUsuario = datos_usuario_a_agregar.get("emailUsuario") != null ? (String)datos_usuario_a_agregar.get("emailUsuario") : null;
            String telefonoUsuario = datos_usuario_a_agregar.get("telefonoUsuario") != null ? (String)datos_usuario_a_agregar.get("telefonoUsuario") : null;
            
            if (nombreUsuario == null || nombreUsuario.isEmpty()) {
                throw new IllegalArgumentException("nombreUsuario");
            }

            if (contrasegnaUsuario == null || contrasegnaUsuario.isEmpty()) {
                throw new IllegalArgumentException("contrasegnaUsuario");
            }

            if (!usuarioRepositorio.existsByNombreUsuario(nombreUsuario)) {

                BCryptPasswordEncoder contrasegna_Encoder = new BCryptPasswordEncoder();
                String contrasegna_hasheada = contrasegna_Encoder.encode(contrasegnaUsuario);

                Usuarios_Java usuario = new Usuarios_Java();
                usuario.setNombreUsuario(nombreUsuario);
                usuario.setContrasegnaUsuario(contrasegna_hasheada);
                usuario.setEmailUsuario(emailUsuario);
                usuario.setTelefonoUsuario(telefonoUsuario);

                usuarioRepositorio.save(usuario);

                return ResponseEntity.status(HttpStatus.CREATED).body(String.format("El usuario %s fue creado", nombreUsuario));

            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("El usuario %s ya existe", nombreUsuario));
            }

        }catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.valueOf(406)).body(String.format("token expirado"));
        }
    }
    


    @PostMapping("/validar_usuario")
    public ResponseEntity<?> validar_usuario(@RequestBody Map<String, Object> datos_usuario) {
        try {
            String nombreUsuario = (String)datos_usuario.get("nombreUsuario");
            String contrasegnaUsuario = (String)datos_usuario.get("contrasegnaUsuario");
            
            if (nombreUsuario == null || nombreUsuario.isEmpty()) {
                throw new IllegalArgumentException("nombreUsuario");
            }

            if (contrasegnaUsuario == null || contrasegnaUsuario.isEmpty()) {
                throw new IllegalArgumentException("contrasegnaUsuario");
            }

            if (usuarioRepositorio.existsByNombreUsuario(nombreUsuario)) {
                Optional<Usuarios_Java> usuario = usuarioRepositorio.findByNombreUsuario(nombreUsuario);

                BCryptPasswordEncoder contrasegna_Encoder = new BCryptPasswordEncoder();

                boolean validar_contrasegna = contrasegna_Encoder.matches(contrasegnaUsuario, usuario.get().getContrasegnaUsuario());

                if (validar_contrasegna) {
                    tokenjwt tokenjwt = new tokenjwt();
            
                    Map<String, Object> data = new HashMap<>();
                
                    data.put("id", usuario.get().getId());
    
                    String token = tokenjwt.Crear_Token(data, usuario.get().getNombreUsuario());
    
                    return ResponseEntity.status(HttpStatus.OK).body(String.format("Usuario válido, token: %s", token));    
                } else {
                    return ResponseEntity.status(HttpStatus.LOCKED).body("Contrasegna incorrecta");
                }
            

            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("El usuario %s no existe", nombreUsuario));
            }      

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("Datos no enviados en %s",e.getMessage()));
        }
    }



    @GetMapping("/buscar_usuario")
    public ResponseEntity<?> buscar_usuario(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                tokenjwt tokenjwt = new tokenjwt();
                Claims token_deployado = tokenjwt.Deployar_Token(token);
                Long id_obtenido_del_token = token_deployado.get("id", Long.class);

                if (usuarioRepositorio.existsById(id_obtenido_del_token)) {
                    Usuarios_Java usuario = usuarioRepositorio.getById(id_obtenido_del_token);
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(String.format("{'id' : %s, 'nombreUsuario' : %s, 'emailUsuario' : %s, 'telefonoUsuario' : %s}", usuario.getId(), usuario.getNombreUsuario(), usuario.getEmailUsuario(), usuario.getTelefonoUsuario()));
                    
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("El usuario %s no existe", token_deployado.getSubject()));
                }
                
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se envió el token");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("Ocurrió un error en el servidor: "+ e.getMessage());
        } 
    }
    


    @PutMapping("/actualizar_usuario")
    public ResponseEntity<?> actualizar_usuario(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, Object> nuevos_datos_usuario) {
        try {
            String contrasegnaUsuario = (String)nuevos_datos_usuario.get("contrasegnaUsuario");
            
            String nueva_contrasegnaUsuario = (String)nuevos_datos_usuario.get("nueva_contrasegnaUsuario");

            String nuevo_nombreUsuario = (String)nuevos_datos_usuario.get("nuevo_nombreUsuario");
            String nuevo_emailUsuario = (String)nuevos_datos_usuario.get("nuevo_emailUsuario");
            String nuevo_telefonoUsuario = (String)nuevos_datos_usuario.get("nuevo_telefonoUsuario");

            if (contrasegnaUsuario == null || contrasegnaUsuario.isEmpty()) {
                throw new IllegalArgumentException("contrasegnaUsuario");
            }

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                tokenjwt tokenjwt = new tokenjwt();
                Claims token_deployado = tokenjwt.Deployar_Token(token);
                Long id_obtenido_del_token = token_deployado.get("id", Long.class);

                if (usuarioRepositorio.existsById(id_obtenido_del_token)) {
                    
                    Usuarios_Java usuario = usuarioRepositorio.getById(id_obtenido_del_token);
                    
                    BCryptPasswordEncoder contrasegna_Encoder = new BCryptPasswordEncoder();
                    
                    boolean validar_contrasegna = contrasegna_Encoder.matches(contrasegnaUsuario, usuario.getContrasegnaUsuario());

                    if (validar_contrasegna) {

                        if (nueva_contrasegnaUsuario != null ) {
                            String nueva_contrasegnaUsuario_hasheada = contrasegna_Encoder.encode(nueva_contrasegnaUsuario);
                            usuario.setContrasegnaUsuario(nueva_contrasegnaUsuario_hasheada);
                        }

                        if (nuevo_nombreUsuario != null && !usuarioRepositorio.existsByNombreUsuario(nuevo_nombreUsuario)) {
                            usuario.setNombreUsuario(nuevo_nombreUsuario);
                            
                        } else {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("El usuario %s ya existe, no se puede actualizar con ese nombre", nuevo_nombreUsuario));
                        }

                        usuario.setEmailUsuario(nuevo_emailUsuario != null ? nuevo_emailUsuario : usuario.getEmailUsuario());
                        usuario.setTelefonoUsuario(nuevo_telefonoUsuario != null ? nuevo_telefonoUsuario : usuario.getTelefonoUsuario());

                        usuarioRepositorio.save(usuario);

                        return ResponseEntity.status(HttpStatus.ACCEPTED).body(String.format("Hecho, se actualizaron los datos del usuario %s", usuario.getNombreUsuario()));
 
                    } else {
                        return ResponseEntity.status(HttpStatus.LOCKED).body("Contrasegna incorrecta");
                    }

                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("El usuario %s no existe", token_deployado.getSubject()));
                }

            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se envió el token");
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("Datos no enviados en %s",e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("Ocurrió un error en el servidor: "+ e.getMessage());
        }
    }



    @DeleteMapping("/eliminar_usuario")
    public ResponseEntity<?> eliminar_usuario(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, Object> datos_usuario_eliminar) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                tokenjwt tokenjwt = new tokenjwt();
                Claims token_deployado = tokenjwt.Deployar_Token(token);
                
                String contrasegnaUsuario = (String)datos_usuario_eliminar.get("contrasegnaUsuario");
            
                if (contrasegnaUsuario == null || contrasegnaUsuario.isEmpty()) {
                    throw new IllegalArgumentException("contrasegnaUsuario");
                }

                Long id_obtenido_del_token = token_deployado.get("id", Long.class);

                if (usuarioRepositorio.existsById(id_obtenido_del_token)) {
                    Usuarios_Java usuario = usuarioRepositorio.getById(id_obtenido_del_token);
                    
                    BCryptPasswordEncoder contrasegna_Encoder = new BCryptPasswordEncoder();
                    
                    boolean validar_contrasegna = contrasegna_Encoder.matches(contrasegnaUsuario, usuario.getContrasegnaUsuario());
                    
                    if (validar_contrasegna) {
                        usuarioRepositorio.delete(usuario);
                        return ResponseEntity.status(HttpStatus.ACCEPTED).body(String.format("Hecho, se eliminó el usuario %s", usuario.getNombreUsuario()));
                        
                    } else {
                        return ResponseEntity.status(HttpStatus.LOCKED).body("Contrasegna incorrecta");
                    }

                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("El usuario %s no existe", token_deployado.getSubject()));
                }
                
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se envió el token");
            }
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("Datos no enviados en %s",e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("Ocurrió un error en el servidor: "+ e.getMessage());
        }
    }
}