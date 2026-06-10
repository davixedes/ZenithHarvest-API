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


-- =============================================================================
-- CENÁRIO DE DEMO (apresentação) — cadeia completa com UUIDs FIXOS
-- =============================================================================
-- Diferente dos lookups acima, isto é dado OPERACIONAL pronto para a demo rodar
-- com apenas LOGIN + abrir sinistro (sem montar User→Farm→Plot→Insurer→
-- Insurance→Policy na mão). Idempotente: PKs com UUID fixo + ON CONFLICT/NOT
-- EXISTS, então re-execuções não duplicam.
--
--   Login da demo:  joao@email.com  /  senhaSegura123
--   policyId fixo:  ff6b203e-5ece-41a5-b992-854757f287ed
--   plotId  fixo:   63abc434-52b8-4c43-bf46-617b511e5aa5
--
-- O hash da senha é gerado pelo pgcrypto (crypt + gen_salt 'bf'), formato $2a$,
-- compatível com o BCryptPasswordEncoder do core-svc.
-- =============================================================================

-- Endereço do produtor
INSERT INTO "Address" ("Id", "Street", "Number", "Neighboor", "City", "PostalCode", "UF", "Country")
VALUES ('164c4789-df97-4ba4-b4a2-5db1305e27b5', 'Rua das Flores', 100, 'Centro',
        'Ribeirão Preto', '14010-001', 'SP', 'Brasil')
ON CONFLICT DO NOTHING;

-- Produtor (User)
INSERT INTO "User" ("Id", "Cpf", "Name", "LastName", "AddressId", "Email", "Phone")
VALUES ('276edb95-bc64-4775-a4ec-0773efcb0f2c', '123.456.789-00', 'João', 'Silva',
        '164c4789-df97-4ba4-b4a2-5db1305e27b5', 'joao@email.com', '(11) 98765-4321')
ON CONFLICT DO NOTHING;

-- Credencial (hash BCrypt via pgcrypto — aceito pelo BCryptPasswordEncoder)
INSERT INTO "Credential" ("UserId", "Password", "Secret")
SELECT '276edb95-bc64-4775-a4ec-0773efcb0f2c',
       crypt('senhaSegura123', gen_salt('bf', 10)),
       gen_random_uuid()::text
WHERE NOT EXISTS (SELECT 1 FROM "Credential" WHERE "UserId" = '276edb95-bc64-4775-a4ec-0773efcb0f2c');

-- Seguradora (InsurerSituation 1 = Ativa)
INSERT INTO "Insurer" ("Id", "CorporateName", "TradeName", "Cnpj", "SusepCode",
                       "CommercialEmail", "Phone", "AdminFeePct", "TakeRatePct", "InsurerSituationId")
VALUES ('50124578-a7c1-4d63-9b5a-dc097c3f729c', 'Brasilseg Companhia de Seguros S.A.', 'Brasilseg',
        '28.196.889/0001-43', '05631', 'comercial@brasilseg.com.br', '(11) 3003-0000', 5.00, 12.50, 1)
ON CONFLICT DO NOTHING;

-- Produto de seguro (InsuranceSituation 1 = Disponível)
INSERT INTO "Insurance" ("Id", "InsurerId", "Name", "Description", "DeductiblePct", "GraceDays",
                         "MaxCoveragePerHectare", "BaseRatePct", "AvailableStates", "InsuranceSituationId")
VALUES ('131156a3-903b-4e89-8ef2-621fbc97f89c', '50124578-a7c1-4d63-9b5a-dc097c3f729c',
        'Multirrisco Agrícola Soja', 'Cobertura paramétrica via NDVI (Sentinel-2) para soja',
        10.00, 15, 6500.00, 4.500, 'SP,MG,GO,MT,PR', 1)
ON CONFLICT DO NOTHING;

-- Fazenda (Biome 2 = Cerrado)
INSERT INTO "Farm" ("Id", "UserId", "Name", "CarRegistration", "Nirf", "Latitude", "Longitude",
                    "TotalAreaHectares", "State", "BiomeId", "PropertyType", "PolygonWkt")
VALUES ('7f2cc962-aeac-4863-9715-a49eed56f99c', '276edb95-bc64-4775-a4ec-0773efcb0f2c',
        'Fazenda Sao Joao', 'SP-1234567-8901234567-8901234567-89', '12345678',
        -21.1767000, -47.8208000, 250.50, 'SP', 2, 'Rural',
        'POLYGON((-47.82 -21.17, -47.81 -21.17, -47.81 -21.18, -47.82 -21.18, -47.82 -21.17))')
ON CONFLICT DO NOTHING;

-- Talhão (PlotSituation 2 = Plantado, ProductionSystem 2 = Irrigado, cultura Soja do catálogo)
INSERT INTO "Plot" ("Id", "FarmId", "CropId", "PlotSituationId", "ProductionSystemId", "Identifier",
                    "AreaHectares", "PlantingDate", "EstimatedHarvestDate", "CycleDays", "SeedVariety", "PolygonWkt")
VALUES ('63abc434-52b8-4c43-bf46-617b511e5aa5', '7f2cc962-aeac-4863-9715-a49eed56f99c',
        (SELECT "Id" FROM "Crop" WHERE "Name" = 'Soja' LIMIT 1), 2, 2, 'Talhao A1',
        45.00, '2026-01-15', '2026-05-15', 120, 'M7739IPRO',
        'POLYGON((-47.82 -21.17, -47.81 -21.17, -47.81 -21.18, -47.82 -21.18, -47.82 -21.17))')
ON CONFLICT DO NOTHING;

-- Apólice VIGENTE (PolicySituation 1 = Vigente, AllowsClaim=true) — policyId da demo
INSERT INTO "Policy" ("Id", "PolicyNumber", "PlotId", "InsurerId", "InsuranceId", "PolicySituationId",
                      "InsuredAmount", "TotalPremium", "MonthlyPremium", "DeductiblePct", "MaxCoverage",
                      "StartDate", "EndDate")
VALUES ('ff6b203e-5ece-41a5-b992-854757f287ed', 'ZH-APO-2026-000001',
        '63abc434-52b8-4c43-bf46-617b511e5aa5', '50124578-a7c1-4d63-9b5a-dc097c3f729c',
        '131156a3-903b-4e89-8ef2-621fbc97f89c', 1, 292500.00, 13162.50, 1096.88, 10.00, 292500.00,
        '2026-01-15', '2026-12-31')
ON CONFLICT DO NOTHING;

-- Item da apólice: cobertura de Seca (ClaimEventType 1)
INSERT INTO "PolicyItem" ("PolicyId", "ClaimEventTypeId", "CoveragePct", "MaxCoverageAmount", "Notes")
SELECT 'ff6b203e-5ece-41a5-b992-854757f287ed', 1, 100.00, 292500.00, 'Cobertura total para evento de Seca'
WHERE NOT EXISTS (
  SELECT 1 FROM "PolicyItem"
  WHERE "PolicyId" = 'ff6b203e-5ece-41a5-b992-854757f287ed' AND "ClaimEventTypeId" = 1
);

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
