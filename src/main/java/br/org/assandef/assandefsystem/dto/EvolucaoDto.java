package br.org.assandef.assandefsystem.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class EvolucaoDto {
    private Integer id;
    private Integer atendimentoId;
    private String descricao;
    private LocalDateTime dataHoraRegistro;
}