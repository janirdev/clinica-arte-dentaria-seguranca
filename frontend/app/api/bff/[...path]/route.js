import { cookies } from "next/headers";
import { NextResponse } from "next/server";

// BFF (Backend-for-Frontend) do MODO SEGURO.
// Corre no SERVIDOR Next.js: o browser só fala com localhost:3000, e é este
// código (no servidor) que chama o backend seguro (8082). Assim a localização
// do backend e o token NUNCA aparecem no browser / na aba Network.
const BACKEND = process.env.SECURE_API_URL || "http://localhost:8082/api";

async function proxy(req, params) {
  const sub = "/" + (params.path ? params.path.join("/") : "");
  const { search } = new URL(req.url);
  const alvo = BACKEND + sub + search;

  const headers = { "Content-Type": "application/json" };
  // O token vem do cookie httpOnly (lado servidor), não do browser.
  const token = cookies().get("sessao")?.value;
  if (token) headers["Authorization"] = "Bearer " + token;

  const init = { method: req.method, headers };
  if (!["GET", "HEAD", "DELETE"].includes(req.method)) {
    init.body = await req.text();
  }

  try {
    const res = await fetch(alvo, init);
    const corpo = await res.text();
    return new NextResponse(corpo, {
      status: res.status,
      headers: { "Content-Type": res.headers.get("content-type") || "application/json" },
    });
  } catch (e) {
    return NextResponse.json(
      { erro: "BFF não conseguiu contactar o backend seguro (8082): " + e.message },
      { status: 502 }
    );
  }
}

export async function GET(req, { params }) { return proxy(req, params); }
export async function POST(req, { params }) { return proxy(req, params); }
export async function PUT(req, { params }) { return proxy(req, params); }
export async function DELETE(req, { params }) { return proxy(req, params); }
