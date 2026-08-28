package com.cooperative.voting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cooperative.voting.client.VoterEligibilityService;
import com.cooperative.voting.dto.RegisterVoteRequest;
import com.cooperative.voting.exception.BusinessException;
import com.cooperative.voting.exception.ConflictException;
import com.cooperative.voting.model.Vote;
import com.cooperative.voting.model.VoteOption;
import com.cooperative.voting.model.VotingSession;
import com.cooperative.voting.repository.VoteRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock
    private AgendaService agendaService;

    @Mock
    private VotingSessionService votingSessionService;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private VoterEligibilityService voterEligibilityService;

    @Test
    void shouldRegisterYesVote() {
        VotingSession session = openSession();
        when(votingSessionService.getForAgenda("agenda-1")).thenReturn(session);
        when(voteRepository.save(any(Vote.class))).thenAnswer(invocation -> invocation.getArgument(0));
        VoteService service = service();

        var response = service.register("agenda-1", request(VoteOption.YES));

        assertEquals(VoteOption.YES, response.vote());
        verify(voterEligibilityService).ensureEligible("12345678909");
    }

    @Test
    void shouldRejectDuplicateVoteFromDatabaseConstraint() {
        when(votingSessionService.getForAgenda("agenda-1")).thenReturn(openSession());
        when(voteRepository.save(any(Vote.class))).thenThrow(new DuplicateKeyException("duplicate key"));
        VoteService service = service();

        assertThrows(ConflictException.class, () -> service.register("agenda-1", request(VoteOption.NO)));
    }

    @Test
    void shouldRejectVoteAfterSessionCloses() {
        VotingSession closedSession = new VotingSession(
                "agenda-1",
                Instant.now().minusSeconds(120),
                Instant.now().minusSeconds(60)
        );
        when(votingSessionService.getForAgenda("agenda-1")).thenReturn(closedSession);
        VoteService service = service();

        assertThrows(BusinessException.class, () -> service.register("agenda-1", request(VoteOption.YES)));
        verifyNoInteractions(voterEligibilityService, voteRepository);
    }

    @Test
    void shouldReturnTiedResult() {
        when(voteRepository.countByAgendaIdAndVote("agenda-1", VoteOption.YES)).thenReturn(10L);
        when(voteRepository.countByAgendaIdAndVote("agenda-1", VoteOption.NO)).thenReturn(10L);
        VoteService service = service();

        var result = service.getResult("agenda-1");

        assertEquals(20, result.totalVotes());
        assertEquals("TIED", result.result());
    }

    private VoteService service() {
        return new VoteService(
                agendaService,
                votingSessionService,
                voteRepository,
                messageSource,
                voterEligibilityService
        );
    }

    private VotingSession openSession() {
        return new VotingSession(
                "agenda-1",
                Instant.now(),
                Instant.now().plusSeconds(60)
        );
    }

    private RegisterVoteRequest request(VoteOption vote) {
        return new RegisterVoteRequest("associate-1", "12345678909", vote);
    }
}
