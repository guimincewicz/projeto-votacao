package com.cooperative.voting.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MobileController.class)
@TestPropertySource(properties = "mobile.callback-base-url=http://api.test")
class MobileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnVoteFormWithCallbackUrls() throws Exception {
        mockMvc.perform(get("/api/v1/mobile/agendas/agenda-1/voto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("FORMULARIO"))
                .andExpect(jsonPath("$.itens[0].id").value("associateId"))
                .andExpect(jsonPath("$.botao1.url")
                        .value("http://api.test/api/v1/agendas/agenda-1/votes"))
                .andExpect(jsonPath("$.botao1.body.vote").value("YES"))
                .andExpect(jsonPath("$.botaoCancelar.body.vote").value("NO"));
    }
}
