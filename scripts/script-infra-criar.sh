#!/usr/bin/env bash
# Provisiona a infraestrutura ZenithHarvest na Azure.
#
# Uso: ./scripts/infra-criar.sh [location]
# Pre-requisito: az login ja executado e subscription selecionada.
#
# O que este script cria:
#   - Resource Group
#   - Container Registry (ACR)
#   - Log Analytics + Application Insights
#   - Container Apps Environment (cae-zenithharvest)
#   - MongoDB como Container App (ingress interno, porta 27017)
#   - PostgreSQL Flexible Server (Azure managed)
#   - Container Apps placeholder para gateway, core-svc e analise-svc
#   - Service Principal com permissao de Contributor + AcrPush
#
# O que o pipeline cuida (nao esta aqui):
#   - RabbitMQ como Container App (criado no 1o deploy do pipeline)
#   - Build e push das imagens Docker
#   - Deploy dos 3 microsservicos Java
set -euo pipefail

LOCATION="${1:-canadacentral}"

# ── Nomes dos recursos (devem bater com o azure-pipelines.yml) ───────────────
RG="rg-zenithharvest"
ACR="acrzenith"
CAE="cae-zenithharvest"
ACA_MONGO="aca-zenith-mongo"
ACA_GATEWAY="aca-zenith-gateway"
ACA_CORE="aca-zenith-core-svc"
ACA_ANALISE="aca-zenith-analise-svc"
PSQL_SERVER="psql-zenithharvest-$RANDOM"
PSQL_DB="db-zenithharvest"
PSQL_ADMIN="zenith_admin"
LA_WORKSPACE="log-zenithharvest"
AI_COMPONENT="ai-zenithharvest"
SP_NAME="sp-zenithharvest-cicd"
MONGO_USER="zenith"
# ─────────────────────────────────────────────────────────────────────────────

echo "╔══════════════════════════════════════════════╗"
echo "║  ZenithHarvest — Provisionando infra Azure  ║"
echo "║  Location: $LOCATION"
echo "╚══════════════════════════════════════════════╝"
echo ""

SUBSCRIPTION_ID=$(az account show --query id -o tsv)
echo "Subscription: $SUBSCRIPTION_ID"
echo ""

# ── 1. Resource Group ─────────────────────────────────────────────────────────
echo "[1/9] Resource Group: $RG"
az group create --name "$RG" --location "$LOCATION" --output none
echo "      OK"

# ── 2. Container Registry ─────────────────────────────────────────────────────
echo "[2/9] ACR: $ACR"
az acr create \
  --resource-group "$RG" \
  --name "$ACR" \
  --sku Basic \
  --admin-enabled true \
  --output none
echo "      OK — ${ACR}.azurecr.io"

# ── 3. Log Analytics + Application Insights ───────────────────────────────────
echo "[3/9] Log Analytics: $LA_WORKSPACE"
az monitor log-analytics workspace create \
  --resource-group "$RG" \
  --workspace-name "$LA_WORKSPACE" \
  --location "$LOCATION" \
  --output none

LA_ID=$(az monitor log-analytics workspace show \
  --resource-group "$RG" --workspace-name "$LA_WORKSPACE" \
  --query customerId -o tsv)

LA_KEY=$(az monitor log-analytics workspace get-shared-keys \
  --resource-group "$RG" --workspace-name "$LA_WORKSPACE" \
  --query primarySharedKey -o tsv)

az monitor app-insights component create \
  --app "$AI_COMPONENT" \
  --location "$LOCATION" \
  --resource-group "$RG" \
  --application-type web \
  --workspace "$LA_WORKSPACE" \
  --output none
echo "      OK"

# ── 4. Container Apps Environment ─────────────────────────────────────────────
echo "[4/9] Container Apps Environment: $CAE"
az containerapp env create \
  --name "$CAE" \
  --resource-group "$RG" \
  --location "$LOCATION" \
  --logs-workspace-id "$LA_ID" \
  --logs-workspace-key "$LA_KEY" \
  --output none
echo "      OK"

# ── 5. MongoDB como Container App ─────────────────────────────────────────────
# Ingress interno TCP — acessivel pelos outros Container Apps via nome do app.
echo "[5/9] MongoDB Container App: $ACA_MONGO"
MONGO_PASSWORD="$(openssl rand -base64 18 | tr -d '=/+' | head -c 20)"

az containerapp create \
  --name "$ACA_MONGO" \
  --resource-group "$RG" \
  --environment "$CAE" \
  --image mongo:7 \
  --ingress internal \
  --target-port 27017 \
  --transport tcp \
  --min-replicas 1 \
  --max-replicas 1 \
  --env-vars \
    MONGO_INITDB_ROOT_USERNAME="$MONGO_USER" \
    MONGO_INITDB_ROOT_PASSWORD="$MONGO_PASSWORD" \
    MONGO_INITDB_DATABASE=zenith \
  --output none
echo "      OK — hostname interno: $ACA_MONGO"

# ── 6. PostgreSQL Flexible Server ─────────────────────────────────────────────
echo "[6/9] PostgreSQL Flexible Server: $PSQL_SERVER"
PSQL_PASSWORD="Nx$(openssl rand -base64 14 | tr -d '=/+' | head -c 12)7z@Q1"

