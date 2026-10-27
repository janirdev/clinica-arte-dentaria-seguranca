-- V2: dados de exemplo (ficticios) para o laboratorio.

INSERT INTO usuarios (nome, email, senha, perfil) VALUES ('Ana Costa', 'ana@exemplo.pt', 'ana123', 'paciente');
INSERT INTO usuarios (nome, email, senha, perfil) VALUES ('Bruno Dias', 'bruno@exemplo.pt', 'bruno123', 'paciente');
INSERT INTO usuarios (nome, email, senha, perfil) VALUES ('Admin Clinica', 'admin@exemplo.pt', 'admin123', 'admin');

INSERT INTO consultas (paciente_id, nome_paciente, data, procedimento, dentista, observacoes, estado)
VALUES (1, 'Ana Costa', '2026-11-03 14:30', 'Limpeza', 'Dra. Marta', 'Sensibilidade no dente 26.', 'MARCADA');
INSERT INTO consultas (paciente_id, nome_paciente, data, procedimento, dentista, observacoes, estado)
VALUES (1, 'Ana Costa', '2026-09-10 09:00', 'Destartarizacao', 'Dra. Marta', 'Tudo normal.', 'REALIZADA');
INSERT INTO consultas (paciente_id, nome_paciente, data, procedimento, dentista, observacoes, estado)
VALUES (2, 'Bruno Dias', '2026-11-05 16:00', 'Canal', 'Dr. Paulo', 'Dor aguda no dente 36. Alergia a penicilina.', 'MARCADA');
