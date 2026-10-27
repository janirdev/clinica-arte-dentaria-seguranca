"use client";

import { useEffect, useState } from "react";
import { apiGet } from "../../lib/api";

export default function AdminPage() {
  const [autorizado, setAutorizado] = useState(false);
  const [pacientes, setPacientes] = useState([]);
  const [consultas, setConsultas] = useState([]);
  const [erro, setErro] = useState(null);

  useEffect(() => {
    // FALHA INTENCIONAL (controlo de acesso só no cliente): a decisão de
    // mostrar o painel é feita no browser, a partir de um valor do localStorage
    // que o utilizador pode alterar (perfil = "admin"). Além disso, as próprias
    // APIs devolvem tudo mesmo sem ser admin.
    setAutorizado(localStorage.getItem("perfil") === "admin");
  }, []);

  async function carregar() {
    setErro(null);
    try {
      setPacientes(await apiGet("/pacientes")); // inclui senhas em texto simples
      setConsultas(await apiGet("/consultas")); // todas as consultas de todos
    } catch (err) {
      setErro("Falha ao contactar a API: " + err.message);
    }
  }

  if (!autorizado) {
    return (
      <div>
        <h1>Área reservada</h1>
        <p className="subtitle">Acesso restrito à equipa da clínica.</p>
        <div className="alert alert-warn">
          <strong>Dica de laboratório:</strong> no DevTools → Application → Local Storage, defina{" "}
          <code>perfil = admin</code> e recarregue. Controlo de acesso só no cliente é inútil.
        </div>
      </div>
    );
  }

  return (
    <div>
      <h1>Painel da clínica</h1>
      <p className="subtitle">Gestão de pacientes e consultas.</p>

      <button className="btn btn-primary" onClick={carregar}>Carregar dados</button>
      {erro && <div className="alert alert-error">{erro}</div>}

      <h2>Pacientes</h2>
      <table className="table">
        <thead>
          <tr><th>ID</th><th>Nome</th><th>Email</th><th>Senha</th><th>Perfil</th></tr>
        </thead>
        <tbody>
          {pacientes.map((p) => (
            <tr key={p.id}>
              <td>{p.id}</td><td>{p.nome}</td><td>{p.email}</td><td>{p.senha}</td><td>{p.perfil}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <h2>Todas as consultas</h2>
      <table className="table">
        <thead>
          <tr><th>#</th><th>Paciente</th><th>Procedimento</th><th>Data</th><th>Observações</th></tr>
        </thead>
        <tbody>
          {consultas.map((c) => (
            <tr key={c.id}>
              <td>{c.id}</td><td>{c.nomePaciente}</td><td>{c.procedimento}</td><td>{c.data}</td><td>{c.observacoes}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
