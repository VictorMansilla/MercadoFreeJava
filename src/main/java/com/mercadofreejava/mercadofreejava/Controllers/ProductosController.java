package com.mercadofreejava.mercadofreejava.Controllers;
import com.mercadofreejava.mercadofreejava.Repositories.ProductoRepositorio;
import com.mercadofreejava.mercadofreejava.Repositories.UsuarioRepositorio;
import com.mercadofreejava.mercadofreejava.tokenjwt.tokenjwt;

import io.jsonwebtoken.Claims;

import com.mercadofreejava.mercadofreejava.Entities.Productos_Java;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/Productos")
public class ProductosController {

    @Autowired
    private ProductoRepositorio productoRepositorio;
    
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @GetMapping
    public List<Productos_Java> obtener_todos_los_Productos(){
        return productoRepositorio.findAll();
    }



    @PostMapping
    public ResponseEntity<?> agregar_Producto(@RequestHeader ("Authorization") String authHeader, @RequestBody Map<String, Object> producto_a_agregar){
        try{
            String producto_nombre = (String)producto_a_agregar.get("producto_nombre");
            Integer producto_precio = (Integer)producto_a_agregar.get("producto_precio");
            String producto_descripcion = (String)producto_a_agregar.get("producto_descripcion");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                tokenjwt tokenjwt = new tokenjwt();
                Claims token_deployado = tokenjwt.Deployar_Token(token);
                Long id_obtenido_del_token = token_deployado.get("id", Long.class);

                if(producto_nombre == null || producto_nombre.isEmpty()){
                    throw new IllegalArgumentException("producto_nombre");
                }
                
                if(producto_precio == null){
                    throw new IllegalArgumentException("producto_precio");
                }
                
                if(producto_precio > 0){
                    Productos_Java nuevo_producto = new Productos_Java();
                    nuevo_producto.setProducto_nombre(producto_nombre);
                    nuevo_producto.setProducto_precio(producto_precio);
                    nuevo_producto.setProducto_descripcion(producto_descripcion);
                    nuevo_producto.setProducto_usuario(id_obtenido_del_token);
                    productoRepositorio.save(nuevo_producto);
                
                    return ResponseEntity.status(HttpStatus.CREATED).body(String.format("Se creo el producto %s del usuario %s", producto_nombre, token_deployado.getSubject()));
                
                }else{
                    return ResponseEntity.status(HttpStatus.OK).body("El precio no puede ser 0 o negativo");
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



    @GetMapping("/{id}")
    public ResponseEntity<?> buscar_Producto(@PathVariable String id){
        try{
            Long idLong = Long.parseLong(id); 
            if(id==null){
                throw new IllegalArgumentException("id");
            }

            if(productoRepositorio.existsById(idLong)){
                Optional<Productos_Java> producto = productoRepositorio.findById(idLong);
                return ResponseEntity.status(HttpStatus.OK).body(producto);

            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No existe el producto con el id "+id);
            }
        
        } catch(NumberFormatException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("El id debe ser un valor entero '%s'", e.getMessage()));
        
        } catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("No se ha enviado el argumento '%s'", e.getMessage()));
        }
    }


    
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar_Producto(@RequestHeader("Authorization") String authHeader, @PathVariable String id, @RequestBody Map<String, Object> datos_del_producto) {
        try{
            String producto_nombre = (String)datos_del_producto.get("producto_nombre");
            Integer producto_precio = (Integer)datos_del_producto.get("producto_precio");
            String producto_descripcion = (String)datos_del_producto.get("producto_descripcion");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                tokenjwt tokenjwt = new tokenjwt();
                Claims token_deployado = tokenjwt.Deployar_Token(token);
                Long id_obtenido_del_token = token_deployado.get("id", Long.class);
                
                if (usuarioRepositorio.existsById(id_obtenido_del_token)) {
                    Long idLong = Long.parseLong(id); 
                
                    if(productoRepositorio.existsById(idLong)){
                        Optional<Productos_Java> producto_a_actualizar = productoRepositorio.findById(idLong);

                        if(!(idLong < 0 || idLong == 0)){
                            producto_a_actualizar.get().setProducto_nombre(producto_nombre != null ? producto_nombre : producto_a_actualizar.get().getProducto_nombre());
                            producto_a_actualizar.get().setProducto_precio(producto_precio != null ? producto_precio : producto_a_actualizar.get().getProducto_precio());
                            producto_a_actualizar.get().setProducto_descripcion(producto_descripcion != null ? producto_descripcion : producto_a_actualizar.get().getProducto_descripcion());
                            productoRepositorio.save(producto_a_actualizar.get());
                            return ResponseEntity.status(HttpStatus.OK).body("Se actualizaron los datos del usuario ");

                        }else{
                            return ResponseEntity.status(HttpStatus.OK).body("El precio no puede ser 0 o negativo");
                        }

                    }else{
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No existe el producto con el id "+id);
                    }
                    
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("El usuario %s no existe", token_deployado.getSubject()));
                }
                
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se envió el token");
            }

        } catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("Datos no enviados en %s",e.getMessage()));
        
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("Ocurrió un error en el servidor: "+ e.getMessage());
        }
    }


    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar_Producto(@RequestHeader("Authorization") String authHeader, @PathVariable Long id){
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                tokenjwt tokenjwt = new tokenjwt();
                Claims token_deployado = tokenjwt.Deployar_Token(token);
                Long id_obtenido_del_token = token_deployado.get("id", Long.class);

                if (usuarioRepositorio.existsById(id_obtenido_del_token)) {

                    if (productoRepositorio.existsById(id)) {
                        Productos_Java producto = productoRepositorio.getById(id);
                        
                        if (producto.getProducto_usuario() == id_obtenido_del_token) {
                            productoRepositorio.deleteById(id);
                            return ResponseEntity.status(HttpStatus.OK).body("Se eliminó el producto");

                        } else {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El id obtenido del token y el id del usuario del producto no son compatibles");
                        }
                    } else{
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("No existe el producto con el id %s", id));
                    }
                }  else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("El usuario %s no existe", token_deployado.getSubject()));
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se envió el token");
            }

        } catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("Datos no enviados en %s",e.getMessage()));
        
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("Ocurrió un error en el servidor: "+ e.getMessage());
        }       
    }
}