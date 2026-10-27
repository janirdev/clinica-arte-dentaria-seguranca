package com.clinica.seguro.controller;

import com.clinica.seguro.model.Consulta;
import com.clinica.seguro.model.Usuario;
import com.clinica.seguro.repository.ConsultaRepository;
import com.clinica.seguro.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Consultas corrigidas. O id/perfil vem do token (atributos do pedido),
 * nunca do corpo ou da URL. A autorizacao e feita no SERVIDOR.
 */
@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private static final Set<String> ESTADOS = Set.of("MARCADA", "REALIZADA", "CANCELADA");

    private final ConsultaRepository consultas;
    private final UsuarioRepository usuarios;

    public ConsultaController(ConsultaRepository consultas, UsuarioRepository usuarios) {
        this.consultas = consultas;
        this.usuarios = usuarios;
    }

    private Long userId(HttpServletRequest req) { return (Long) req.getAttribute("userId"); }
    private boolean isAdmin(HttpServletRequest req) { return "admin".equals(req.getAttribute("perfil")); }

    // So admin ve tudo.
    @GetMapping
    public List<Consulta> todas(HttpServletRequest req) {
        if (!isAdmin(req)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas administradores");
        return consultas.findAll();
    }

    // CORRIGIDO (anti-IDOR): so o proprio dono (ou admin) ve o historico.
    @GetMapping("/paciente/{pacienteId}")
    public List<Consulta> porPaciente(@PathVariable Long pacienteId, HttpServletRequest req) {
        if (!isAdmin(req) && !userId(req).equals(pacienteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nao pode ver consultas de outro paciente");
        }
        return consultas.findByPacienteId(pacienteId);
    }

    // CORRIGIDO: o dono e sempre o utilizador autenticado; estado validado.
    @PostMapping
    public Consulta marcar(@RequestBody Consulta dados, HttpServletRequest req) {
        Usuario u = usuarios.findById(userId(req)).orElseThrow();
        if (dados.getData() == null || dados.getData().isBlank()
                || dados.getProcedimento() == null || dados.getProcedimento().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data e procedimento sao obrigatorios");
        }
        Consulta c = new Consulta();
        c.setPacienteId(u.getId());            // ignora o pacienteId do corpo
        c.setNomePaciente(u.getNome());
        c.setData(dados.getData());
        c.setProcedimento(dados.getProcedimento());
        c.setDentista(dados.getDentista());
        c.setObservacoes(dados.getObservacoes());
        c.setEstado("MARCADA");
        return consultas.save(c);
    }

    // CORRIGIDO: so altera se for o dono (ou admin); estado validado.
    @PutMapping("/{id}")
    public Consulta atualizar(@PathVariable Long id, @RequestBody Consulta dados, HttpServletRequest req) {
        Consulta c = consultas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consulta inexistente"));
        exigirDono(c, req);
        if (dados.getEstado() != null && !ESTADOS.contains(dados.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado invalido");
        }
        c.setData(dados.getData());
        c.setProcedimento(dados.getProcedimento());
        c.setDentista(dados.getDentista());
        c.setObservacoes(dados.getObservacoes());
        if (dados.getEstado() != null) c.setEstado(dados.getEstado());
        return consultas.save(c);
    }

    // CORRIGIDO: so apaga se for o dono (ou admin).
    @DeleteMapping("/{id}")
    public Map<String, Object> apagar(@PathVariable Long id, HttpServletRequest req) {
        Consulta c = consultas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consulta inexistente"));
        exigirDono(c, req);
        consultas.delete(c);
        return Map.of("ok", true, "apagada", id);
    }

    // CORRIGIDO: pesquisa parametrizada e limitada ao proprio (ou tudo, se admin).
    @GetMapping("/buscar")
    public List<Consulta> buscar(@RequestParam String termo, HttpServletRequest req) {
        return isAdmin(req)
                ? consultas.buscarTodas(termo)
                : consultas.buscarDoPaciente(userId(req), termo);
    }

    private void exigirDono(Consulta c, HttpServletRequest req) {
        if (!isAdmin(req) && !userId(req).equals(c.getPacienteId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta consulta nao e sua");
        }
    }
}
