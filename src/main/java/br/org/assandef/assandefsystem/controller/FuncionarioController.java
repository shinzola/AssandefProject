package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.service.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    @GetMapping
    public String pagina(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Integer hierarquia,
            @RequestParam(required = false) Boolean apenasAtivos,
            @ModelAttribute("msg") String msg,
            @ModelAttribute("erro") String erro,
            Model model) {

        List<Funcionario> todosFuncionarios;

        // Aplicar filtros
        if (apenasAtivos != null && apenasAtivos) {
            todosFuncionarios = funcionarioService.findAllAtivos();
        } else {
            todosFuncionarios = funcionarioService.findAll();
        }

        // Busca por nome
        if (busca != null && !busca.trim().isEmpty()) {
            String buscaLower = busca.toLowerCase();
            todosFuncionarios = todosFuncionarios.stream()
                    .filter(f -> f.getNomeCompleto().toLowerCase().contains(buscaLower)
                              || f.getLogin().toLowerCase().contains(buscaLower))
                    .collect(Collectors.toList());
        }

        // Filtro por hierarquia
        if (hierarquia != null) {
            todosFuncionarios = todosFuncionarios.stream()
                    .filter(f -> f.getHierarquia().equals(hierarquia))
                    .collect(Collectors.toList());
        }

        // Paginação manual
        int start = page * size;
        int end = Math.min(start + size, todosFuncionarios.size());
        List<Funcionario> funcionariosPaginados = todosFuncionarios.subList(start, end);

        Pageable pageable = PageRequest.of(page, size);
        Page<Funcionario> funcionariosPage = new PageImpl<>(funcionariosPaginados, pageable, todosFuncionarios.size());

        model.addAttribute("funcionarios", funcionariosPage);
        model.addAttribute("funcionariosAtivos", funcionarioService.findAllAtivos());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", funcionariosPage.getTotalPages());
        model.addAttribute("busca", busca);
        model.addAttribute("hierarquia", hierarquia);
        model.addAttribute("apenasAtivos", apenasAtivos);

        if (msg != null && !msg.isBlank()) model.addAttribute("msg", msg);
        if (erro != null && !erro.isBlank()) model.addAttribute("erro", erro);

        return "funcionarios/gestaofuncionarios";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Funcionario funcionario,
                        BindingResult result,
                        RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                ra.addFlashAttribute("erro", "Erro de validação: " + result.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/funcionarios";
            }

            funcionarioService.save(funcionario);
            ra.addFlashAttribute("msg", funcionario.getIdFuncionario() == null
                    ? "Funcionário cadastrado com sucesso!"
                    : "Funcionário atualizado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar funcionário: " + e.getMessage());
        }
        return "redirect:/funcionarios";
    }

    @PostMapping("/desativar")
    public String desativar(@RequestParam Integer id,
                           @RequestParam(required = false) Integer idSubstituto,
                           RedirectAttributes ra) {
        try {
            funcionarioService.desativar(id, idSubstituto);
            ra.addFlashAttribute("msg", "Funcionário desativado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao desativar funcionário: " + e.getMessage());
        }
        return "redirect:/funcionarios";
    }

    @PostMapping("/redefinir-senha")
    public String redefinirSenha(@RequestParam Integer id,
                                @RequestParam String novaSenha,
                                RedirectAttributes ra) {
        try {
            if (novaSenha == null || novaSenha.length() < 6) {
                ra.addFlashAttribute("erro", "A senha deve ter no mínimo 6 caracteres!");
                return "redirect:/funcionarios";
            }

            funcionarioService.redefinirSenha(id, novaSenha);
            ra.addFlashAttribute("msg", "Senha redefinida com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao redefinir senha: " + e.getMessage());
        }
        return "redirect:/funcionarios";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> obterFuncionario(@PathVariable Integer id) {
        try {
            Funcionario funcionario = funcionarioService.findById(id);

            // Não retornar a senha
            Map<String, Object> response = new HashMap<>();
            response.put("idFuncionario", funcionario.getIdFuncionario());
            response.put("nomeCompleto", funcionario.getNomeCompleto());
            response.put("login", funcionario.getLogin());
            response.put("hierarquia", funcionario.getHierarquia());
            response.put("ativo", funcionario.getAtivo());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

