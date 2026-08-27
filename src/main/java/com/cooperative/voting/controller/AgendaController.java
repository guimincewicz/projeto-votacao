package com.cooperative.voting.controller;

import com.cooperative.voting.dto.*;
import com.cooperative.voting.service.AgendaService;
import com.cooperative.voting.service.SessionService;
import com.cooperative.voting.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agendas")
public class AgendaController {
    private final AgendaService agendas; private final SessionService sessions; private final VoteService votes;
    public AgendaController(AgendaService agendas, SessionService sessions, VoteService votes) { this.agendas = agendas; this.sessions = sessions; this.votes = votes; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @Operation(summary = "Create an agenda")
    public AgendaResponse create(@Valid @RequestBody CreateAgendaRequest request) { return agendas.create(request); }
    @PostMapping("/{agendaId}/sessions") @ResponseStatus(HttpStatus.CREATED) @Operation(summary = "Open an agenda voting session")
    public SessionResponse openSession(@PathVariable String agendaId, @Valid @RequestBody(required = false) OpenSessionRequest request) { return sessions.open(agendaId, request == null ? new OpenSessionRequest(null) : request); }
    @PostMapping("/{agendaId}/votes") @ResponseStatus(HttpStatus.CREATED) @Operation(summary = "Register an associate vote")
    public VoteResponse vote(@PathVariable String agendaId, @Valid @RequestBody RegisterVoteRequest request) { return votes.register(agendaId, request); }
    @GetMapping("/{agendaId}/result") @Operation(summary = "Get voting result")
    public VotingResultResponse result(@PathVariable String agendaId) { return votes.result(agendaId); }
}
