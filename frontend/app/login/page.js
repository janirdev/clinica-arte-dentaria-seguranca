"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { apiPost } from "../../lib/api";

export default function LoginPage() {
  const [email, setEmail] = useState("ana@exemplo.pt");
  const [senha, setSenha] = useState("ana123");
  const [status, setStatus] = useState(null); // {tipo, texto}
  const router = useRouter();

  async function entrar(e) {
    e.preventDefault();
    setStatus({ tipo: "info", texto: "A autenticar..." });
    try {
      // FALHA INTENCIONAL: todo o consumo da API acontece aqui no cliente
      // (browser), nunca no servidor Next.js.
      const r = await apiPost("/auth/login", { email, senha });
      if (r.ok) {
        // Modo vulnerável: o "token" fraco vem na resposta e fica no localStorage
        // (o cliente confia nele). Modo seguro: não vem token nenhum — está num
        // cookie httpOnly posto pelo servidor Next; aqui só guardamos dados de UI.
        if (r.token) localStorage.setItem("token", r.token);
        localStorage.setItem("pacienteId", r.id);
        localStorage.setItem("perfil", r.perfil);
        localStorage.setItem("nome", r.nome);
        setStatus({ tipo: "success", texto: `Autenticado como ${r.nome} (perfil: ${r.perfil}). A redirecionar...` });
        router.push(r.perfil === "admin" ? "/admin" : "/historico");
      } else {
        setStatus({ tipo: "error", texto: "Erro: " + r.erro });
      }
    } catch (err) {
      setStatus({
        tipo: "error",
        texto:
          "Não foi possível contactar a API em http://localhost:8080. " +
          "Confirme que o backend está a correr (mvn spring-boot:run). Detalhe: " + err.message,
      });
    }
  }

  return (
    <div>
      <h1>Entrar</h1>
      <p className="subtitle">Aceda à sua área de paciente.</p>

      <div className="card" style={{ maxWidth: 420 }}>
        <form onSubmit={entrar} className="form" style={{ maxWidth: "none" }}>
          <div className="field">
            <label>Email</label>
            <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="email@exemplo.pt" />
          </div>
          <div className="field">
            <label>Senha</label>
            <input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} placeholder="••••••" />
          </div>
          <button type="submit" className="btn btn-primary">Entrar</button>
        </form>
      </div>

      {status && <div className={`alert alert-${status.tipo}`}>{status.texto}</div>}

      <div className="alert alert-warn" style={{ marginTop: 20 }}>
        <strong>Contas de teste:</strong> ana@exemplo.pt / ana123 · bruno@exemplo.pt / bruno123 · admin@exemplo.pt / admin123
      </div>
    </div>
  );
}
