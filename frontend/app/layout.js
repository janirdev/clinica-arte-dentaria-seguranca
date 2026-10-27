import "./globals.css";
import Nav from "./components/Nav";
import ModeSwitch from "./components/ModeSwitch";

export const metadata = {
  title: "Clínica Arte Dentária — LAB de Segurança",
  description: "Aplicação de laboratório propositadamente vulnerável (uso didático)",
};

export default function RootLayout({ children }) {
  return (
    <html lang="pt">
      <body>
        <div className="lab-banner">
          ⚠️ AMBIENTE DE LABORATÓRIO — aplicação propositadamente insegura. Só para aula, em localhost. Não usar dados reais.
        </div>
        <header className="header">
          <span className="brand">🦷 Clínica Arte Dentária</span>
          <Nav />
          <ModeSwitch />
        </header>
        <main className="container">{children}</main>
      </body>
    </html>
  );
}
