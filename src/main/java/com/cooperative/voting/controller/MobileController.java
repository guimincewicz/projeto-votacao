package com.cooperative.voting.controller;

import com.cooperative.voting.dto.MobileScreen;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mobile/agendas")
@Tag(name = "Mobile", description = "Telas JSON interpretadas pelo aplicativo mobile")
public class MobileController {

    private static final String FORMULARIO = "FORMULARIO";

    private final String callbackBaseUrl;

    public MobileController(@Value("${mobile.callback-base-url}") String callbackBaseUrl) {
        this.callbackBaseUrl = callbackBaseUrl.replaceAll("/$", "");
    }

    @GetMapping("/formulario")
    @Operation(summary = "Retorna a tela de cadastro de pauta")
    public MobileScreen.Formulario createAgendaForm() {
        return new MobileScreen.Formulario(
                FORMULARIO,
                "Cadastrar pauta",
                List.of(
                        inputText("title", "Título"),
                        inputText("description", "Descrição")
                ),
                new MobileScreen.Acao("Cadastrar", callback("/api/v1/agendas"), Map.of()),
                null
        );
    }

    @GetMapping("/{agendaId}/sessao")
    @Operation(summary = "Retorna a tela de abertura de sessão")
    public MobileScreen.Formulario openSessionForm(@PathVariable String agendaId) {
        return new MobileScreen.Formulario(
                FORMULARIO,
                "Abrir sessão de votação",
                List.of(new MobileScreen.Item("INPUT_NUMERO", "durationMinutes", "Duração em minutos", 1, null)),
                new MobileScreen.Acao(
                        "Abrir sessão",
                        callback("/api/v1/agendas/" + agendaId + "/sessions"),
                        Map.of()
                ),
                null
        );
    }

    @GetMapping("/{agendaId}/voto")
    @Operation(summary = "Retorna a tela de registro de voto")
    public MobileScreen.Formulario voteForm(@PathVariable String agendaId) {
        String voteUrl = callback("/api/v1/agendas/" + agendaId + "/votes");

        return new MobileScreen.Formulario(
                FORMULARIO,
                "Registrar voto",
                List.of(
                        inputText("associateId", "Identificador do associado"),
                        inputText("cpf", "CPF")
                ),
                new MobileScreen.Acao("Votar SIM", voteUrl, Map.of("vote", "YES")),
                new MobileScreen.Acao("Votar NÃO", voteUrl, Map.of("vote", "NO"))
        );
    }

    private MobileScreen.Item inputText(String id, String title) {
        return new MobileScreen.Item("INPUT_TEXTO", id, title, null, null);
    }

    private String callback(String path) {
        return callbackBaseUrl + path;
    }
}
