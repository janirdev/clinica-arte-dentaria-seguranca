package com.clinica.seguro.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CORRIGIDO: limitacao de pedidos (rate limiting) por IP.
 *
 * Mitiga abuso de recursos / DoS simples: cada IP pode fazer no maximo
 * LIMITE pedidos dentro de uma janela de JANELA_MS. Acima disso, devolve
 * 429 Too Many Requests ate a janela reiniciar.
 *
 * Nota didatica: isto protege contra excesso de pedidos de UMA origem. Um DDoS
 * real (muitas origens) mitiga-se mais acima na pilha (CDN/WAF, limites de
 * infraestrutura), nao so na aplicacao.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LIMITE = 2;          // pedidos permitidos
    private static final long JANELA_MS = 10_000;  // por janela de 10 segundos

    private final Map<String, long[]> contador = new ConcurrentHashMap<>(); // ip -> [inicioJanela, contagem]
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String ip = req.getRemoteAddr();
        long agora = System.currentTimeMillis();

        long[] janela = contador.compute(ip, (k, v) -> {
            if (v == null || agora - v[0] > JANELA_MS) {
                return new long[]{agora, 1}; // nova janela
            }
            v[1]++;
            return v;
        });

        if (janela[1] > LIMITE) {
            long retryMs = JANELA_MS - (agora - janela[0]);
            res.setStatus(429); // Too Many Requests
            res.setHeader("Retry-After", String.valueOf(Math.max(1, retryMs / 1000)));
            res.setContentType("application/json");
            res.getWriter().write(mapper.writeValueAsString(
                    Map.of("erro", "Demasiados pedidos. Tente novamente mais tarde.", "retryAposMs", retryMs)));
            return;
        }

        chain.doFilter(req, res);
    }
}
