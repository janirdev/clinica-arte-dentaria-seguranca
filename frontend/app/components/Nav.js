"use client";

import { usePathname } from "next/navigation";

const LINKS = [
  { href: "/", label: "Início" },
  { href: "/marcacao", label: "Marcar consulta" },
  { href: "/historico", label: "O meu histórico" },
  { href: "/admin", label: "Área reservada" },
  { href: "/auditoria", label: "Auditoria" },
  { href: "/login", label: "Entrar" },
];

export default function Nav() {
  const pathname = usePathname();
  return (
    <nav className="nav">
      {LINKS.map((l) => {
        const active = l.href === "/" ? pathname === "/" : pathname.startsWith(l.href);
        return (
          <a key={l.href} href={l.href} className={active ? "active" : ""}>
            {l.label}
          </a>
        );
      })}
    </nav>
  );
}
