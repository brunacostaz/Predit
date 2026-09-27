INSERT INTO customers (id, name, email, phone, dealership, contact_consent, consent_updated_at, created_at) VALUES
('11111111-1111-1111-1111-111111111111', 'Ana Souza', 'ana.souza@example.com', '+5511999990001', 'Ford Lapa', TRUE, NOW(), NOW()),
('22222222-2222-2222-2222-222222222222', 'Rafael Lima', 'rafael.lima@example.com', '+5511999990002', 'Ford Morumbi', TRUE, NOW(), NOW()),
('33333333-3333-3333-3333-333333333333', 'Marina Costa', 'marina.costa@example.com', '+5511999990003', 'Ford Campinas', TRUE, NOW(), NOW()),
('44444444-4444-4444-4444-444444444444', 'Bruno Martins', 'bruno.martins@example.com', '+5511999990004', 'Ford Morumbi', TRUE, NOW(), NOW()),
('55555555-5555-5555-5555-555555555555', 'Camila Rocha', 'camila.rocha@example.com', '+5511999990005', 'Ford Lapa', FALSE, NULL, NOW()),
('66666666-6666-6666-6666-666666666666', 'Diego Nunes', 'diego.nunes@example.com', '+5511999990006', 'Ford Campinas', TRUE, NOW(), NOW());

INSERT INTO vehicles (id, customer_id, vin, model, model_year, mileage, warranty_end_date, last_service_date) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '11111111-1111-1111-1111-111111111111', '9BFRNG25A82000001', 'Ranger', 2025, 18200, '2027-02-20', '2026-04-18'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', '22222222-2222-2222-2222-222222222222', '9BFBRO24B74000002', 'Bronco', 2024, 32100, '2027-08-15', '2026-01-20'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', '33333333-3333-3333-3333-333333333333', '9BFMAV23C66000003', 'Maverick', 2023, 41800, '2026-12-08', '2025-11-05'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', '44444444-4444-4444-4444-444444444444', '9BFMUS24D58000004', 'Mustang', 2024, 14700, '2027-04-11', '2026-03-12'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', '55555555-5555-5555-5555-555555555555', '9BFRNG24E41000005', 'Ranger', 2024, 22100, '2027-06-25', '2026-08-16'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6', '66666666-6666-6666-6666-666666666666', '9BFTER25F63000006', 'Territory', 2025, 9200, '2028-01-10', '2026-02-02');

INSERT INTO risk_assessments (id, vehicle_id, score, level, reasons, recommended_action, assessed_at) VALUES
('d1111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 82, 'CRITICAL', 'Revisao obrigatoria atrasada; garantia proxima do fim; baixa interacao com a rede', 'Oferecer check-up prioritario e Ford Protect', NOW()),
('d2222222-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 74, 'HIGH', 'Oito meses sem passagem; uso severo informado; alerta por modelo', 'Reservar inspecao preventiva com prioridade', NOW()),
('d3333333-3333-3333-3333-333333333333', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 66, 'HIGH', 'Garantia proxima do fim; revisao fora do prazo; campanha anterior sem retorno', 'Oferecer revisao com beneficio de retorno', NOW()),
('d4444444-4444-4444-4444-444444444444', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 58, 'MEDIUM', 'Baixo engajamento no app; revisao proxima do vencimento', 'Enviar convite para revisao premium', NOW()),
('d5555555-5555-5555-5555-555555555555', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 41, 'MEDIUM', 'Revisoes em dia; garantia ativa; boa interacao com a concessionaria', 'Manter lembrete preventivo sem contato comercial', NOW()),
('d6666666-6666-6666-6666-666666666666', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 63, 'HIGH', 'Primeiro ciclo de pos-venda; baixa abertura de notificacoes', 'Realizar contato consultivo para a primeira revisao', NOW());

INSERT INTO leads (id, customer_id, vehicle_id, title, action, status, priority, due_at, assigned_to, created_at, updated_at) VALUES
('e1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'Garantia em risco', 'Reservar horario e explicar o impacto da revisao atrasada', 'OPEN', 'CRITICAL', CURRENT_TIMESTAMP + INTERVAL '1' DAY, 'advisor@predit.com.br', NOW(), NOW()),
('e2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'Inspecao preventiva', 'Oferecer inspecao e reserva tecnica', 'IN_PROGRESS', 'HIGH', CURRENT_TIMESTAMP + INTERVAL '2' DAY, 'advisor@predit.com.br', NOW(), NOW()),
('e3333333-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'Retorno para a rede', 'Apresentar beneficio de retorno e opcoes de agenda', 'OPEN', 'HIGH', CURRENT_TIMESTAMP + INTERVAL '2' DAY, NULL, NOW(), NOW()),
('e4444444-4444-4444-4444-444444444444', '44444444-4444-4444-4444-444444444444', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'Revisao premium', 'Enviar convite personalizado e janela de agendamento', 'CONTACTED', 'MEDIUM', CURRENT_TIMESTAMP + INTERVAL '4' DAY, 'advisor@predit.com.br', NOW(), NOW()),
('e6666666-6666-6666-6666-666666666666', '66666666-6666-6666-6666-666666666666', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 'Primeira revisao', 'Orientar sobre o primeiro ciclo de manutencao', 'OPEN', 'HIGH', CURRENT_TIMESTAMP + INTERVAL '3' DAY, NULL, NOW(), NOW());
