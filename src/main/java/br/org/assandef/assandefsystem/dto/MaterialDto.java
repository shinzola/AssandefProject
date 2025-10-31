package br.org.assandef.assandefsystem.dto;

import lombok.Data;

import java.time.LocalDate;
@Data
public class MaterialDto {
    private Integer id;
    private String nome;
    private Integer categoriaId;
    private Integer quantidadeAtual;
    private String fornecedor;
    private LocalDate dataValidade;
    // getters e setters
}