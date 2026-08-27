package com.cooperative.voting.service;

import com.cooperative.voting.client.VoterEligibilityService;
import com.cooperative.voting.dto.RegisterVoteRequest;
import com.cooperative.voting.dto.VoteResponse;
import com.cooperative.voting.dto.VotingResultResponse;
import com.cooperative.voting.exception.ConflictException;
import com.cooperative.voting.exception.SessionClosedException;
import com.cooperative.voting.model.Vote;
import com.cooperative.voting.model.VoteOption;
import com.cooperative.voting.model.VotingSession;
import com.cooperative.voting.repository.VoteRepository;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class VoteService {
    private static final Logger log = LoggerFactory.getLogger(VoteService.class);
    private final AgendaService agendaService; private final SessionService sessionService; private final VoteRepository repository; private final VoterEligibilityService eligibility; private final Clock clock;
    public VoteService(AgendaService agendaService, SessionService sessionService, VoteRepository repository, VoterEligibilityService eligibility, Clock clock) { this.agendaService = agendaService; this.sessionService = sessionService; this.repository = repository; this.eligibility = eligibility; this.clock = clock; }
    public VoteResponse register(String agendaId, RegisterVoteRequest request) {
        agendaService.getRequired(agendaId);
        VotingSession session = sessionService.getForAgenda(agendaId);
        if (!Instant.now(clock).isBefore(session.getClosesAt())) { log.warn("Vote attempted after session close for agenda {}", agendaId); throw new SessionClosedException("Voting session is closed"); }
        eligibility.ensureEligible(request.cpf());
        try {
            Vote vote = repository.save(new Vote(agendaId, request.associateId().trim(), request.vote(), Instant.now(clock)));
            log.info("Vote registered for agenda {}", agendaId);
            return new VoteResponse(vote.getId(), vote.getAgendaId(), vote.getAssociateId(), vote.getVote(), vote.getCreatedAt());
        } catch (DuplicateKeyException exception) { log.warn("Duplicate vote attempted for agenda {}", agendaId); throw new ConflictException("Associate has already voted on this agenda"); }
    }
    public VotingResultResponse result(String agendaId) {
        agendaService.getRequired(agendaId);
        long yes = repository.countByAgendaIdAndVote(agendaId, VoteOption.YES), no = repository.countByAgendaIdAndVote(agendaId, VoteOption.NO);
        String result = yes == no ? "TIED" : yes > no ? "APPROVED" : "REJECTED";
        return new VotingResultResponse(agendaId, yes, no, yes + no, result);
    }
}
