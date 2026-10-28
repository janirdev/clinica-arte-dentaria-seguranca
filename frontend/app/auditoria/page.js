"use client";

import { useEffect, useState } from "react";
import { apiGet, getModo } from "../../lib/api";

export default function AuditoriaPage() {
  const [autorizado, setAutorizado] = useState(false);
  const [registos, setRegistos] = useState([]);
  const [erro, setErro] = useState(null);

  useEffect(() => {
    // Gate no cliente (cosmético). O servidor é que impõe mesmo "só admin".
    setAutorizado(localStorage.getItem("perfil") === "admin");
  }, []);

  async function carregar() {
    setErro(null);
    try {
      setRegistos(await apiGet("/auditoria"));
    } catch (e) {
      setErro(e.message + (getModo() === "vulneravel"
        ? " — a consola de auditoria existe no modo Seguro (8082)."
        : ""));
    }
  }

  function campo(json, chave) {
    if (!json) return "";
    try { return JSON.parse(json)[chave] ?? ""; } catch { return ""; }
  }

  if (!autorizado) {
    return (
      <div>
        <h1>Auditoria</h1>
        <p className="subtitle">Registo de todas as alterações aos dados.</p>
        <div className="alert alert-warn">
          Área restrita a administradores. Entra como <code>admin@exemplo.pt</code> (modo Seguro).
        </div>
      </div>
    );
  }

  return (
    <div>
      <h1>Auditoria</h1>
      <p className="subtitle">Todas as alterações (INSERT/UPDATE/DELETE) registadas na base de dados, mesmo vindas de um ataque.</p>

      <button className="btn btn-primary" onClick={carregar}>Carregar registos</button>
      {erro && <div className="alert alert-error">{erro}</div>}

      {registos.length > 0 && (
        <table className="table" style={{ marginTop: 16 }}>
          <thead>
            <tr>
              <th>#</th><th>Quando</th><th>Operação</th><th>Tabela</th><th>Reg.</th>
              <th>IP</th><th>Antes</th><th>Depois</th>
            </tr>
          </thead>
          <tbody>
            {registos.map((r) => (
              <tr key={r.id}>
                <td>{r.id}</td>
                <td>{String(r.momento).replace("T", " ").slice(0, 19)}</td>
                <td><span className={`badge badge-${r.operacao === "DELETE" ? "CANCELADA" : r.operacao === "INSERT" ? "REALIZADA" : "MARCADA"}`}>{r.operacao}</span></td>
                <td>{r.tabela}</td>
                <td>{r.registo_id}</td>
                <td>{r.ip_cliente}</td>
                <td style={{ fontSize: 12 }}>{campo(r.dados_antigos, "procedimento")} {campo(r.dados_antigos, "estado")}</td>
                <td style={{ fontSize: 12 }}>{campo(r.dados_novos, "procedimento")} {campo(r.dados_novos, "estado")}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {registos.length === 0 && !erro && <div className="card" style={{ marginTop: 16 }}>Sem registos. Carrega para atualizar.</div>}
    </div>
  );
}
