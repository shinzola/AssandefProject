package br.org.assandef.assandefsystem.dto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SolicitacoesMaterialDto {
    private Integer id;
    private Integer funcionarioId;
    private Integer materialId;
    private Integer quantidadeSolicitada;
    private String tipoSaida;
    private LocalDateTime dataSolicitacao;
    private String status; // use string se não quiser expor enum no form
    private String descricao;
    // getters e     setters
}