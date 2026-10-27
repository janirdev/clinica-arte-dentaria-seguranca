package com.clinica.controller;

import com.clinica.model.Consulta;
import com.clinica.repository.ConsultaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API de consultas.
 *
 * FALHAS INTENCIONAIS (para a aula):
 *  - Nenhum endpoint exige token/autenticacao (Broken Authentication).
 *  - GET /api/consultas/paciente/{id} -> IDOR: basta trocar o id para ver o
 *    historico (e observacoes clinicas) de outro paciente.
 *  - GET /api/consultas -> expoe TODAS as consultas de todos os pacientes.
 *  - DELETE e PUT sem verificacao de dono -> corrupcao/alteracao de dados alheios.
 *  - GET /api/consultas/buscar?termo=... -> SQL Injection (concatenacao de string).
 */
@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaRepository consultaRepository;
    private final JdbcTemplate jdbc;

    public ConsultaController(ConsultaRepository consultaRepository, JdbcTemplate jdbc) {
        this.consultaRepository = consultaRepository;
        this.jdbc = jdbc;
    }

    // Lista TUDO, sem autenticacao.
    @GetMapping
    public List<Consulta> todas() {
        return consultaRepository.findAll();
    }

    // IDOR: qualquer um ve o historico de qualquer pacienteId.
    @GetMapping("/paciente/{pacienteId}")
    public List<Consulta> porPaciente(@PathVariable Long pacienteId) {
        return consultaRepository.findByPacienteId(pacienteId);
    }

    // Marcacao de consulta - aceita o pacienteId que vier no corpo, sem validar.
    @PostMapping
    public Consulta marcar(@RequestBody Consulta consulta) {
        if (consulta.getEstado() == null) consulta.setEstado("MARCADA");
        return consultaRepository.save(consulta);
    }

    // Altera qualquer consulta, sem verificar o dono (corrupcao de dados).
    @PutMapping("/{id}")
    public Consulta atualizar(@PathVariable Long id, @RequestBody Consulta dados) {
        Consulta c = consultaRepository.findById(id).orElseThrow();
        c.setData(dados.getData());
        c.setProcedimento(dados.getProcedimento());
        c.setDentista(dados.getDentista());
        c.setObservacoes(dados.getObservacoes());
        c.setEstado(dados.getEstado());
        return consultaRepository.save(c);
    }

    // Apaga qualquer consulta, sem verificar o dono.
    @DeleteMapping("/{id}")
    public Map<String, Object> apagar(@PathVariable Long id) {
        consultaRepository.deleteById(id);
        return Map.of("ok", true, "apagada", id);
    }

    // SQL Injection: o termo e concatenado diretamente na query.
    // Ex. de ataque: termo = ' OR '1'='1   -> devolve tudo.
    @GetMapping("/buscar")
    public List<Map<String, Object>> buscar(@RequestParam String termo) {
        String sql = "SELECT * FROM consultas WHERE nome_paciente LIKE '%" + termo + "%'";
        return jdbc.queryForList(sql);
    }
}
