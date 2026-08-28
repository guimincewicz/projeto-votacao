package com.cooperative.voting.service;

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
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.context.MessageSource;

@Service
@RequiredArgsConstructor
public class VoteService {

    private static final Logger log = LoggerFactory.getLogger(VoteService.class);

    private final AgendaService agendaService;
    private final VotingSessionService votingSessionService;
    private final VoteRepository voteRepository;
    private final Clock clock;
    private final MessageSource messageSource;

    public VoteResponse register(String agendaId, RegisterVoteRequest request) {
        agendaService.getRequired(agendaId);

        VotingSession session = votingSessionService.getForAgenda(agendaId);
        if (!Instant.now(clock).isBefore(session.getClosesAt())) {
            log.warn("Vote attempted after session close for agenda {}", agendaId);
            throw new SessionClosedException(message("voting-session.closed"));
        }

        Vote vote = new Vote(
                agendaId,
                request.associateId().trim(),
                request.vote(),
                Instant.now(clock)
        );

        try {
            Vote savedVote = voteRepository.save(vote);
            log.info("Vote registered for agenda {}", agendaId);
            return toResponse(savedVote);
        } catch (DuplicateKeyException exception) {
            log.warn("Duplicate vote attempted for agenda {}", agendaId);
            throw new ConflictException(message("vote.duplicate"));
        }
    }

    public VotingResultResponse getResult(String agendaId) {
        agendaService.getRequired(agendaId);

        long yesVotes = voteRepository.countByAgendaIdAndVote(agendaId, VoteOption.YES);
        long noVotes = voteRepository.countByAgendaIdAndVote(agendaId, VoteOption.NO);

        String result = yesVotes == noVotes
                ? "TIED"
                : yesVotes > noVotes ? "APPROVED" : "REJECTED";

        return new VotingResultResponse(
                agendaId,
                yesVotes,
                noVotes,
                yesVotes + noVotes,
                result
        );
    }

    private VoteResponse toResponse(Vote vote) {
        return new VoteResponse(
                vote.getId(),
                vote.getAgendaId(),
                vote.getAssociateId(),
                vote.getVote(),
                vote.getCreatedAt()
        );
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, Locale.getDefault());
    }
}
