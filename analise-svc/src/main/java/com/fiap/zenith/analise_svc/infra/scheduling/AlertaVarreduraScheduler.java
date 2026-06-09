package com.fiap.zenith.analise_svc.infra.scheduling;

import com.fiap.zenith.analise_svc.application.service.AlertaDeteccaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Agendamento automático da varredura de alertas preventivos.
 * Cron configurável via zenith.alerta.varredura.cron (default: diário às 6h).
 */
@Component
public class AlertaVarreduraScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertaVarreduraScheduler.class);

    private final AlertaDeteccaoService deteccaoService;

    public AlertaVarreduraScheduler(AlertaDeteccaoService deteccaoService) {
        this.deteccaoService = deteccaoService;
    }

    @Scheduled(cron = "${zenith.alerta.varredura.cron:0 0 6 * * *}")
    public void executar() {
        log.info("Scheduler: iniciando varredura automática de alertas preventivos");
        deteccaoService.executarVarredura();
    }
}
