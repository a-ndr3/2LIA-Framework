CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS query_category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

INSERT INTO query_category (name) VALUES 
('network'),
('exception'),
('endpoint'),
('time'),
('generic')
ON CONFLICT (name) DO NOTHING;

CREATE TABLE IF NOT EXISTS esper_queries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL,
    query_statement VARCHAR(70) NOT NULL,
    deploymentId TEXT NOT NULL,
    query TEXT NOT NULL,
    event_classes JSONB NOT NULL,
    category_id INT REFERENCES query_category(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    status BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS analysis_queries (
    query TEXT NOT NULL,
    query_type VARCHAR(50) NOT NULL,
    check_topology BOOLEAN NOT NULL DEFAULT TRUE,
    status BOOLEAN NOT NULL DEFAULT TRUE
);
