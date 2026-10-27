package com.clinica.controller;

import com.clinica.model.Usuario;
import com.clinica.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API de utilizadores/pacientes.
 *
 * FALHAS INTENCIONAIS (para a aula):
 *  - GET /api/pacientes devolve a lista COMPLETA incluindo a senha em texto simples
 *    (Exposicao de Dados Sensiveis), sem autenticacao.
 *  - POST /api/pacientes permite criar um utilizador com perfil "admin"
 *    escolhido pelo cliente (Mass Assignment / escalada de privilegios).
 */
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final UsuarioRepository usuarioRepository;

    public PacienteController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<Usuario> todos() {
        return usuarioRepository.findAll(); // devolve a senha tambem
    }

    @GetMapping("/{id}")
    public Usuario porId(@PathVariable Long id) {
        return usuarioRepository.findById(id).orElseThrow();
    }

    // Registo: aceita o campo "perfil" vindo do cliente sem o filtrar.
    @PostMapping
    public Usuario criar(@RequestBody Usuario usuario) {
        if (usuario.getPerfil() == null) usuario.setPerfil("paciente");
        return usuarioRepository.save(usuario);
    }
}
