package com.cooperative.voting.service;

import com.cooperative.voting.dto.*;
import com.cooperative.voting.exception.NotFoundException;
import com.cooperative.voting.model.Agenda;
import com.cooperative.voting.repository.AgendaRepository;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AgendaService {
    private static final Logger log = LoggerFactory.getLogger(AgendaService.class);
    private final AgendaRepository repository; private final Clock clock;
    public AgendaService(AgendaRepository repository, Clock clock) { this.repository = repository; this.clock = clock; }
    public AgendaResponse create(CreateAgendaRequest request) {
        Agenda agenda = repository.save(new Agenda(request.title().trim(), request.description().trim(), Instant.now(clock)));
        log.info("Agenda created: {}", agenda.getId());
        return toResponse(agenda);
    }
    public Agenda getRequired(String agendaId) { return repository.findById(agendaId).orElseThrow(() -> new NotFoundException("Agenda not found")); }
    public AgendaResponse toResponse(Agenda agenda) { return new AgendaResponse(agenda.getId(), agenda.getTitle(), agenda.getDescription(), agenda.getCreatedAt()); }
}
