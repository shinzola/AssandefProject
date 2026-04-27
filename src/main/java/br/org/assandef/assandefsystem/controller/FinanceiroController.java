package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.CategoriaFinanceira;
import br.org.assandef.assandefsystem.model.ContaBancaria;
import br.org.assandef.assandefsystem.model.MovimentacaoFinanceira;
import br.org.assandef.assandefsystem.service.CategoriaFinanceiraService;
import br.org.assandef.assandefsystem.service.ContaBancariaService;
import br.org.assandef.assandefsystem.service.MovimentacaoFinanceiraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/financeiro")
@RequiredArgsConstructor
public class FinanceiroController {

    private final ContaBancariaService contaBancariaService;
    private final CategoriaFinanceiraService categoriaFinanceiraService;
    private final MovimentacaoFinanceiraService movimentacaoFinanceiraService;

    // =========================================================
    // PÁGINA PRINCIPAL
    // =========================================================

    @GetMapping
    public String exibirFinanceiro(Model model) {
        popularModel(model);
        model.addAttribute("contaForm", new ContaBancaria());
        model.addAttribute("movimentacaoForm", new MovimentacaoFinanceira());
        model.addAttribute("categoriaForm", new CategoriaFinanceira());
        return "financeiro/contabancaria";
    }

    // =========================================================
    // CONTAS BANCÁRIAS
    // =========================================================

    @PostMapping("/contas/salvar")
    public String salvarConta(
            @ModelAttribute("contaForm") ContaBancaria conta,
            RedirectAttributes redirectAttributes) {

        boolean isEdicao = conta.getIdConta() != null;
        try {
            contaBancariaService.save(conta);
            redirectAttributes.addFlashAttribute("mensagem",
                    isEdicao ? "Conta atualizada com sucesso!" : "Conta cadastrada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar conta: " + e.getMessage());
        }
        return "redirect:/financeiro";
    }

    @GetMapping("/contas/editar/{id}")
    public String editarConta(@PathVariable Integer id, Model model) {
        ContaBancaria conta = contaBancariaService.findById(id);
        popularModel(model);
        model.addAttribute("contaForm", conta);
        model.addAttribute("movimentacaoForm", new MovimentacaoFinanceira());
        model.addAttribute("categoriaForm", new CategoriaFinanceira());
        return "financeiro/contabancaria";
    }

    @PostMapping("/contas/excluir/{id}")
    public String excluirConta(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            contaBancariaService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Conta excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir conta: " + e.getMessage());
        }
        return "redirect:/financeiro";
    }

    // =========================================================
    // MOVIMENTAÇÕES FINANCEIRAS
    // =========================================================

    @PostMapping("/movimentacoes/salvar")
    public String salvarMovimentacao(
            @ModelAttribute("movimentacaoForm") MovimentacaoFinanceira movimentacao,
            @RequestParam(value = "idConta", required = false) Integer idConta,
            @RequestParam(value = "idCategoriaFinanceira", required = false) Integer idCategoriaFinanceira,
            RedirectAttributes redirectAttributes) {

        try {
            if (idConta != null) {
                ContaBancaria conta = contaBancariaService.findById(idConta);
                movimentacao.setConta(conta);
            }
            if (idCategoriaFinanceira != null) {
                CategoriaFinanceira categoria = categoriaFinanceiraService.findById(idCategoriaFinanceira);
                movimentacao.setCategoriaFinanceira(categoria);
            }
            movimentacaoFinanceiraService.save(movimentacao);
            redirectAttributes.addFlashAttribute("mensagem", "Movimentação registrada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar movimentação: " + e.getMessage());
        }
        return "redirect:/financeiro";
    }

    @PostMapping("/movimentacoes/excluir/{id}")
    public String excluirMovimentacao(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            movimentacaoFinanceiraService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Movimentação excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir movimentação: " + e.getMessage());
        }
        return "redirect:/financeiro";
    }

    // =========================================================
    // CATEGORIAS FINANCEIRAS
    // =========================================================

    @PostMapping("/categorias/salvar")
    public String salvarCategoria(
            @ModelAttribute("categoriaForm") CategoriaFinanceira categoria,
            RedirectAttributes redirectAttributes) {

        try {
            categoriaFinanceiraService.save(categoria);
            redirectAttributes.addFlashAttribute("mensagem", "Categoria cadastrada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar categoria: " + e.getMessage());
        }
        return "redirect:/financeiro";
    }

    @PostMapping("/categorias/excluir/{id}")
    public String excluirCategoria(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            categoriaFinanceiraService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Categoria excluída com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir categoria: " + e.getMessage());
        }
        return "redirect:/financeiro";
    }

    // =========================================================
    // MÉTODO AUXILIAR
    // =========================================================

    private void popularModel(Model model) {
        List<ContaBancaria> contas = contaBancariaService.findAll();
        List<MovimentacaoFinanceira> movimentacoes = movimentacaoFinanceiraService.findAll();
        List<CategoriaFinanceira> categorias = categoriaFinanceiraService.findAll();

        // Totais calculados a partir das movimentações
        BigDecimal totalEntradas = movimentacoes.stream()
                .filter(m -> m.getTipoMovimentacao() == MovimentacaoFinanceira.TipoMovimentacao.ENTRADA)
                .map(m -> m.getValor() != null ? m.getValor() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaidas = movimentacoes.stream()
                .filter(m -> m.getTipoMovimentacao() == MovimentacaoFinanceira.TipoMovimentacao.SAIDA)
                .map(m -> m.getValor() != null ? m.getValor() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoGeral = totalEntradas.subtract(totalSaidas);

        model.addAttribute("contas", contas);
        model.addAttribute("movimentacoes", movimentacoes);
        model.addAttribute("categorias", categorias);
        model.addAttribute("totalEntradas", totalEntradas);
        model.addAttribute("totalSaidas", totalSaidas);
        model.addAttribute("saldoGeral", saldoGeral);
    }
}