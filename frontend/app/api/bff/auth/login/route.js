import { cookies } from "next/headers";
import { NextResponse } from "next/server";

// Login do MODO SEGURO, feito no SERVIDOR Next.
// Recebe as credenciais, chama o backend seguro (8082) do lado servidor,
// guarda o token num cookie httpOnly (inacessível ao JavaScript do browser)
// e devolve ao cliente apenas dados não sensíveis (id, nome, perfil).
const BACKEND = process.env.SECURE_API_URL || "http://localhost:8082/api";

export async function POST(req) {
  const body = await req.text();
  let data;
  try {
    const res = await fetch(BACKEND + "/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body,
    });
    data = await res.json();
  } catch (e) {
    return NextResponse.json(
      { ok: false, erro: "BFF não conseguiu contactar o backend seguro (8082): " + e.message },
      { status: 502 }
    );
  }

  if (data && data.ok) {
    cookies().set("sessao", data.token, {
      httpOnly: true,       // o browser não consegue ler via document.cookie
      sameSite: "lax",
      path: "/",
      maxAge: 3600,
    });
    // Não devolvemos o token ao cliente.
    return NextResponse.json({ ok: true, id: data.id, nome: data.nome, perfil: data.perfil });
  }

  return NextResponse.json(data || { ok: false, erro: "Credenciais inválidas" });
}
