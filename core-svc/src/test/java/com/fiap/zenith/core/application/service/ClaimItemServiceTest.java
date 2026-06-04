package com.fiap.zenith.core.application.service;

import com.fiap.zenith.core.application.dto.ClaimItemResponse;
import com.fiap.zenith.core.application.dto.CreateClaimItemRequest;
import com.fiap.zenith.core.application.dto.UpdateClaimItemRequest;
import com.fiap.zenith.core.application.mapper.ClaimItemMapper;
import com.fiap.zenith.core.domain.entity.ClaimItem;
import com.fiap.zenith.core.domain.repository.ClaimEventTypeRepository;
import com.fiap.zenith.core.domain.repository.ClaimItemRepository;
import com.fiap.zenith.core.domain.repository.ClaimRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimItemServiceTest {

    @Mock
    private ClaimItemRepository claimItemRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimEventTypeRepository claimEventTypeRepository;

    @Mock
    private ClaimItemMapper claimItemMapper;

    @InjectMocks
    private ClaimItemService service;

    @Test
    void criarDeveSalvarItemQuandoDadosForemValidos() {
        UUID claimId = UUID.randomUUID();
        CreateClaimItemRequest request = new CreateClaimItemRequest(
                claimId,
                4,
                new BigDecimal("12.50"),
                new BigDecimal("27.30"),
                new BigDecimal("0.721"),
                new BigDecimal("0.531"),
                new BigDecimal("4200.00"),
                "Geada setorial"
        );

        when(claimRepository.findByIdAndDeletedAtIsNull(claimId)).thenReturn(Optional.of(mock()));
        when(claimEventTypeRepository.existsByIdAndActiveTrueAndDeletedAtIsNull(4)).thenReturn(true);
        when(claimItemRepository.save(any(ClaimItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(claimItemMapper.toResponse(any(ClaimItem.class))).thenAnswer(invocation -> {
            ClaimItem item = invocation.getArgument(0);
            return new ClaimItemResponse(
                    item.getId(),
                    item.getClaimId(),
                    item.getClaimEventTypeId(),
                    item.getAffectedAreaHa(),
                    item.getLossPct(),
                    item.getNdviBefore(),
                    item.getNdviAfter(),
                    item.getItemAmount(),
                    item.getDescription()
            );
        });

        ClaimItemResponse response = service.criar(request);

        assertThat(response.claimId()).isEqualTo(claimId);
        assertThat(response.lossPct()).isEqualByComparingTo("27.30");
        assertThat(response.ndviAfter()).isEqualByComparingTo("0.531");
    }

    @Test
    void criarDeveFalharQuandoSinistroNaoExistir() {
        UUID claimId = UUID.randomUUID();
        CreateClaimItemRequest request = new CreateClaimItemRequest(claimId, 4, null, null, null, null, null, null);

        when(claimRepository.findByIdAndDeletedAtIsNull(claimId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Sinistro não encontrado");
    }

    @Test
    void listarDeveUsarFiltroPorSinistroQuandoInformado() {
        UUID claimId = UUID.randomUUID();
        ClaimItem item = ClaimItem.create(
                claimId,
                2,
                new BigDecimal("5.00"),
                new BigDecimal("12.00"),
                new BigDecimal("0.654"),
                new BigDecimal("0.432"),
                new BigDecimal("980.00"),
                "Seca"
        );
        PageRequest pageable = PageRequest.of(0, 20);
        when(claimItemRepository.findAllByClaimIdWithActiveClaim(claimId, pageable))
                .thenReturn(new PageImpl<>(List.of(item), pageable, 1));
        when(claimItemMapper.toResponse(item)).thenReturn(new ClaimItemResponse(
                item.getId(),
                item.getClaimId(),
                item.getClaimEventTypeId(),
                item.getAffectedAreaHa(),
                item.getLossPct(),
                item.getNdviBefore(),
                item.getNdviAfter(),
                item.getItemAmount(),
                item.getDescription()
        ));

        Page<ClaimItemResponse> page = service.listar(claimId, pageable);

        verify(claimItemRepository).findAllByClaimIdWithActiveClaim(claimId, pageable);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).description()).isEqualTo("Seca");
    }

    @Test
    void atualizarDeveSubstituirMetricasDoItem() {
        UUID itemId = UUID.randomUUID();
        ClaimItem item = ClaimItem.create(
                UUID.randomUUID(),
                8,
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                new BigDecimal("0.700"),
                new BigDecimal("0.500"),
                new BigDecimal("1000.00"),
                "Antes"
        );
        when(claimItemRepository.findAccessibleById(itemId)).thenReturn(Optional.of(item));
        when(claimItemMapper.toResponse(any(ClaimItem.class))).thenAnswer(invocation -> {
            ClaimItem mapped = invocation.getArgument(0);
            return new ClaimItemResponse(
                    mapped.getId(),
                    mapped.getClaimId(),
                    mapped.getClaimEventTypeId(),
                    mapped.getAffectedAreaHa(),
                    mapped.getLossPct(),
                    mapped.getNdviBefore(),
                    mapped.getNdviAfter(),
                    mapped.getItemAmount(),
                    mapped.getDescription()
            );
        });

        ClaimItemResponse response = service.atualizar(itemId, new UpdateClaimItemRequest(
                new BigDecimal("11.00"),
                new BigDecimal("31.00"),
                new BigDecimal("0.650"),
                new BigDecimal("0.420"),
                new BigDecimal("1500.00"),
                "Depois"
        ));

        assertThat(item.getAffectedAreaHa()).isEqualByComparingTo("11.00");
        assertThat(item.getLossPct()).isEqualByComparingTo("31.00");
        assertThat(item.getNdviAfter()).isEqualByComparingTo("0.420");
        assertThat(item.getDescription()).isEqualTo("Depois");
        assertThat(response.itemAmount()).isEqualByComparingTo("1500.00");
    }
}
