#!/usr/bin/env bash
# Repoe os dados do backend VULNERAVEL (schema public) ao estado inicial,
# desfazendo a corrupcao causada pela demonstracao. Uso de laboratorio.
set -e
echo "A repor os dados do schema public..."
docker exec -i postgres_db psql -U postgres -d clinica <<'SQL'
TRUNCATE public.consultas RESTART IDENTITY;
TRUNCATE public.usuarios  RESTART IDENTITY CASCADE;

INSERT INTO public.usuarios (nome, email, senha, perfil) VALUES
 ('Ana Costa',     'ana@exemplo.pt',   'ana123',   'paciente'),
 ('Bruno Dias',    'bruno@exemplo.pt', 'bruno123', 'paciente'),
 ('Admin Clinica', 'admin@exemplo.pt', 'admin123', 'admin');

INSERT INTO public.consultas (paciente_id, nome_paciente, data, procedimento, dentista, observacoes, estado) VALUES
 (1, 'Ana Costa',  '2026-11-03 14:30', 'Limpeza',        'Dra. Marta', 'Sensibilidade no dente 26.',                   'MARCADA'),
 (1, 'Ana Costa',  '2026-09-10 09:00', 'Destartarizacao','Dra. Marta', 'Tudo normal.',                                 'REALIZADA'),
 (2, 'Bruno Dias', '2026-11-05 16:00', 'Canal',          'Dr. Paulo',  'Dor aguda no dente 36. Alergia a penicilina.', 'MARCADA');
SQL
echo "Dados repostos. Confirmar:"
docker exec postgres_db psql -U postgres -d clinica -c "SELECT id, nome_paciente, procedimento, estado FROM public.consultas ORDER BY id;"
