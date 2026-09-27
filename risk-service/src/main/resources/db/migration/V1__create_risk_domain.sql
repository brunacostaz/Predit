CREATE TABLE customers (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    phone VARCHAR(24),
    dealership VARCHAR(120) NOT NULL,
    contact_consent BOOLEAN NOT NULL DEFAULT FALSE,
    consent_updated_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE vehicles (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id),
    vin VARCHAR(17) NOT NULL UNIQUE,
    model VARCHAR(80) NOT NULL,
    model_year INTEGER NOT NULL,
    mileage INTEGER NOT NULL,
    warranty_end_date DATE,
    last_service_date DATE
);

CREATE TABLE risk_assessments (
    id UUID PRIMARY KEY,
    vehicle_id UUID NOT NULL REFERENCES vehicles(id),
    score INTEGER NOT NULL CHECK (score BETWEEN 0 AND 100),
    level VARCHAR(20) NOT NULL,
    reasons TEXT NOT NULL,
    recommended_action VARCHAR(400) NOT NULL,
    assessed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE leads (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id),
    vehicle_id UUID NOT NULL REFERENCES vehicles(id),
    title VARCHAR(160) NOT NULL,
    action VARCHAR(400) NOT NULL,
    status VARCHAR(24) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    due_at TIMESTAMP WITH TIME ZONE,
    assigned_to VARCHAR(180),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_vehicles_customer ON vehicles(customer_id);
CREATE INDEX idx_risk_vehicle_date ON risk_assessments(vehicle_id, assessed_at DESC);
CREATE INDEX idx_leads_status_priority ON leads(status, priority);
