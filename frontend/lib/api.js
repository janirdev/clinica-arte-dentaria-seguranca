// Camada de acesso a API.
//
// FALHA INTENCIONAL (modo vulneravel): todo o consumo da API e feito
// DIRETAMENTE no cliente (browser). A localizacao da API fica no codigo que
// chega ao browser, visivel em DevTools.
//
// Dois modos para a aula, com arquiteturas DIFERENTES de propósito:
//   - "vulneravel": o browser chama o backend 8080 DIRETAMENTE (consumo no
//     cliente). O endereco do backend aparece na aba Network.
//   - "seguro": o browser chama apenas /api/bff (o proprio servidor Next, mesma
//     origem). E o SERVIDOR Next que fala com o backend 8082 e guarda o token
//     num cookie httpOnly. O backend real e o token nunca aparecem no browser.
// O modo fica no localStorage (chave "modoApi") e e trocado no cabecalho.

export const BASES = {
  vulneravel: "http://localhost:8080/api", // chamado no browser (cliente)
  seguro: "/api/bff",                       // passa pelo servidor Next (BFF) -> 8082
};

// ❌ JEITO ERRADO (falha #2, modo vulnerável): um "segredo" escrito no código
// do cliente. Tudo o que está neste ficheiro é enviado ao browser, por isso
// esta chave é visível em DevTools → Sources. Está aqui só como exemplo didático
// e NÃO é usada por nenhum pedido. Num sistema real, nunca fazer isto.
export const API_KEY_DEMO = "chave-demo-1234-NAO-USAR-EM-PRODUCAO";

// ✅ JEITO CERTO (modo seguro): um segredo vive apenas no SERVIDOR, em
// process.env, e é lido dentro do BFF (app/api/bff/.../route.js) — nunca aqui.
// Exemplo (no route handler, lado servidor):
//     const chave = process.env.API_KEY;  // definida em .env.local, fora do git
// Assim o segredo nunca chega ao browser.

export function getModo() {
  if (typeof window === "undefined") return "vulneravel";
  return localStorage.getItem("modoApi") || "vulneravel";
}

function base() {
  return BASES[getModo()] || BASES.vulneravel;
}

function headers(extra) {
  const h = { ...extra };
  // Só no modo vulnerável o token (fraco) é enviado a partir do browser.
  // No modo seguro o token vive num cookie httpOnly e é o servidor Next (BFF)
  // que o acrescenta — o browser nem lhe toca.
  if (getModo() === "vulneravel" && typeof window !== "undefined") {
    const token = localStorage.getItem("token");
    if (token) h["Authorization"] = "Bearer " + token;
  }
  return h;
}

async function tratar(res) {
  let corpo = null;
  try { corpo = await res.json(); } catch { /* sem corpo */ }
  if (!res.ok) {
    const msg = (corpo && (corpo.erro || corpo.message)) || `HTTP ${res.status}`;
    const err = new Error(msg);
    err.status = res.status;
    throw err;
  }
  return corpo;
}

export async function apiGet(caminho) {
  return tratar(await fetch(base() + caminho, { headers: headers() }));
}

export async function apiPost(caminho, corpo) {
  return tratar(await fetch(base() + caminho, {
    method: "POST",
    headers: headers({ "Content-Type": "application/json" }),
    body: JSON.stringify(corpo),
  }));
}

export async function apiPut(caminho, corpo) {
  return tratar(await fetch(base() + caminho, {
    method: "PUT",
    headers: headers({ "Content-Type": "application/json" }),
    body: JSON.stringify(corpo),
  }));
}

export async function apiDelete(caminho) {
  return tratar(await fetch(base() + caminho, { method: "DELETE", headers: headers() }));
}
