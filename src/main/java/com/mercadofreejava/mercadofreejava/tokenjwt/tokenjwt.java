package com.mercadofreejava.mercadofreejava.tokenjwt;

import java.util.Date;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;

public class tokenjwt {
    private Dotenv dotenv = Dotenv.load();

    private String SECRET_KEY = dotenv.get("SECRET_KEY");

    public String Crear_Token(Map<String, Object> data, String Subject) {
        return Jwts.builder()
        .setClaims(data)
        .setSubject(Subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 1))
        .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
        .compact();
    }

    public Claims Deployar_Token (String token) throws Exception {
        try {
            return Jwts.parser()
            .setSigningKey(SECRET_KEY)
            .parseClaimsJws(token)
            .getBody();
        
        } catch (UnsupportedJwtException e) {
            throw new Exception("Error al decodificar el token");
        
        } catch (ExpiredJwtException e) {
            throw new Exception("El token a expirado");
        
        } catch (JwtException e) {
            throw new Exception("Error en la validación del token");
        }
    }
}