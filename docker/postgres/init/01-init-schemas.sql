-- PostgreSQL Initialization for AppService Multi-Tenant Architecture
-- Supports: Shared DB, PostgreSQL Schemas, and Dedicated DBs

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Default shared database schema
CREATE SCHEMA IF NOT EXISTS shared_tenant;

-- Example dedicated tenant schemas for Tier-2 PostgreSQL Schema isolation
CREATE SCHEMA IF NOT EXISTS tenant_corp_a;
CREATE SCHEMA IF NOT EXISTS tenant_corp_b;
