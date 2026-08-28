package com.cooperative.voting.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

public final class MobileScreen {

    private MobileScreen() {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Formulario(
            String tipo,
            String titulo,
            List<Item> itens,
            Acao botao1,
            Acao botaoCancelar
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Item(
            String tipo,
            String id,
            String titulo,
            Object valor,
            String texto
    ) {
    }

    public record Acao(
            String texto,
            String url,
            Map<String, Object> body
    ) {
    }

}
