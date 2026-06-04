package com.fiap.zenith.core.web.controller;

import com.fiap.zenith.core.application.service.PolicyItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PolicyItemController.class)
class PolicyItemControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PolicyItemService policyItemService;

    @Test
    void criarDeveRetornarUnauthorizedQuandoNaoHouverAutenticacao() throws Exception {
        String request = """
                {
                  "policyId": "%s",
                  "claimEventTypeId": 4,
                  "coveragePct": 85.50
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/policy-items")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(policyItemService);
    }
}
