package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.CreatePolicyItemRequest;
import com.fiap.zenith.core.application.dto.PolicyItemResponse;
import com.fiap.zenith.core.application.dto.UpdatePolicyItemRequest;
import com.fiap.zenith.core.application.exception.DuplicateResourceException;
import com.fiap.zenith.core.application.mapper.PolicyItemMapper;
import com.fiap.zenith.core.domain.entity.PolicyItem;
import com.fiap.zenith.core.domain.repository.ClaimEventTypeRepository;
import com.fiap.zenith.core.domain.repository.PolicyItemRepository;
import com.fiap.zenith.core.domain.repository.PolicyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyItemServiceTest {

    @Mock
    private PolicyItemRepository policyItemRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ClaimEventTypeRepository claimEventTypeRepository;

    @Mock
    private PolicyItemMapper policyItemMapper;

    @InjectMocks
    private PolicyItemService service;

    @Test
    void criarDeveSalvarItemQuandoDadosForemValidos() {
        UUID policyId = UUID.randomUUID();
        CreatePolicyItemRequest request = new CreatePolicyItemRequest(
                policyId,
                7,
                null,
                new BigDecimal("15000.00"),
                "Cobertura NDVI"
        );

        when(policyRepository.findByIdAndDeletedAtIsNull(policyId)).thenReturn(Optional.of(mock()));
        when(claimEventTypeRepository.existsByIdAndActiveTrueAndDeletedAtIsNull(7)).thenReturn(true);
        when(policyItemRepository.existsByPolicyIdAndClaimEventTypeId(policyId, 7)).thenReturn(false);
        when(policyItemRepository.save(any(PolicyItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(policyItemMapper.toResponse(any(PolicyItem.class))).thenAnswer(invocation -> {
            PolicyItem item = invocation.getArgument(0);
            return new PolicyItemResponse(
                    item.getId(),
                    item.getPolicyId(),
                    item.getClaimEventTypeId(),
                    item.getCoveragePct(),
                    item.getMaxCoverageAmount(),
                    item.getNotes()
            );
        });

        PolicyItemResponse response = service.criar(request);

        ArgumentCaptor<PolicyItem> captor = ArgumentCaptor.forClass(PolicyItem.class);
        verify(policyItemRepository).save(captor.capture());
        PolicyItem saved = captor.getValue();

        assertThat(saved.getPolicyId()).isEqualTo(policyId);
        assertThat(saved.getClaimEventTypeId()).isEqualTo(7);
        assertThat(saved.getCoveragePct()).isEqualByComparingTo("100");
        assertThat(saved.getMaxCoverageAmount()).isEqualByComparingTo("15000.00");
        assertThat(saved.getNotes()).isEqualTo("Cobertura NDVI");
        assertThat(response.policyId()).isEqualTo(policyId);
        assertThat(response.coveragePct()).isEqualByComparingTo("100");
    }

    @Test
    void criarDeveFalharQuandoCoberturaDuplicada() {
        UUID policyId = UUID.randomUUID();
        CreatePolicyItemRequest request = new CreatePolicyItemRequest(
                policyId,
                3,
                new BigDecimal("75.00"),
                null,
                null
        );

        when(policyRepository.findByIdAndDeletedAtIsNull(policyId)).thenReturn(Optional.of(mock()));
        when(claimEventTypeRepository.existsByIdAndActiveTrueAndDeletedAtIsNull(3)).thenReturn(true);
        when(policyItemRepository.existsByPolicyIdAndClaimEventTypeId(policyId, 3)).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("já existe");

        verify(policyItemRepository, never()).save(any());
    }

    @Test
    void atualizarDeveManterCoveragePctQuandoNaoVierNoRequest() {
        UUID itemId = UUID.randomUUID();
        PolicyItem item = PolicyItem.create(
                UUID.randomUUID(),
                5,
                new BigDecimal("80.00"),
                new BigDecimal("10000.00"),
                "Original"
        );
        when(policyItemRepository.findAccessibleById(itemId)).thenReturn(Optional.of(item));
        when(policyItemMapper.toResponse(any(PolicyItem.class))).thenAnswer(invocation -> {
            PolicyItem mapped = invocation.getArgument(0);
            return new PolicyItemResponse(
                    mapped.getId(),
                    mapped.getPolicyId(),
                    mapped.getClaimEventTypeId(),
                    mapped.getCoveragePct(),
                    mapped.getMaxCoverageAmount(),
                    mapped.getNotes()
            );
        });

        PolicyItemResponse response = service.atualizar(itemId, new UpdatePolicyItemRequest(
                null,
                new BigDecimal("25000.00"),
                "Atualizado"
        ));

        assertThat(item.getCoveragePct()).isEqualByComparingTo("80.00");
        assertThat(item.getMaxCoverageAmount()).isEqualByComparingTo("25000.00");
        assertThat(item.getNotes()).isEqualTo("Atualizado");
        assertThat(response.maxCoverageAmount()).isEqualByComparingTo("25000.00");
    }

    @Test
    void criarDeveFalharQuandoTipoEventoNaoExistir() {
        UUID policyId = UUID.randomUUID();
        CreatePolicyItemRequest request = new CreatePolicyItemRequest(policyId, 99, null, null, null);

        when(policyRepository.findByIdAndDeletedAtIsNull(policyId)).thenReturn(Optional.of(mock()));
        when(claimEventTypeRepository.existsByIdAndActiveTrueAndDeletedAtIsNull(99)).thenReturn(false);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tipo de evento de sinistro não encontrado");
    }
}
