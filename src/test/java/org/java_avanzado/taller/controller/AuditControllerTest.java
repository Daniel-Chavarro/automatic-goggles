package org.java_avanzado.taller.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.java_avanzado.taller.security.JwtAuthenticationFilter;
import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.java_avanzado.taller.service.EventLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
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
    void given_logsAvailable_when_getAuditLogs_then_returnsLogsContent() throws Exception {
        EventLogEntity log = new EventLogEntity();
        ReflectionTestUtils.setField(log, "id", 1L);
        ReflectionTestUtils.setField(log, "eventType", "LOGIN_SUCCESS");
        ReflectionTestUtils.setField(log, "details", "User logged in successfully");
        ReflectionTestUtils.setField(log, "timestamp", LocalDateTime.of(2026, 4, 17, 10, 15, 30));
        ReflectionTestUtils.setField(log, "username", "ana@example.com");

        when(eventLogService.getAllLogs()).thenReturn(List.of(log));

        mockMvc.perform(get("/api/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].eventType").value("LOGIN_SUCCESS"))
                .andExpect(jsonPath("$[0].details").value("User logged in successfully"))
                .andExpect(jsonPath("$[0].username").value("ana@example.com"));
    }
}
