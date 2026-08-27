package com.cooperative.voting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.cooperative.voting.dto.OpenSessionRequest;
import com.cooperative.voting.model.VotingSession;
import com.cooperative.voting.repository.VotingSessionRepository;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

class SessionServiceTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-27T12:00:00Z"), ZoneOffset.UTC);
    @Test void usesOneMinuteWhenDurationIsAbsent() {
        AgendaService agendas = mock(AgendaService.class); VotingSessionRepository repository = mock(VotingSessionRepository.class);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        var response = new SessionService(agendas, repository, clock).open("agenda-1", new OpenSessionRequest(null));
        assertEquals(60, Duration.between(response.openedAt(), response.closesAt()).toSeconds());
    }
    @Test void usesProvidedDuration() {
        AgendaService agendas = mock(AgendaService.class); VotingSessionRepository repository = mock(VotingSessionRepository.class);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        var response = new SessionService(agendas, repository, clock).open("agenda-1", new OpenSessionRequest(5));
        assertEquals(300, Duration.between(response.openedAt(), response.closesAt()).toSeconds());
    }
}
