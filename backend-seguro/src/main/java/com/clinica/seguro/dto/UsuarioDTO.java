package com.clinica.seguro.dto;

import com.clinica.seguro.model.Usuario;

/** CORRIGIDO: expoe o utilizador SEM a senha/hash. */
public class UsuarioDTO {
    public Long id;
    public String nome;
    public String email;
    public String perfil;

    public UsuarioDTO(Usuario u) {
        this.id = u.getId();
        this.nome = u.getNome();
        this.email = u.getEmail();
        this.perfil = u.getPerfil();
    }
}
