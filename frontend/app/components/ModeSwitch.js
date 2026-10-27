"use client";

import { useEffect, useState } from "react";

/**
 * Seletor de modo: alterna o backend que o site consome.
 *  - Vulneravel -> :8080
 *  - Seguro     -> :8082
 * Ao trocar, limpa a sessao (token/perfil) porque os tokens nao sao
 * compativeis entre os dois backends, e recarrega.
 */
export default function ModeSwitch() {
  const [modo, setModo] = useState("vulneravel");

  useEffect(() => {
    setModo(localStorage.getItem("modoApi") || "vulneravel");
  }, []);

  function trocar(novo) {
    localStorage.setItem("modoApi", novo);
    ["token", "pacienteId", "perfil", "nome"].forEach((k) => localStorage.removeItem(k));
    window.location.href = "/";
  }

  const seguro = modo === "seguro";
  return (
    <div className="mode-switch" title="Alterna entre a API vulnerável (8080) e a corrigida (8082)">
      <span className={`mode-dot ${seguro ? "on" : "off"}`} />
      <span className="mode-label">Modo:</span>
      <select value={modo} onChange={(e) => trocar(e.target.value)}>
        <option value="vulneravel">Vulnerável (8080)</option>
        <option value="seguro">Seguro (8082)</option>
      </select>
    </div>
  );
}
