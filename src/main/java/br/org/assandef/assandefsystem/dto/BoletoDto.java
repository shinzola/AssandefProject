package br.org.assandef.assandefsystem.dto;

import lombok.Data;

@Data
public class BoletoDto {
    private Integer id;
    private Integer doadorId;
    private String status;
    private String pdfBoleto;
    // getters e setters
}