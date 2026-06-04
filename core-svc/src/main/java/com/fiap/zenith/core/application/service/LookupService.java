package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.domain.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Expõe todos os lookups do domínio como listas cacheadas.
 * Lookups são imutáveis em runtime — o cache nunca expira (reinício invalida).
 */
@Service
@Transactional(readOnly = true)
public class LookupService {

    public record LookupItem(Integer id, String description, boolean active) {}
    public record BiomeItem(Integer id, String description, boolean active, java.math.BigDecimal regionalRiskFactor) {}
    public record ProductionSystemItem(Integer id, String description, boolean active, java.math.BigDecimal premiumDiscount) {}
    public record SituationItem(Integer id, String description, boolean active, Boolean isTerminal) {}
    public record PolicySituationItem(Integer id, String description, boolean active, Boolean allowsClaim, Boolean isTerminal) {}
    public record ClaimSituationItem(Integer id, String description, boolean active, Boolean allowsClaim, Boolean isTerminal) {}
    public record ClaimEventTypeItem(Integer id, String description, boolean active, Boolean requiresPhoto) {}
    public record SatelliteSourceItem(Integer id, String description, boolean active, String operator, String resolution, Integer revisitFrequencyDays) {}
    public record SatelliteClassItem(Integer id, String description, boolean active, Integer severity) {}
    public record AlertSeverityItem(Integer id, String description, Integer level, String colorHex, Boolean pushNotify, Boolean smsNotify) {}
    public record PaymentTypeItem(Integer id, String description, boolean active, String direction) {}
    public record PaymentSituationItem(Integer id, String description, boolean active, Boolean isTerminal, Boolean isSuccess) {}

    private final BiomeRepository biomeRepo;
    private final ProductionSystemRepository productionSystemRepo;
    private final PlotSituationRepository plotSituationRepo;
    private final InsurerSituationRepository insurerSituationRepo;
    private final InsuranceSituationRepository insuranceSituationRepo;
    private final InsuranceQuoteSituationRepository quotesSituationRepo;
    private final PolicySituationRepository policySituationRepo;
    private final ClaimSituationRepository claimSituationRepo;
    private final ClaimEventTypeRepository claimEventTypeRepo;
    private final ClaimCategoryRepository claimCategoryRepo;
    private final ClaimSubCategoryRepository claimSubCategoryRepo;
    private final RejectionReasonRepository rejectionReasonRepo;
    private final SatelliteSourceRepository satelliteSourceRepo;
    private final SatelliteClassRepository satelliteClassRepo;
    private final AlertTypeRepository alertTypeRepo;
    private final AlertSeverityRepository alertSeverityRepo;
    private final AlertSituationRepository alertSituationRepo;
    private final PaymentTypeRepository paymentTypeRepo;
    private final PaymentSituationRepository paymentSituationRepo;

    public LookupService(BiomeRepository biomeRepo,
                         ProductionSystemRepository productionSystemRepo,
                         PlotSituationRepository plotSituationRepo,
                         InsurerSituationRepository insurerSituationRepo,
                         InsuranceSituationRepository insuranceSituationRepo,
                         InsuranceQuoteSituationRepository quotesSituationRepo,
                         PolicySituationRepository policySituationRepo,
                         ClaimSituationRepository claimSituationRepo,
                         ClaimEventTypeRepository claimEventTypeRepo,
                         ClaimCategoryRepository claimCategoryRepo,
                         ClaimSubCategoryRepository claimSubCategoryRepo,
                         RejectionReasonRepository rejectionReasonRepo,
                         SatelliteSourceRepository satelliteSourceRepo,
                         SatelliteClassRepository satelliteClassRepo,
                         AlertTypeRepository alertTypeRepo,
                         AlertSeverityRepository alertSeverityRepo,
                         AlertSituationRepository alertSituationRepo,
                         PaymentTypeRepository paymentTypeRepo,
                         PaymentSituationRepository paymentSituationRepo) {
        this.biomeRepo = biomeRepo;
        this.productionSystemRepo = productionSystemRepo;
        this.plotSituationRepo = plotSituationRepo;
        this.insurerSituationRepo = insurerSituationRepo;
        this.insuranceSituationRepo = insuranceSituationRepo;
        this.quotesSituationRepo = quotesSituationRepo;
        this.policySituationRepo = policySituationRepo;
        this.claimSituationRepo = claimSituationRepo;
        this.claimEventTypeRepo = claimEventTypeRepo;
        this.claimCategoryRepo = claimCategoryRepo;
        this.claimSubCategoryRepo = claimSubCategoryRepo;
        this.rejectionReasonRepo = rejectionReasonRepo;
        this.satelliteSourceRepo = satelliteSourceRepo;
        this.satelliteClassRepo = satelliteClassRepo;
        this.alertTypeRepo = alertTypeRepo;
        this.alertSeverityRepo = alertSeverityRepo;
        this.alertSituationRepo = alertSituationRepo;
        this.paymentTypeRepo = paymentTypeRepo;
        this.paymentSituationRepo = paymentSituationRepo;
    }

    @Cacheable("biomes")
    public List<BiomeItem> listarBiomes() {
        return biomeRepo.findAllByActiveTrue().stream()
                .map(b -> new BiomeItem(b.getId(), b.getDescription(), b.getActive(), b.getRegionalRiskFactor()))
                .toList();
    }

