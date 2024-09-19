package com.mercadofreejava.mercadofreejava.Controllers;
import com.mercadofreejava.mercadofreejava.Repositories.ProductoRepositorio;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;

import com.mercadofreejava.mercadofreejava.Entities.Productos_Java;
import com.mercadofreejava.mercadofreejava.Entities.Usuarios_Java;

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

    private Dotenv dotenv = Dotenv.load();
    
    @Autowired
    private ProductoRepositorio productoRepositorio;
    
    @GetMapping
    public List<Productos_Java> obtener_todos_los_Productos(){
        return productoRepositorio.findAll();
    }

    @PostMapping
    public ResponseEntity<?> agregar_aroducto(@RequestHeader ("Authorization") String authHeader, @RequestBody Map<String, Object> producto_a_agregar){
        try{
            String producto_nombre = (String)producto_a_agregar.get("producto_nombre");
            Integer producto_precio = (Integer)producto_a_agregar.get("producto_precio");
            String producto_descripcion = (String)producto_a_agregar.get("producto_descripcion");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String SECRET_KEY = dotenv.get("SECRET_KEY");
                Claims token_deployado = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
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

        }catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("Datos no enviados en %s",e.getMessage()));

        } catch (UnsupportedJwtException  e) {
            return ResponseEntity.status(HttpStatus.valueOf(400)).body("Error al decodificar el token");

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.valueOf(406)).body("El token a expirado");

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.valueOf(409)).body("Error en la validación del token");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("Ocurrió un error en el servidor");
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
        
        }catch(NumberFormatException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("El id debe ser un valor entero '%s'", e.getMessage()));
        
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("No se ha enviado el argumento '%s'", e.getMessage()));
        }
    }

/*     @GetMapping("/buscar")
    public ResponseEntity<?> buscar_Productos(@RequestBody Map<String, String> query){
        String producto_nombre = (String)query.get("producto_nombre");
        try{
            if(producto_nombre == null || producto_nombre.isEmpty()){
                throw new IllegalArgumentException("producto_nombre");
            }
            List<?> productos = productoRepositorio.buscar_Productos(producto_nombre);
            return ResponseEntity.status(HttpStatus.OK).body(productos);

        }catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("Fallo en la %s, no se mando o no contene nada",e.getMessage()));
        }
    } */
    
    @PutMapping("/{id}")
    public ResponseEntity<String> actualizar_Producto(@PathVariable String id, @RequestBody Map<String, Object> datos_del_producto){
        try{
            String producto_nombre = (String)datos_del_producto.get("producto_nombre");
            Integer producto_precio = (Integer)datos_del_producto.get("producto_precio");
            String producto_descripcion = (String)datos_del_producto.get("producto_descripcion");
            Long producto_usuario = (Long)datos_del_producto.get("producto_usuario");

            //if(producto_nombre == null || producto_nombre.isEmpty()){
            //    throw new IllegalArgumentException("producto_nombre");
            //}

            Long idLong = Long.parseLong(id); 
            if(productoRepositorio.existsById(idLong)){
                Optional<Productos_Java> producto_a_actualizar = productoRepositorio.findById(idLong);

                if(!(idLong < 0 || idLong == 0)){
                    producto_a_actualizar.get().setProducto_nombre(producto_nombre != null ? producto_nombre : producto_a_actualizar.get().getProducto_nombre());
                    producto_a_actualizar.get().setProducto_precio(producto_precio != null ? producto_precio : producto_a_actualizar.get().getProducto_precio());
                    producto_a_actualizar.get().setProducto_descripcion(producto_descripcion != null ? producto_descripcion : producto_a_actualizar.get().getProducto_descripcion());
                    producto_a_actualizar.get().setProducto_usuario(producto_usuario != null ? producto_usuario : producto_a_actualizar.get().getProducto_usuario());
                    productoRepositorio.save(producto_a_actualizar.get());
                    return ResponseEntity.status(HttpStatus.OK).body("Se actualizaron los datos del usuario ");

                }else{
                    return ResponseEntity.status(HttpStatus.OK).body("El precio no puede ser 0 o negativo");
                }

            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No existe el producto con el id "+id);
            }

        }catch(NumberFormatException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("El id debe ser un valor entero '%s'", e.getMessage()));
        
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format("Datos no enviados en %s",e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar_Producto(@PathVariable Long id){
        if(productoRepositorio.existsById(id)){
            productoRepositorio.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body("Se eliminó el producto");
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("No existe el producto con el id %s", id));
        }
    }
}