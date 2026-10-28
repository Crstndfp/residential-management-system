package com.residencial.finance.infrastructure.web;

import com.residencial.finance.application.dto.AccountStatementResponse;
import com.residencial.finance.application.dto.SolvencyStatus;
import com.residencial.finance.application.usecase.GetAccountStatementUseCase;
import com.residencial.finance.domain.exception.TenantStatementAccessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AccountStatementController.class, GlobalExceptionHandler.class})
class AccountStatementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetAccountStatementUseCase getAccountStatementUseCase;

    @Test
    @DisplayName("US-204 Web: Successfully retrieve account statement for Owner")
    void shouldReturnAccountStatementForOwner() throws Exception {
        UUID houseId = UUID.randomUUID();
        AccountStatementResponse response = new AccountStatementResponse(
                houseId,
                "A-101",
                new BigDecimal("350.00"),
                new BigDecimal("350.00"),
                BigDecimal.ZERO,
                new BigDecimal("350.00"),
                SolvencyStatus.SOLVENT,
                Collections.emptyList()
        );

        when(getAccountStatementUseCase.execute(houseId, "OWNER")).thenReturn(response);

        mockMvc.perform(get("/api/v1/finance/account-statements/" + houseId)
                        .header("X-User-Role", "OWNER")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.houseId").value(houseId.toString()))
                .andExpect(jsonPath("$.houseNumber").value("A-101"))
                .andExpect(jsonPath("$.totalBalance").value(350.00))
                .andExpect(jsonPath("$.solvencyStatus").value("SOLVENT"));
    }

    @Test
    @DisplayName("US-204 Web: Return 403 Forbidden when user is Tenant")
    void shouldReturnForbiddenForTenant() throws Exception {
        UUID houseId = UUID.randomUUID();

        when(getAccountStatementUseCase.execute(houseId, "TENANT"))
                .thenThrow(new TenantStatementAccessException());

        mockMvc.perform(get("/api/v1/finance/account-statements/" + houseId)
                        .header("X-User-Role", "TENANT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Access Forbidden"))
                .andExpect(jsonPath("$.errorCode").value("TENANT_STATEMENT_ACCESS_DENIED"));
    }
}