    @Cacheable("production-systems")
    public List<ProductionSystemItem> listarProductionSystems() {
        return productionSystemRepo.findAllByActiveTrue().stream()
                .map(p -> new ProductionSystemItem(p.getId(), p.getDescription(), p.getActive(), p.getPremiumDiscount()))
                .toList();
    }

    @Cacheable("plot-situations")
    public List<SituationItem> listarPlotSituations() {
        return plotSituationRepo.findAllByActiveTrue().stream()
                .map(s -> new SituationItem(s.getId(), s.getDescription(), s.getActive(), s.getIsTerminal()))
                .toList();
    }

    @Cacheable("insurer-situations")
    public List<LookupItem> listarInsurerSituations() {
        return insurerSituationRepo.findAllByActiveTrue().stream()
                .map(s -> new LookupItem(s.getId(), s.getDescription(), s.getActive()))
                .toList();
    }

    @Cacheable("insurance-situations")
    public List<LookupItem> listarInsuranceSituations() {
        return insuranceSituationRepo.findAllByActiveTrue().stream()
                .map(s -> new LookupItem(s.getId(), s.getDescription(), s.getActive()))
                .toList();
    }

    @Cacheable("quote-situations")
    public List<SituationItem> listarQuoteSituations() {
        return quotesSituationRepo.findAllByActiveTrue().stream()
                .map(s -> new SituationItem(s.getId(), s.getDescription(), s.getActive(), s.getIsTerminal()))
                .toList();
    }

    @Cacheable("policy-situations")
    public List<PolicySituationItem> listarPolicySituations() {
        return policySituationRepo.findAllByActiveTrue().stream()
                .map(s -> new PolicySituationItem(s.getId(), s.getDescription(), s.getActive(), s.getAllowsClaim(), s.getIsTerminal()))
                .toList();
    }

    @Cacheable("claim-situations")
    public List<ClaimSituationItem> listarClaimSituations() {
        return claimSituationRepo.findAllByActiveTrue().stream()
                .map(s -> new ClaimSituationItem(s.getId(), s.getDescription(), s.getActive(), s.getAllowsClaim(), s.getIsTerminal()))
                .toList();
    }

    @Cacheable("claim-event-types")
    public List<ClaimEventTypeItem> listarClaimEventTypes() {
        return claimEventTypeRepo.findAllByActiveTrue().stream()
                .map(e -> new ClaimEventTypeItem(e.getId(), e.getDescription(), e.getActive(), e.getRequiresPhoto()))
                .toList();
    }

    @Cacheable("claim-categories")
    public List<LookupItem> listarClaimCategories() {
        return claimCategoryRepo.findAllByActiveTrue().stream()
                .map(c -> new LookupItem(c.getId(), c.getDescription(), c.getActive()))
                .toList();
    }

    @Cacheable("claim-subcategories")
    public List<LookupItem> listarClaimSubCategories() {
        return claimSubCategoryRepo.findAllByActiveTrue().stream()
                .map(c -> new LookupItem(c.getId(), c.getDescription(), c.getActive()))
                .toList();
    }

    @Cacheable("rejection-reasons")
    public List<LookupItem> listarRejectionReasons() {
        return rejectionReasonRepo.findAllByActiveTrue().stream()
                .map(r -> new LookupItem(r.getId(), r.getDescription(), r.getActive()))
                .toList();
    }

    @Cacheable("satellite-sources")
    public List<SatelliteSourceItem> listarSatelliteSources() {
        return satelliteSourceRepo.findAllByActiveTrue().stream()
                .map(s -> new SatelliteSourceItem(s.getId(), s.getDescription(), s.getActive(),
                        s.getOperator(), s.getResolution(), s.getRevisitFrequencyDays()))
                .toList();
    }

    @Cacheable("satellite-classes")
    public List<SatelliteClassItem> listarSatelliteClasses() {
        return satelliteClassRepo.findAllByActiveTrue().stream()
                .map(s -> new SatelliteClassItem(s.getId(), s.getDescription(), s.getActive(), s.getSeverity()))
                .toList();
    }

    @Cacheable("alert-types")
    public List<LookupItem> listarAlertTypes() {
        return alertTypeRepo.findAllByActiveTrue().stream()
                .map(a -> new LookupItem(a.getId(), a.getDescription(), a.getActive()))
                .toList();
    }

    @Cacheable("alert-severities")
    public List<AlertSeverityItem> listarAlertSeverities() {
        return alertSeverityRepo.findAll().stream()
                .map(a -> new AlertSeverityItem(a.getId(), a.getDescription(), a.getLevel(),
                        a.getColorHex(), a.getPushNotify(), a.getSmsNotify()))
                .toList();
    }

    @Cacheable("alert-situations")
    public List<SituationItem> listarAlertSituations() {
        return alertSituationRepo.findAllByActiveTrue().stream()
                .map(s -> new SituationItem(s.getId(), s.getDescription(), s.getActive(), s.getIsTerminal()))
                .toList();
    }

    @Cacheable("payment-types")
    public List<PaymentTypeItem> listarPaymentTypes() {
        return paymentTypeRepo.findAllByActiveTrue().stream()
                .map(p -> new PaymentTypeItem(p.getId(), p.getDescription(), p.getActive(), p.getDirection()))
                .toList();
    }

    @Cacheable("payment-situations")
    public List<PaymentSituationItem> listarPaymentSituations() {
        return paymentSituationRepo.findAllByActiveTrue().stream()
                .map(p -> new PaymentSituationItem(p.getId(), p.getDescription(), p.getActive(),
                        p.getIsTerminal(), p.getIsSuccess()))
                .toList();
    }
}
