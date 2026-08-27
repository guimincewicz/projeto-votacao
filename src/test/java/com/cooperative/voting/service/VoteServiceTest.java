package com.cooperative.voting.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cooperative.voting.client.VoterEligibilityService;
import com.cooperative.voting.dto.RegisterVoteRequest;
import com.cooperative.voting.exception.ConflictException;
import com.cooperative.voting.exception.SessionClosedException;
import com.cooperative.voting.model.*;
import com.cooperative.voting.repository.VoteRepository;
import java.time.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class VoteServiceTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-27T12:00:00Z"), ZoneOffset.UTC);
    @Mock AgendaService agendas; @Mock SessionService sessions; @Mock VoteRepository votes; @Mock VoterEligibilityService eligibility;
    @InjectMocks VoteService service;
    @BeforeEach void setUp() { service = new VoteService(agendas, sessions, votes, eligibility, clock); }
    private RegisterVoteRequest request(VoteOption option) { return new RegisterVoteRequest("associate-1", "12345678909", option); }
    private void openSession() { when(sessions.getForAgenda("agenda-1")).thenReturn(new VotingSession("agenda-1", Instant.now(clock), Instant.now(clock).plusSeconds(60))); }
    @Test void registersYesVote() {
        openSession(); when(votes.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals(VoteOption.YES, service.register("agenda-1", request(VoteOption.YES)).vote());
        verify(eligibility).ensureEligible("12345678909");
    }
    @Test void rejectsDuplicateVoteFromDatabaseConstraint() {
        openSession(); when(votes.save(any())).thenThrow(new DuplicateKeyException("duplicate"));
        assertThrows(ConflictException.class, () -> service.register("agenda-1", request(VoteOption.NO)));
    }
    @Test void rejectsVoteAfterSessionCloses() {
        when(sessions.getForAgenda("agenda-1")).thenReturn(new VotingSession("agenda-1", Instant.now(clock).minusSeconds(120), Instant.now(clock)));
        assertThrows(SessionClosedException.class, () -> service.register("agenda-1", request(VoteOption.YES)));
        verifyNoInteractions(eligibility, votes);
    }
    @Test void calculatesTie() {
        when(votes.countByAgendaIdAndVote("agenda-1", VoteOption.YES)).thenReturn(10L);
        when(votes.countByAgendaIdAndVote("agenda-1", VoteOption.NO)).thenReturn(10L);
        assertEquals("TIED", service.result("agenda-1").result());
    }
}
