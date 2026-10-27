"use client";

import { useEffect, useState } from "react";
import { apiGet, apiDelete } from "../../lib/api";

export default function HistoricoPage() {
  const [pacienteId, setPacienteId] = useState("1");
  const [consultas, setConsultas] = useState([]);
  const [erro, setErro] = useState(null);

  async function carregar(id) {
    setErro(null);
    try {
      // FALHA INTENCIONAL (IDOR): o id do paciente é apenas um parâmetro na URL
      // da API. Trocar o número aqui mostra o histórico de outro paciente,
      // porque a API não verifica se o utilizador autenticado é o dono.
      const dados = await apiGet("/consultas/paciente/" + id);
      setConsultas(Array.isArray(dados) ? dados : []);
    } catch (err) {
      setErro("Falha ao contactar a API: " + err.message);
    }
  }

  useEffect(() => {
    const guardado = localStorage.getItem("pacienteId") || "1";
    setPacienteId(guardado);
    carregar(guardado);
  }, []);

  async function apagar(id) {
    // FALHA INTENCIONAL: apaga sem o servidor verificar o dono (corrupção de dados).
    await apiDelete("/consultas/" + id);
    carregar(pacienteId);
  }

  return (
    <div>
      <h1>O meu histórico</h1>
      <p className="subtitle">Consultas passadas e futuras.</p>

      <div className="toolbar">
        <label style={{ fontSize: 13, fontWeight: 600, color: "var(--muted)" }}>ID do paciente:</label>
        <input className="inline-input" value={pacienteId} onChange={(e) => setPacienteId(e.target.value)} />
        <button className="btn btn-ghost btn-sm" onClick={() => carregar(pacienteId)}>Carregar</button>
        <span className="hint">experimente trocar o ID — demonstra IDOR</span>
      </div>

      {erro && <div className="alert alert-error">{erro}</div>}
      {!erro && consultas.length === 0 && <div className="card">Sem consultas para mostrar.</div>}

      {consultas.map((c) => (
        <div key={c.id} className="card consulta">
          <div className="consulta-top">
            <strong>{c.procedimento}</strong>
            <span className={`badge badge-${c.estado}`}>{c.estado}</span>
          </div>
          <div className="consulta-meta">
            {c.data} · {c.dentista} · Paciente: {c.nomePaciente}
          </div>
          {c.observacoes && <div className="consulta-obs">📝 {c.observacoes}</div>}
          <div style={{ marginTop: 10 }}>
            <button className="btn btn-danger btn-sm" onClick={() => apagar(c.id)}>Apagar</button>
          </div>
        </div>
      ))}
    </div>
  );
}
