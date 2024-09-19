package com.mercadofreejava.mercadofreejava.tokenjwt;

import java.util.Date;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class tokenjwt {
    private Dotenv dotenv = Dotenv.load();

    private String SECRET_KEY = dotenv.get("SECRET_KEY");

    public String Crear_Token(Map<String, Object> data, String Subject){
        return Jwts.builder()
        .setClaims(data)
        .setSubject(Subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 1))
        .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
        .compact();
    }
}
