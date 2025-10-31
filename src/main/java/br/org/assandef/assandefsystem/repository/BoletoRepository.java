package br.org.assandef.assandefsystem.repository;
import br.org.assandef.assandefsystem.model.Boleto;
import br.org.assandef.assandefsystem.model.StatusBoleto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BoletoRepository extends JpaRepository<Boleto, Integer> {
    List<Boleto> findByDoadorIdDoador(Integer idDoador);
    List<Boleto> findByStatus(StatusBoleto status);
}
