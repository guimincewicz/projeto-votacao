package com.cooperative.voting.controller;

import com.cooperative.voting.dto.AgendaResponse;
import com.cooperative.voting.dto.CreateAgendaRequest;
import com.cooperative.voting.dto.OpenSessionRequest;
import com.cooperative.voting.dto.RegisterVoteRequest;
import com.cooperative.voting.dto.SessionResponse;
import com.cooperative.voting.dto.VoteResponse;
import com.cooperative.voting.dto.VotingResultResponse;
import com.cooperative.voting.service.AgendaService;
import com.cooperative.voting.service.VoteService;
import com.cooperative.voting.service.VotingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agendas")
@RequiredArgsConstructor
public class AgendaController {

    private final AgendaService agendaService;
    private final VotingSessionService votingSessionService;
    private final VoteService voteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgendaResponse create(@Valid @RequestBody CreateAgendaRequest request) {
        return agendaService.create(request);
    }

    @PostMapping("/{agendaId}/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse openSession(
            @PathVariable String agendaId,
            @Valid @RequestBody(required = false) OpenSessionRequest request
    ) {
        OpenSessionRequest sessionRequest = request == null
                ? new OpenSessionRequest(null)
                : request;

        return votingSessionService.open(agendaId, sessionRequest);
    }

    @PostMapping("/{agendaId}/votes")
    @ResponseStatus(HttpStatus.CREATED)
    public VoteResponse registerVote(
            @PathVariable String agendaId,
            @Valid @RequestBody RegisterVoteRequest request
    ) {
        return voteService.register(agendaId, request);
    }

    @GetMapping("/{agendaId}/result")
    public VotingResultResponse getResult(@PathVariable String agendaId) {
        return voteService.getResult(agendaId);
    }
}
