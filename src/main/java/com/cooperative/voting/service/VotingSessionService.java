package com.cooperative.voting.service;

import com.cooperative.voting.dto.OpenSessionRequest;
import com.cooperative.voting.dto.SessionResponse;
import com.cooperative.voting.exception.ConflictException;
import com.cooperative.voting.exception.NotFoundException;
import com.cooperative.voting.model.VotingSession;
import com.cooperative.voting.repository.VotingSessionRepository;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VotingSessionService {

    private static final Logger log = LoggerFactory.getLogger(VotingSessionService.class);
    private static final int DEFAULT_DURATION_MINUTES = 1;

    private final AgendaService agendaService;
    private final VotingSessionRepository votingSessionRepository;
    private final Clock clock;

    public SessionResponse open(String agendaId, OpenSessionRequest request) {
        agendaService.getRequired(agendaId);

        int durationMinutes = request.durationMinutes() == null
                ? DEFAULT_DURATION_MINUTES
                : request.durationMinutes();
        Instant openedAt = Instant.now(clock);

        VotingSession session = new VotingSession(
                agendaId,
                openedAt,
                openedAt.plusSeconds(durationMinutes * 60L)
        );

        try {
            VotingSession savedSession = votingSessionRepository.save(session);
            log.info("Voting session opened for agenda {}", agendaId);
            return toResponse(savedSession);
        } catch (DuplicateKeyException exception) {
            throw new ConflictException("A voting session already exists for this agenda");
        }
    }

    public VotingSession getForAgenda(String agendaId) {
        return votingSessionRepository.findByAgendaId(agendaId)
                .orElseThrow(() -> new NotFoundException("Voting session not found"));
    }

    private SessionResponse toResponse(VotingSession session) {
        return new SessionResponse(
                session.getId(),
                session.getAgendaId(),
                session.getOpenedAt(),
                session.getClosesAt()
        );
    }
}
