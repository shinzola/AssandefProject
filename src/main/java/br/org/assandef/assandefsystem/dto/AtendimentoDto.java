package br.org.assandef.assandefsystem.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class AtendimentoDto {
    private Integer id;
    private Integer pacienteId;
    private Integer funcionarioId;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private String status;
    private String tipoEncaminhamento;}