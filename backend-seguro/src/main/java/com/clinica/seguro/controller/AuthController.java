package com.clinica.seguro.controller;

import com.clinica.seguro.model.Usuario;
import com.clinica.seguro.repository.UsuarioRepository;
import com.clinica.seguro.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Login corrigido:
 *  - senha validada contra o HASH BCrypt;
 *  - token assinado e com expiracao;
 *  - mensagem de erro GENERICA (sem enumeracao de utilizadores).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final TokenService tokenService;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder encoder, TokenService tokenService) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        Map<String, Object> resposta = new HashMap<>();
        Usuario u = usuarios.findByEmail(body.get("email"));

        if (u == null || !encoder.matches(body.getOrDefault("senha", ""), u.getSenha())) {
            resposta.put("ok", false);
            resposta.put("erro", "Credenciais invalidas"); // mensagem unica
            return resposta;
        }

        resposta.put("ok", true);
        resposta.put("token", tokenService.gerar(u.getId(), u.getPerfil()));
        resposta.put("id", u.getId());
        resposta.put("nome", u.getNome());
        resposta.put("perfil", u.getPerfil());
        return resposta;
    }
}
