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

-- Crop NÃO é lookup (é entidade: PK UUID + Code), mas é CATÁLOGO GLOBAL de
-- referência agronômica (NDVI esperado, valor/ha, vulnerabilidades) — curado
-- centralmente e compartilhado por todos. Por isso vem pré-populado no seed.
-- Id e Code ficam por conta dos defaults (gen_random_uuid / IDENTITY).
INSERT INTO "Crop" ("Name", "ScientificName", "AverageCycleDays",
                    "ExpectedNdviMin", "ExpectedNdviMax", "AverageValuePerHectare",
                    "DroughtVulnerability", "FrostVulnerability")
SELECT nome, sci, ciclo, ndvimin, ndvimax, valor, seca, geada FROM (VALUES
  ('Soja',            'Glycine max',            120, 0.650, 0.850,  6500.00, 7.5, 4.0),
  ('Milho',           'Zea mays',               150, 0.600, 0.880,  5200.00, 8.0, 6.0),
  ('Algodão',         'Gossypium hirsutum',     180, 0.550, 0.800,  9800.00, 6.5, 3.0),
  ('Café',            'Coffea arabica',         365, 0.700, 0.900, 18500.00, 5.0, 9.0),
  ('Trigo',           'Triticum aestivum',      130, 0.550, 0.820,  4100.00, 7.0, 8.5),
  ('Cana-de-açúcar',  'Saccharum officinarum',  360, 0.650, 0.900,  7200.00, 6.0, 5.0),
  ('Arroz',           'Oryza sativa',           130, 0.600, 0.880,  5600.00, 9.0, 5.5),
  ('Feijão',          'Phaseolus vulgaris',      90, 0.550, 0.820,  4800.00, 8.5, 6.5),
  ('Sorgo',           'Sorghum bicolor',        120, 0.500, 0.780,  3500.00, 4.0, 5.0)
) AS t(nome, sci, ciclo, ndvimin, ndvimax, valor, seca, geada)
WHERE NOT EXISTS (SELECT 1 FROM "Crop");

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
