ALTER TABLE risk_assessments
    ADD COLUMN model_name VARCHAR(80) NOT NULL DEFAULT 'predit-retention-model';

ALTER TABLE risk_assessments
    ADD COLUMN model_version VARCHAR(40) NOT NULL DEFAULT '1.0.0';

CREATE TABLE campaigns (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(400) NOT NULL,
    segment VARCHAR(120) NOT NULL,
    consent_required BOOLEAN NOT NULL,
    status VARCHAR(20) NOT NULL,
    eligible_customers INTEGER NOT NULL,
    estimated_conversion_rate NUMERIC(5,2) NOT NULL,
    estimated_revenue NUMERIC(14,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    activated_at TIMESTAMP WITH TIME ZONE
);

INSERT INTO campaigns (id, name, description, segment, consent_required, status,
                       eligible_customers, estimated_conversion_rate, estimated_revenue, created_at)
VALUES
('f1111111-1111-1111-1111-111111111111', 'Pre-viagem',
 'Check-up preventivo para clientes que informaram uma viagem no aplicativo.',
 'Viagem informada e revisao proxima', TRUE, 'DRAFT', 1240, 18.00, 2600000.00, NOW()),
('f2222222-2222-2222-2222-222222222222', 'Garantia em risco',
 'Contato ativo antes do fim da cobertura para reduzir evasao.',
 'Garantia a vencer em ate 180 dias', TRUE, 'ACTIVE', 860, 24.00, 2400000.00, NOW()),
('f3333333-3333-3333-3333-333333333333', 'Retorno pos-atrito',
 'Acao consultiva para clientes que enfrentaram espera por peca ou atendimento prolongado.',
 'Atendimento com atrito registrado', TRUE, 'DRAFT', 420, 31.00, 1500000.00, NOW());

CREATE INDEX idx_campaigns_status ON campaigns(status);
