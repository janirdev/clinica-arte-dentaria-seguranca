package com.clinica.model;

import jakarta.persistence.*;

@Entity
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dono da consulta. Usado para demonstrar IDOR (acesso a dados de outro paciente).
    private Long pacienteId;

    private String nomePaciente;
    private String data;        // ex.: "2026-11-03 14:30"
    private String procedimento; // ex.: "Limpeza", "Canal"
    private String dentista;
    private String observacoes;  // nota clinica (dado sensivel)
    private String estado;       // "MARCADA", "REALIZADA", "CANCELADA"

    public Consulta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
    public String getNomePaciente() { return nomePaciente; }
    public void setNomePaciente(String nomePaciente) { this.nomePaciente = nomePaciente; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getProcedimento() { return procedimento; }
    public void setProcedimento(String procedimento) { this.procedimento = procedimento; }
    public String getDentista() { return dentista; }
    public void setDentista(String dentista) { this.dentista = dentista; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
