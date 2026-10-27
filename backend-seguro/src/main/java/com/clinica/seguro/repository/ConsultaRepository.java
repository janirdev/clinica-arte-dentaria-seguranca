package com.clinica.seguro.repository;

import com.clinica.seguro.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByPacienteId(Long pacienteId);

    // CORRIGIDO: pesquisa parametrizada (sem concatenacao -> sem SQL Injection).
    @Query("SELECT c FROM Consulta c WHERE c.pacienteId = :pid AND LOWER(c.nomePaciente) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Consulta> buscarDoPaciente(@Param("pid") Long pacienteId, @Param("termo") String termo);

    @Query("SELECT c FROM Consulta c WHERE LOWER(c.nomePaciente) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Consulta> buscarTodas(@Param("termo") String termo);
}
