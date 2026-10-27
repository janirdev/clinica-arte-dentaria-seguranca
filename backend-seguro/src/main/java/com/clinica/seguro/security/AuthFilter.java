package com.clinica.seguro.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * CORRIGIDO: exige um token valido em todos os /api/** (excepto o login).
 * Coloca o id e o perfil autenticados como atributos do pedido, para os
 * controllers poderem decidir a autorizacao no SERVIDOR.
 */
public class AuthFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuthFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();
        String metodo = req.getMethod();

        // Endpoints publicos: preflight CORS, login e o registo de novo paciente.
        boolean registoPublico = "POST".equalsIgnoreCase(metodo) && path.equals("/api/pacientes");
        if ("OPTIONS".equalsIgnoreCase(metodo) || path.equals("/api/auth/login") || registoPublico) {
            chain.doFilter(req, res);
            return;
        }

        String auth = req.getHeader("Authorization");
        String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
        Map<String, Object> dados = tokenService.validar(token);

        if (dados == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            res.setContentType("application/json");
            res.getWriter().write(mapper.writeValueAsString(Map.of("erro", "Nao autenticado")));
            return;
        }

        req.setAttribute("userId", ((Number) dados.get("id")).longValue());
        req.setAttribute("perfil", String.valueOf(dados.get("perfil")));
        chain.doFilter(req, res);
    }
}
