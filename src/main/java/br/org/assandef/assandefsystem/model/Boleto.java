package br.org.assandef.assandefsystem.model;

import jakarta.persistence.*;
import lombok.Data;

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

    @Column(columnDefinition = "LONGTEXT")
    private String pdfBoleto;
}

