package com.clinica.seguro.config;

import com.clinica.seguro.model.Consulta;
import com.clinica.seguro.model.Usuario;
import com.clinica.seguro.repository.ConsultaRepository;
import com.clinica.seguro.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Semeia os dados de exemplo com as senhas JA EM HASH (BCrypt).
 * So corre se a tabela estiver vazia.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final ConsultaRepository consultas;
    private final PasswordEncoder encoder;

    public DataSeeder(UsuarioRepository usuarios, ConsultaRepository consultas, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.consultas = consultas;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() > 0) return;

        Usuario ana = usuarios.save(new Usuario("Ana Costa", "ana@exemplo.pt", encoder.encode("ana123"), "paciente"));
        Usuario bruno = usuarios.save(new Usuario("Bruno Dias", "bruno@exemplo.pt", encoder.encode("bruno123"), "paciente"));
        usuarios.save(new Usuario("Admin Clinica", "admin@exemplo.pt", encoder.encode("admin123"), "admin"));

        consultas.save(nova(ana.getId(), "Ana Costa", "2026-11-03 14:30", "Limpeza", "Dra. Marta", "Sensibilidade no dente 26.", "MARCADA"));
        consultas.save(nova(ana.getId(), "Ana Costa", "2026-09-10 09:00", "Destartarizacao", "Dra. Marta", "Tudo normal.", "REALIZADA"));
        consultas.save(nova(bruno.getId(), "Bruno Dias", "2026-11-05 16:00", "Canal", "Dr. Paulo", "Dor aguda no dente 36. Alergia a penicilina.", "MARCADA"));
    }

    private Consulta nova(Long pid, String nome, String data, String proc, String dentista, String obs, String estado) {
        Consulta c = new Consulta();
        c.setPacienteId(pid);
        c.setNomePaciente(nome);
        c.setData(data);
        c.setProcedimento(proc);
        c.setDentista(dentista);
        c.setObservacoes(obs);
        c.setEstado(estado);
        return c;
    }
}
