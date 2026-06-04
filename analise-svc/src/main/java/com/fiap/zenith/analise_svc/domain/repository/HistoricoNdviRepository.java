package com.fiap.zenith.analise_svc.domain.repository;

import com.fiap.zenith.analise_svc.domain.document.HistoricoNdviDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface HistoricoNdviRepository extends MongoRepository<HistoricoNdviDocument, String> {
    List<HistoricoNdviDocument> findAllByPlotIdOrderByImageDateDesc(UUID plotId);
    List<HistoricoNdviDocument> findAllByClaimIdOrderByImageDateDesc(UUID claimId);
}
