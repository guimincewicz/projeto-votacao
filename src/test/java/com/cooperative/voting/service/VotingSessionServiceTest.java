package com.cooperative.voting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cooperative.voting.dto.OpenSessionRequest;
import com.cooperative.voting.exception.ConflictException;
import com.cooperative.voting.model.VotingSession;
import com.cooperative.voting.repository.VotingSessionRepository;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class VotingSessionServiceTest {

    @Mock
    private AgendaService agendaService;

    @Mock
    private VotingSessionRepository votingSessionRepository;

    @Mock
    private MessageSource messageSource;

    @Test
    void shouldUseOneMinuteAsDefaultDuration() {
        when(votingSessionRepository.save(any(VotingSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        VotingSessionService service = new VotingSessionService(
                agendaService,
                votingSessionRepository,
                messageSource
        );

        var response = service.open("agenda-1", new OpenSessionRequest(null));

        assertEquals(60, Duration.between(response.openedAt(), response.closesAt()).toSeconds());
    }

    @Test
    void shouldUseCustomDuration() {
        when(votingSessionRepository.save(any(VotingSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        VotingSessionService service = new VotingSessionService(
                agendaService,
                votingSessionRepository,
                messageSource
        );

        var response = service.open("agenda-1", new OpenSessionRequest(5));

        assertEquals(300, Duration.between(response.openedAt(), response.closesAt()).toSeconds());
    }

    @Test
    void shouldNotOpenTwoSessionsForTheSameAgenda() {
        when(votingSessionRepository.save(any(VotingSession.class)))
                .thenThrow(new DuplicateKeyException("duplicate key"));
        VotingSessionService service = new VotingSessionService(
                agendaService,
                votingSessionRepository,
                messageSource
        );

        assertThrows(
                ConflictException.class,
                () -> service.open("agenda-1", new OpenSessionRequest(1))
        );
    }
}
