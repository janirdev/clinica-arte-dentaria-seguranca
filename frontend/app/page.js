export default function Home() {
  return (
    <div>
      <h1>Bem-vindo à Clínica Arte Dentária</h1>
      <p className="subtitle">Marque a sua consulta online e consulte o seu histórico clínico num só lugar.</p>

      <div className="card-grid">
        <Cartao icon="📅" titulo="Marcar consulta" texto="Escolha data, procedimento e dentista em poucos segundos." href="/marcacao" />
        <Cartao icon="📋" titulo="O meu histórico" texto="Veja as suas consultas passadas e futuras." href="/historico" />
        <Cartao icon="🔒" titulo="Área reservada" texto="Acesso da equipa da clínica." href="/admin" />
      </div>

      <div className="alert alert-warn" style={{ marginTop: 32 }}>
        <strong>Nota para a aula:</strong> este site contém vulnerabilidades propositadas.
        Consulte <code>docs/LAB-VULNERABILIDADES.md</code> e <code>docs/EXERCICIOS-ATAQUE.md</code>.
      </div>
    </div>
  );
}

function Cartao({ icon, titulo, texto, href }) {
  return (
    <a href={href} className="card-link">
      <div className="card">
        <div className="card-icon">{icon}</div>
        <h3>{titulo}</h3>
        <p>{texto}</p>
      </div>
    </a>
  );
}
