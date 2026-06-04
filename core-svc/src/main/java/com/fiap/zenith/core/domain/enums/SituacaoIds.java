package com.fiap.zenith.core.domain.enums;

/**
 * IDs das situações de lookup conforme o seed canônico ({@code db/seed/zenith_harvest_seed.sql}).
 * Centraliza os valores para que mudanças no seed quebrem em UM lugar, não silenciosamente
 * espalhadas pelos services.
 */
public final class SituacaoIds {

    // InsuranceQuoteSituation: 1=Em aberto, 2=Aceita, 3=Recusada, 4=Expirada
    public static final int COTACAO_ACEITA = 2;

    // PolicySituation: 1=Vigente, 2=Aguardando pagamento, 3=Cancelada, 4=Expirada
    public static final int APOLICE_CANCELADA = 3;

    // ClaimSituation: 1=Aberto, 2=Em análise, 3=Aprovado, 4=Rejeitado, 5=Pago
    public static final int SINISTRO_EM_ANALISE = 2;
    public static final int SINISTRO_APROVADO   = 3;
    public static final int SINISTRO_REJEITADO  = 4;

    // PaymentSituation: 1=Pendente, 2=Processando, 3=Confirmado, 4=Falhou, 5=Estornado
    public static final int PAGAMENTO_CONFIRMADO = 3;

    private SituacaoIds() {
        // classe de constantes — não instanciar
    }
}
