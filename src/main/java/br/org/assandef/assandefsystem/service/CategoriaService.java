package br.org.assandef.assandefsystem.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.org.assandef.assandefsystem.model.Categoria;
import br.org.assandef.assandefsystem.repository.CategoriaRepository;

@Service
@Transactional
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Optional<Categoria> buscarPorId(Integer id) {
        return categoriaRepository.findById(id);
    }

    public Optional<Categoria> buscarPorNome(String nome) {
        return categoriaRepository.findByNome(nome);
    }

    public Categoria salvar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public Categoria atualizar(Integer id, Categoria categoriaAtualizada) {
        return categoriaRepository.findById(id)
            .map(categoria -> {
                categoria.setNome(categoriaAtualizada.getNome());
                return categoriaRepository.save(categoria);
            })
            .orElseThrow(() -> new RuntimeException("Categoria não encontrada com id: " + id));
    }

    public void deletar(Integer id) {
        categoriaRepository.deleteById(id);
    }

    public boolean existe(Integer id) {
        return categoriaRepository.existsById(id);
    }
}
