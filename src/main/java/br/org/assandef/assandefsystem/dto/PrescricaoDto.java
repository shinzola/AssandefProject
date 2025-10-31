package br.org.assandef.assandefsystem.dto;

import lombok.Data;

@Data
public class PrescricaoDto {
    private Integer id;
    private Integer evolucaoId;
    private String tipo;
    private String descricao;
}
