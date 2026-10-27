"use client";

import { useState } from "react";
import { apiPost } from "../../lib/api";

export default function MarcacaoPage() {
  const [form, setForm] = useState({
    data: "2026-11-10 10:00",
    procedimento: "Limpeza",
    dentista: "Dra. Marta",
    observacoes: "",
  });
  const [status, setStatus] = useState(null);

  function set(campo, valor) {
    setForm({ ...form, [campo]: valor });
  }

  async function marcar(e) {
    e.preventDefault();
    setStatus({ tipo: "info", texto: "A marcar..." });
    try {
      // FALHA INTENCIONAL: o pacienteId vem do localStorage (controlado pelo
      // cliente). Pode marcar-se uma consulta em nome de qualquer paciente.
      const pacienteId = Number(localStorage.getItem("pacienteId") || 1);
      const nome = localStorage.getItem("nome") || "Anónimo";
      const r = await apiPost("/consultas", {
        pacienteId,
        nomePaciente: nome,
        ...form,
        estado: "MARCADA",
      });
      setStatus({ tipo: "success", texto: "Consulta marcada com o id " + r.id + "." });
    } catch (err) {
      setStatus({ tipo: "error", texto: "Falha ao contactar a API: " + err.message });
    }
  }

  return (
    <div>
      <h1>Marcar consulta</h1>
      <p className="subtitle">Preencha os dados abaixo para agendar.</p>

      <div className="card" style={{ maxWidth: 460 }}>
        <form onSubmit={marcar} className="form" style={{ maxWidth: "none" }}>
          <div className="field">
            <label>Data e hora</label>
            <input value={form.data} onChange={(e) => set("data", e.target.value)} />
          </div>
          <div className="field">
            <label>Procedimento</label>
            <select value={form.procedimento} onChange={(e) => set("procedimento", e.target.value)}>
              <option>Limpeza</option>
              <option>Destartarização</option>
              <option>Canal</option>
              <option>Extração</option>
              <option>Consulta de avaliação</option>
            </select>
          </div>
          <div className="field">
            <label>Dentista</label>
            <input value={form.dentista} onChange={(e) => set("dentista", e.target.value)} />
          </div>
          <div className="field">
            <label>Observações</label>
            <textarea value={form.observacoes} onChange={(e) => set("observacoes", e.target.value)} />
          </div>
          <button type="submit" className="btn btn-primary">Marcar consulta</button>
        </form>
      </div>

      {status && <div className={`alert alert-${status.tipo}`}>{status.texto}</div>}
    </div>
  );
}
