package com.cooperative.voting.service;

import com.cooperative.voting.dto.OpenSessionRequest;
import com.cooperative.voting.dto.SessionResponse;
import com.cooperative.voting.exception.ConflictException;
import com.cooperative.voting.model.VotingSession;
import com.cooperative.voting.repository.VotingSessionRepository;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private final AgendaService agendaService; private final VotingSessionRepository repository; private final Clock clock;
    public SessionService(AgendaService agendaService, VotingSessionRepository repository, Clock clock) { this.agendaService = agendaService; this.repository = repository; this.clock = clock; }
    public SessionResponse open(String agendaId, OpenSessionRequest request) {
        agendaService.getRequired(agendaId);
        int minutes = request.durationMinutes() == null ? 1 : request.durationMinutes();
        Instant openedAt = Instant.now(clock);
        try {
            VotingSession session = repository.save(new VotingSession(agendaId, openedAt, openedAt.plusSeconds(minutes * 60L)));
            log.info("Voting session opened for agenda {}", agendaId);
            return toResponse(session);
        } catch (DuplicateKeyException exception) { throw new ConflictException("A voting session already exists for this agenda"); }
    }
    public VotingSession getForAgenda(String agendaId) { return repository.findByAgendaId(agendaId).orElseThrow(() -> new com.cooperative.voting.exception.NotFoundException("Voting session not found")); }
    private SessionResponse toResponse(VotingSession s) { return new SessionResponse(s.getId(), s.getAgendaId(), s.getOpenedAt(), s.getClosesAt()); }
}
