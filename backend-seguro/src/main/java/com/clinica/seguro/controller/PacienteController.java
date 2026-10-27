package com.clinica.seguro.controller;

import com.clinica.seguro.dto.UsuarioDTO;
import com.clinica.seguro.model.Usuario;
import com.clinica.seguro.repository.UsuarioRepository;
import com.clinica.seguro.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Pacientes corrigidos:
 *  - a listagem e so para admin e nunca inclui a senha (usa DTO);
 *  - o registo forca o perfil "paciente" e guarda a senha com hash (anti Mass Assignment).
 */
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;

    public PacienteController(UsuarioRepository usuarios, PasswordEncoder encoder, TokenService tokenService) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.tokenService = tokenService;
    }

    @GetMapping
    public List<UsuarioDTO> todos(HttpServletRequest req) {
        if (!"admin".equals(req.getAttribute("perfil"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas administradores");
        }
        return usuarios.findAll().stream().map(UsuarioDTO::new).toList();
    }

    @GetMapping("/{id}")
    public UsuarioDTO porId(@PathVariable Long id, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        if (!"admin".equals(req.getAttribute("perfil")) && !userId.equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem acesso");
        }
        return new UsuarioDTO(usuarios.findById(id).orElseThrow());
    }

    // Registo publico (nao passa pelo filtro? passa; ver nota no README).
    @PostMapping
    public Map<String, Object> registar(@RequestBody Usuario corpo) {
        if (corpo.getEmail() == null || corpo.getSenha() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email e senha obrigatorios");
        }
        if (usuarios.findByEmail(corpo.getEmail()) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ja registado");
        }
        Usuario novo = new Usuario(
                corpo.getNome(),
                corpo.getEmail(),
                encoder.encode(corpo.getSenha()),
                "paciente"   // CORRIGIDO: perfil forcado, ignora o que vem do cliente
        );
        novo = usuarios.save(novo);
        return Map.of("ok", true, "id", novo.getId(), "token", tokenService.gerar(novo.getId(), "paciente"));
    }
}