az postgres flexible-server create \
  --name "$PSQL_SERVER" \
  --resource-group "$RG" \
  --location "$LOCATION" \
  --admin-user "$PSQL_ADMIN" \
  --admin-password "$PSQL_PASSWORD" \
  --sku-name Standard_B1ms \
  --tier Burstable \
  --storage-size 32 \
  --version 16 \
  --public-access 0.0.0.0 \
  --output none

az postgres flexible-server db create \
  --resource-group "$RG" \
  --server-name "$PSQL_SERVER" \
  --database-name "$PSQL_DB" \
  --output none
echo "      OK — ${PSQL_SERVER}.postgres.database.azure.com"

# ── 7. Container Apps placeholder (gateway, core-svc, analise-svc) ────────────
# O pipeline faz o deploy real; aqui so cria o recurso com imagem placeholder.
echo "[7/9] Container Apps (placeholders): gateway / core-svc / analise-svc"
PLACEHOLDER="mcr.microsoft.com/azuredocs/containerapps-helloworld:latest"

az containerapp create \
  --name "$ACA_CORE" \
  --resource-group "$RG" \
  --environment "$CAE" \
  --image "$PLACEHOLDER" \
  --ingress internal \
  --target-port 8081 \
  --min-replicas 0 --max-replicas 3 \
  --output none

az containerapp create \
  --name "$ACA_ANALISE" \
  --resource-group "$RG" \
  --environment "$CAE" \
  --image "$PLACEHOLDER" \
  --ingress internal \
  --target-port 8082 \
  --min-replicas 0 --max-replicas 3 \
  --output none

az containerapp create \
  --name "$ACA_GATEWAY" \
  --resource-group "$RG" \
  --environment "$CAE" \
  --image "$PLACEHOLDER" \
  --ingress external \
  --target-port 8080 \
  --min-replicas 0 --max-replicas 3 \
  --output none
echo "      OK"

# ── 8. Service Principal para CI/CD ───────────────────────────────────────────
echo "[8/9] Service Principal: $SP_NAME"
SP_JSON=$(az ad sp create-for-rbac \
  --name "$SP_NAME" \
  --role Contributor \
  --scopes "/subscriptions/$SUBSCRIPTION_ID/resourceGroups/$RG")

SP_CLIENT_ID=$(echo "$SP_JSON"     | python3 -c "import sys,json;d=json.load(sys.stdin);print(d['appId'])")
SP_CLIENT_SECRET=$(echo "$SP_JSON" | python3 -c "import sys,json;d=json.load(sys.stdin);print(d['password'])")
SP_TENANT_ID=$(echo "$SP_JSON"     | python3 -c "import sys,json;d=json.load(sys.stdin);print(d['tenant'])")

ACR_ID=$(az acr show --name "$ACR" --resource-group "$RG" --query id -o tsv)
az role assignment create \
  --assignee "$SP_CLIENT_ID" \
  --role AcrPush \
  --scope "$ACR_ID" \
  --output none
echo "      OK"

# ── 9. Credenciais do ACR ─────────────────────────────────────────────────────
echo "[9/9] Lendo credenciais do ACR"
ACR_USERNAME=$(az acr credential show --name "$ACR" --query username -o tsv)
ACR_PASSWORD=$(az acr credential show --name "$ACR" --query "passwords[0].value" -o tsv)
echo "      OK"

# ── Resumo ────────────────────────────────────────────────────────────────────
echo ""
echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║           ZenithHarvest — Infra criada com sucesso!            ║"
echo "╠══════════════════════════════════════════════════════════════════╣"
echo "║  Resource Group  : $RG"
echo "║  ACR             : ${ACR}.azurecr.io"
echo "║  Env             : $CAE"
echo "║  Container Apps  : $ACA_GATEWAY  (external)"
echo "║                  : $ACA_CORE     (internal)"
echo "║                  : $ACA_ANALISE  (internal)"
echo "║                  : $ACA_MONGO    (internal TCP 27017)"
echo "║  PostgreSQL      : ${PSQL_SERVER}.postgres.database.azure.com"
echo "║  Database        : $PSQL_DB"
echo "╠══════════════════════════════════════════════════════════════════╣"
echo "║  Cole as variaveis abaixo no Azure DevOps:                     ║"
echo "║  Pipelines > Edit > Variables  (marcar como secret)            ║"
echo "╠══════════════════════════════════════════════════════════════════╣"
echo ""
echo "  ACR_USERNAME               = $ACR_USERNAME"
echo "  ACR_PASSWORD               = $ACR_PASSWORD"
echo "  AZURE_CLIENT_ID            = $SP_CLIENT_ID"
echo "  AZURE_CLIENT_SECRET        = $SP_CLIENT_SECRET"
echo "  AZURE_TENANT_ID            = $SP_TENANT_ID"
echo "  AZURE_SUBSCRIPTION_ID      = $SUBSCRIPTION_ID"
echo "  PSQL_SERVER                = ${PSQL_SERVER}.postgres.database.azure.com"
echo "  PSQL_ADMIN_PASSWORD        = $PSQL_PASSWORD"
echo "  MONGO_HOST                 = $ACA_MONGO"
echo "  MONGO_USERNAME             = $MONGO_USER"
echo "  MONGO_PASSWORD             = $MONGO_PASSWORD"
echo "  OLLAMA_BASE_URL            = http://aca-zenith-ollama:11434  # se subir Ollama como Container App"
echo ""
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""
echo "Proximo passo: rode o pipeline no Azure DevOps (push na branch main)"
echo "O pipeline criara o RabbitMQ automaticamente no primeiro deploy."
