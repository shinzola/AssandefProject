package br.org.assandef.assandefsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // <-- IMPORTAR ISTO
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "doadores")
@Data
public class Doador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDoador;

    @Column(length = 255)
    private String nome;

    @Column(length = 14)
    private String cpfCnpj;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 15)
    private String sexo;

    @Column(columnDefinition = "TEXT")
    private String endereco;

    private LocalDate dataNascimento;

    @Column(name = "data_cadastro")
    private LocalDate dataCadastro;

    @Column(precision = 10, scale = 2)
    private BigDecimal mensalidade;

    @Column(name = "dia_vencimento")
    private Integer diaVencimento;

    @JsonIgnore
    @OneToMany(mappedBy = "doador")
    private List<Boleto> boletos;
}