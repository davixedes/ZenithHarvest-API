package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.dto.CreatePolicyItemRequest;
import com.fiap.zenith.core.application.dto.PolicyItemResponse;
import com.fiap.zenith.core.application.service.PolicyItemService;
import com.fiap.zenith.core.web.handler.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PolicyItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PolicyItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PolicyItemService policyItemService;

    @Test
    void criarDeveRetornarCreatedComLinks() throws Exception {
        UUID itemId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        PolicyItemResponse response = new PolicyItemResponse(
                itemId,
                policyId,
                9,
                new BigDecimal("87.50"),
                new BigDecimal("30000.00"),
                "Cobertura adicional"
        );
        when(policyItemService.criar(any(CreatePolicyItemRequest.class))).thenReturn(response);

        CreatePolicyItemRequest request = new CreatePolicyItemRequest(
                policyId,
                9,
                new BigDecimal("87.50"),
                new BigDecimal("30000.00"),
                "Cobertura adicional"
        );

        mockMvc.perform(post("/api/policy-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/policy-items/" + itemId))
                .andExpect(jsonPath("$.id").value(itemId.toString()))
                .andExpect(jsonPath("$.policyId").value(policyId.toString()))
                .andExpect(jsonPath("$._links.self.href").value("http://localhost/api/policy-items/" + itemId))
                .andExpect(jsonPath("$._links.policy-items.href")
                        .value("http://localhost/api/policy-items?policyId=" + policyId));
    }

    @Test
    void criarDeveRetornarBadRequestQuandoPayloadForInvalido() throws Exception {
        String invalidJson = """
                {
                  "coveragePct": 1200.123,
                  "notes": "%s"
                }
                """.formatted("x".repeat(501));

        mockMvc.perform(post("/api/policy-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erro de validação"))
                .andExpect(jsonPath("$.errors.policyId").exists())
                .andExpect(jsonPath("$.errors.claimEventTypeId").exists())
                .andExpect(jsonPath("$.errors.notes").exists());
    }
}
