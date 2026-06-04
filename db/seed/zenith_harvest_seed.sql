-- =============================================================================
-- Zenith Harvest — Seed dos lookups (tabelas de domínio)
-- =============================================================================
-- Roda APÓS o schema (01_schema.sql) no init do Postgres. Popula as tabelas de
-- domínio para que as FKs de lookup sejam satisfeitas em runtime (o app usa
-- ddl-auto: validate, então NÃO cria nem popula nada sozinho).
--
-- Idempotente: cada bloco só insere se a tabela estiver vazia (NOT EXISTS).
-- IDs ficam por conta do IDENTITY (sequência por tabela, 1 em 1).
-- =============================================================================

-- Users domain ---------------------------------------------------------------
INSERT INTO "AccessLogAction" ("Description")
SELECT v FROM (VALUES
  ('LOGIN_SUCCESS'), ('LOGIN_FAILURE'), ('LOGOUT'),
  ('PASSWORD_CHANGE'), ('ANONYMIZATION'), ('BLOCK')
) AS t(v)
WHERE NOT EXISTS (SELECT 1 FROM "AccessLogAction");

-- Farm domain ----------------------------------------------------------------
INSERT INTO "Biome" ("Description", "RegionalRiskFactor")
SELECT d, r FROM (VALUES
  ('Amazônia', 1.30), ('Cerrado', 1.10), ('Caatinga', 1.40),
  ('Mata Atlântica', 1.00), ('Pampa', 0.90), ('Pantanal', 1.20)
) AS t(d, r)
WHERE NOT EXISTS (SELECT 1 FROM "Biome");

INSERT INTO "ProductionSystem" ("Description", "PremiumDiscount")
SELECT d, p FROM (VALUES
  ('Sequeiro', 0.00), ('Irrigado', 5.00),
  ('Plantio direto', 3.00), ('Plantio convencional', 0.00)
) AS t(d, p)
WHERE NOT EXISTS (SELECT 1 FROM "ProductionSystem");

INSERT INTO "PlotSituation" ("Description", "IsTerminal")
SELECT d, term FROM (VALUES
  ('Em preparo', FALSE), ('Plantado', FALSE), ('Em desenvolvimento', FALSE),
  ('Colhido', TRUE), ('Perda total', TRUE)
) AS t(d, term)
WHERE NOT EXISTS (SELECT 1 FROM "PlotSituation");

-- Insurance catalog ----------------------------------------------------------
INSERT INTO "InsurerSituation" ("Description", "AllowsNewPolicies")
SELECT d, a FROM (VALUES
  ('Ativa', TRUE), ('Suspensa', FALSE), ('Descredenciada', FALSE)
) AS t(d, a)
WHERE NOT EXISTS (SELECT 1 FROM "InsurerSituation");

INSERT INTO "InsuranceSituation" ("Description", "AllowsNewQuotes")
SELECT d, a FROM (VALUES
  ('Disponível', TRUE), ('Pausada', FALSE), ('Descontinuada', FALSE)
) AS t(d, a)
WHERE NOT EXISTS (SELECT 1 FROM "InsuranceSituation");

INSERT INTO "InsuranceQuoteSituation" ("Description", "IsTerminal")
SELECT d, term FROM (VALUES
  ('Em aberto', FALSE), ('Aceita', TRUE), ('Recusada', TRUE), ('Expirada', TRUE)
) AS t(d, term)
WHERE NOT EXISTS (SELECT 1 FROM "InsuranceQuoteSituation");

-- Operation: situações e tipos -----------------------------------------------
INSERT INTO "PolicySituation" ("Description", "AllowsClaim", "IsTerminal")
SELECT d, ac, term FROM (VALUES
  ('Vigente', TRUE, FALSE), ('Aguardando pagamento', FALSE, FALSE),
  ('Cancelada', FALSE, TRUE), ('Expirada', FALSE, TRUE)
) AS t(d, ac, term)
WHERE NOT EXISTS (SELECT 1 FROM "PolicySituation");

INSERT INTO "ClaimSituation" ("Description", "AllowsClaim", "IsTerminal")
SELECT d, ac, term FROM (VALUES
  ('Aberto', TRUE, FALSE), ('Em análise', FALSE, FALSE),
  ('Aprovado', FALSE, FALSE), ('Rejeitado', FALSE, TRUE),
  ('Pago', FALSE, TRUE)
) AS t(d, ac, term)
WHERE NOT EXISTS (SELECT 1 FROM "ClaimSituation");

