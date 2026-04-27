package br.org.assandef.assandefsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "categoria_financeira")
@Data
public class CategoriaFinanceira {
    public enum TipoCategoria {
        RECEITA, DESPESA
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria_financeira")
    private Integer idCategoriaFinanceira;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoCategoria tipo; // Ex: RECEITA, DESPESA

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;
}