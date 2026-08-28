package com.cooperative.voting.service;

import com.cooperative.voting.dto.AgendaResponse;
import com.cooperative.voting.dto.CreateAgendaRequest;
import com.cooperative.voting.exception.NotFoundException;
import com.cooperative.voting.model.Agenda;
import com.cooperative.voting.repository.AgendaRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.context.MessageSource;

@Service
@RequiredArgsConstructor
public class AgendaService {

    private static final Logger log = LoggerFactory.getLogger(AgendaService.class);

    private final AgendaRepository agendaRepository;
    private final Clock clock;
    private final MessageSource messageSource;

    public AgendaResponse create(CreateAgendaRequest request) {
        Agenda agenda = new Agenda(
                request.title().trim(),
                request.description().trim(),
                Instant.now(clock)
        );

        Agenda savedAgenda = agendaRepository.save(agenda);
        log.info("Agenda created: {}", savedAgenda.getId());

        return toResponse(savedAgenda);
    }

    public Agenda getRequired(String agendaId) {
        return agendaRepository.findById(agendaId)
                .orElseThrow(() -> new NotFoundException(message("agenda.not-found")));
    }

    private AgendaResponse toResponse(Agenda agenda) {
        return new AgendaResponse(
                agenda.getId(),
                agenda.getTitle(),
                agenda.getDescription(),
                agenda.getCreatedAt()
        );
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, Locale.getDefault());
    }
}
