package com.clinica.controller;

import com.clinica.model.Usuario;
import com.clinica.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Autenticacao de laboratorio.
 *
 * FALHAS INTENCIONAIS (para a aula):
 *  - O "token" devolvido e previsivel e nao assinado (nao e um JWT verificado).
 *    Na pratica as outras APIs nem sequer o exigem (ver ConsultaController).
 *  - A resposta de login devolve o perfil e o id, e o frontend confia neles no cliente.
 *  - Mensagens de erro distinguem "email nao existe" de "senha errada" (enumeracao de utilizadores).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        Map<String, Object> resposta = new HashMap<>();
        Usuario u = usuarioRepository.findByEmail(body.get("email"));

        if (u == null) {
            resposta.put("ok", false);
            resposta.put("erro", "Email nao registado"); // enumeracao de utilizadores
            return resposta;
        }
        if (!u.getSenha().equals(body.get("senha"))) {
            resposta.put("ok", false);
            resposta.put("erro", "Senha incorreta");
            return resposta;
        }

        // "token" fraco e previsivel - serve so para demonstracao.
        String tokenFraco = "token-" + u.getId() + "-" + u.getPerfil();

        resposta.put("ok", true);
        resposta.put("token", tokenFraco);
        resposta.put("id", u.getId());
        resposta.put("nome", u.getNome());
        resposta.put("perfil", u.getPerfil()); // o cliente confia nisto para mostrar o painel admin
        return resposta;
    }
}