INSERT INTO "ClaimEventType" ("Description", "RequiresPhoto")
SELECT d, ph FROM (VALUES
  ('Seca', FALSE), ('Geada', FALSE), ('Granizo', TRUE),
  ('Excesso de chuva', FALSE), ('Praga', TRUE), ('Incêndio', TRUE)
) AS t(d, ph)
WHERE NOT EXISTS (SELECT 1 FROM "ClaimEventType");

INSERT INTO "ClaimCategory" ("Description")
SELECT v FROM (VALUES
  ('Climático'), ('Biológico'), ('Operacional')
) AS t(v)
WHERE NOT EXISTS (SELECT 1 FROM "ClaimCategory");

INSERT INTO "ClaimSubCategory" ("Description")
SELECT v FROM (VALUES
  ('Estiagem prolongada'), ('Geada de radiação'), ('Tempestade de granizo'),
  ('Alagamento'), ('Infestação de pragas'), ('Queimada')
) AS t(v)
WHERE NOT EXISTS (SELECT 1 FROM "ClaimSubCategory");

INSERT INTO "SatelliteSource" ("Description", "Operator", "Resolution", "RevisitFrequencyDays")
SELECT d, op, res, rev FROM (VALUES
  ('Sentinel-2', 'ESA', '10m', 5),
  ('Landsat-8', 'NASA/USGS', '30m', 16),
  ('MODIS', 'NASA', '250m', 1)
) AS t(d, op, res, rev)
WHERE NOT EXISTS (SELECT 1 FROM "SatelliteSource");

INSERT INTO "SatelliteClass" ("Description", "Severity")
SELECT d, s FROM (VALUES
  ('Vegetação saudável', 0), ('Estresse leve', 1),
  ('Estresse moderado', 2), ('Estresse severo', 3), ('Solo exposto', 4)
) AS t(d, s)
WHERE NOT EXISTS (SELECT 1 FROM "SatelliteClass");

INSERT INTO "AlertType" ("Description")
SELECT v FROM (VALUES
  ('Queda de NDVI'), ('Risco de seca'), ('Risco de geada'), ('Anomalia detectada')
) AS t(v)
WHERE NOT EXISTS (SELECT 1 FROM "AlertType");

INSERT INTO "AlertSeverity" ("Description", "Level", "ColorHex", "PushNotify", "SmsNotify")
SELECT d, lvl, c, p, s FROM (VALUES
  ('Informativo', 1, '#2E7D32', FALSE, FALSE),
  ('Atenção',     2, '#F9A825', TRUE,  FALSE),
  ('Crítico',     3, '#C62828', TRUE,  TRUE)
) AS t(d, lvl, c, p, s)
WHERE NOT EXISTS (SELECT 1 FROM "AlertSeverity");

INSERT INTO "AlertSituation" ("Description", "IsTerminal")
SELECT d, term FROM (VALUES
  ('Aberto', FALSE), ('Visualizado', FALSE), ('Resolvido', TRUE), ('Descartado', TRUE)
) AS t(d, term)
WHERE NOT EXISTS (SELECT 1 FROM "AlertSituation");

INSERT INTO "RejectionReason" ("Description")
SELECT v FROM (VALUES
  ('Fora do período de vigência'), ('Carência não cumprida'),
  ('Evento não coberto pela apólice'), ('Suspeita de fraude'),
  ('Documentação insuficiente'), ('Perda abaixo da franquia')
) AS t(v)
WHERE NOT EXISTS (SELECT 1 FROM "RejectionReason");

-- Financial -------------------------------------------------------------------
INSERT INTO "PaymentType" ("Description", "Direction")
SELECT d, dir FROM (VALUES
  ('Indenização de sinistro (PIX)', 'OUT'),
  ('Pagamento de prêmio',           'IN'),
  ('Estorno',                       'OUT'),
  ('Repasse à seguradora',          'OUT')
) AS t(d, dir)
WHERE NOT EXISTS (SELECT 1 FROM "PaymentType");

INSERT INTO "PaymentSituation" ("Description", "IsTerminal", "IsSuccess")
SELECT d, term, suc FROM (VALUES
  ('Pendente',   FALSE, FALSE), ('Processando', FALSE, FALSE),
  ('Confirmado', TRUE,  TRUE),  ('Falhou',      TRUE,  FALSE),
  ('Estornado',  TRUE,  FALSE)
) AS t(d, term, suc)
WHERE NOT EXISTS (SELECT 1 FROM "PaymentSituation");
