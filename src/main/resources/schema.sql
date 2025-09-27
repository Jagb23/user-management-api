-- SQLite schema for User Management API
-- This file is for reference only - Hibernate will handle DDL automatically

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE,
    additional_fields TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Create unique index for email when not null
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users(email) WHERE email IS NOT NULL;

-- Create index for created_at for potential sorting/filtering
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);