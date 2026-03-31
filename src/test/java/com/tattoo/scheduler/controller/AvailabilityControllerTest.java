package com.tattoo.scheduler.controller;

import com.tattoo.scheduler.service.AvailabilityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
@ExtendWith(MockitoExtension.class)
public class AvailabilityControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvailabilityService availabilityService;

    @Test
    void getAvailableSlots_shouldReturn200WithSlots() throws Exception {
        List<LocalDateTime> slots = List.of(
                LocalDateTime.of(2026, 4, 15, 10, 0),
                LocalDateTime.of(2026, 4, 15, 12, 0)
        );
        when(availabilityService.getAvailableStartTimes(any(), any(), any())).thenReturn(slots);

        mockMvc.perform(get("/api/availability")
                        .param("date", "2026-04-15")
                        .param("sessionType", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"2026-04-15T10:00:00\",\"2026-04-15T12:00:00\"]"));
    }

    @Test
    void getAvailableSlots_shouldReturn400_whenDateMissing() throws Exception {
        mockMvc.perform(get("/api/availability")
                        .param("sessionType", "MEDIUM"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAvailableSlots_shouldReturn400_whenSessionTypeMissing() throws Exception {
        mockMvc.perform(get("/api/availability")
                        .param("date", "2026-04-15"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAvailableSlots_shouldReturn400_whenInvalidDateFormat() throws Exception {
        mockMvc.perform(get("/api/availability")
                        .param("date", "15-04-2026")
                        .param("sessionType", "MEDIUM"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAvailableSlots_shouldReturn400_whenInvalidSessionType() throws Exception {
        mockMvc.perform(get("/api/availability")
                        .param("date", "2026-04-15")
                        .param("sessionType", "WRONG"))
                .andExpect(status().isBadRequest());
    }
}
