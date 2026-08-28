package com.cooperative.voting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cooperative.voting.dto.CreateAgendaRequest;
import com.cooperative.voting.model.Agenda;
import com.cooperative.voting.repository.AgendaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    @Mock
    private AgendaRepository agendaRepository;

    @Mock
    private MessageSource messageSource;

    @Test
    void shouldCreateAgenda() {
        when(agendaRepository.save(any(Agenda.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AgendaService service = new AgendaService(agendaRepository, messageSource);

        var response = service.create(new CreateAgendaRequest(" Orçamento anual ", " Votação anual "));

        assertEquals("Orçamento anual", response.title());
        assertEquals("Votação anual", response.description());
        assertNotNull(response.createdAt());
    }
}
