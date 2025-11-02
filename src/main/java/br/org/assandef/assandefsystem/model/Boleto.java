package br.org.assandef.assandefsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "boletos")
@Data
public class Boleto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idBoleto;

    @ManyToOne
    @JoinColumn(name = "id_doador")
    private Doador doador;

    @Enumerated(EnumType.STRING)
    private StatusBoleto status;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(columnDefinition = "LONGTEXT")
    private String pdfBoleto;
}