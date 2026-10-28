package com.clinica.seguro.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Consola de auditoria - SO para admin (imposto no servidor).
 * Le o registo de auditoria da base de dados (triggers em PostgreSQL), que
 * capta todas as alteracoes, incluindo as feitas por um ataque ao sistema.
 */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final JdbcTemplate jdbc;

    public AuditoriaController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String, Object>> listar(HttpServletRequest req) {
        if (!"admin".equals(req.getAttribute("perfil"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas administradores");
        }
        try {
            return jdbc.queryForList(
                "SELECT id, momento, operacao, tabela, registo_id, utilizador_bd, " +
                "       host(ip_cliente) AS ip_cliente, " +
                "       dados_antigos::text AS dados_antigos, " +
                "       dados_novos::text  AS dados_novos " +
                "FROM public.auditoria ORDER BY id DESC LIMIT 200");
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Tabela de auditoria indisponivel. Garanta que a migration V3__auditoria.sql foi aplicada.");
        }
    }
}
