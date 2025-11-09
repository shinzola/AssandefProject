package br.org.assandef.assandefsystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "telefones")
@Data
public class Telefone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_telefone")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    @JsonBackReference
    private Paciente paciente;

    @NotBlank(message = "Número é obrigatório")
    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    @Column(name = "descricao", length = 50)
    private String descricao;

    // Construtores
    public Telefone() {
    }

    public Telefone(String numero, String descricao) {
        this.numero = numero;
        this.descricao = descricao;
    }
}
