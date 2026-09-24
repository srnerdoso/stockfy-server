-- Remove colunas com dados sensíveis da tabela de auditoria.
-- Dados históricos são eliminados definitivamente para conformidade com LGPD.
-- Referência: Issue #30

ALTER TABLE users_aud DROP COLUMN IF EXISTS email;
ALTER TABLE users_aud DROP COLUMN IF EXISTS password_hash;
ALTER TABLE users_aud DROP COLUMN IF EXISTS reset_password_code_hash;
ALTER TABLE users_aud DROP COLUMN IF EXISTS reset_password_expires_at;
