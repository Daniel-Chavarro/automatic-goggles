package org.java_avanzado.taller.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.java_avanzado.taller.config.JwtAuthenticationFilter;
import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.java_avanzado.taller.service.EventLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuditController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventLogService eventLogService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void given_logsAvailable_when_getAuditLogs_then_returns200() throws Exception {
        EventLogEntity log = org.mockito.Mockito.mock(EventLogEntity.class);

        when(eventLogService.getAllLogs()).thenReturn(List.of(log));

        mockMvc.perform(get("/api/audit"))
                .andExpect(status().isOk());
    }
}
