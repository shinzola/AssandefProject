package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Atendimento;
import br.org.assandef.assandefsystem.service.AtendimentoService;
import br.org.assandef.assandefsystem.service.PacienteService;
import br.org.assandef.assandefsystem.service.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/atendimento")
@RequiredArgsConstructor
public class AtendimentoController {

    private final AtendimentoService atendimentoService;
    private final PacienteService pacienteService;
    private final FuncionarioService funcionarioService;

    @GetMapping
    public String pagina(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @ModelAttribute("msg") String msg,
            @ModelAttribute("erro") String erro,
            Model model) {

        List<Atendimento> todosAtendimentos;

        // Filtrar por período
        if (dataInicio != null && dataFim != null) {
            LocalDateTime inicio = dataInicio.atStartOfDay();
            LocalDateTime fim = dataFim.atTime(LocalTime.MAX);
            todosAtendimentos = atendimentoService.findByPeriodo(inicio, fim);
        } else {
            todosAtendimentos = atendimentoService.findAll();
        }

        // Filtrar por status
        if (status != null && !status.isEmpty() && !"todos".equals(status)) {
            todosAtendimentos = todosAtendimentos.stream()
                    .filter(a -> status.equals(a.getStatus()))
                    .collect(Collectors.toList());
        }

        // Buscar por nome do paciente
        if (busca != null && !busca.trim().isEmpty()) {
            String buscaLower = busca.toLowerCase();
            todosAtendimentos = todosAtendimentos.stream()
                    .filter(a -> a.getPaciente().getNomeCompleto().toLowerCase().contains(buscaLower))
                    .collect(Collectors.toList());
        }

        // Paginação
        int start = page * size;
        int end = Math.min(start + size, todosAtendimentos.size());
        List<Atendimento> atendimentosPaginados = todosAtendimentos.subList(start, end);

        Pageable pageable = PageRequest.of(page, size);
        Page<Atendimento> atendimentosPage = new PageImpl<>(atendimentosPaginados, pageable, todosAtendimentos.size());

        // Dados para a página
        model.addAttribute("atendimentos", atendimentosPage);
        model.addAttribute("pacientes", pacienteService.findAll());
        model.addAttribute("profissionais", funcionarioService.findAllAtivos());
        model.addAttribute("filaEspera", atendimentoService.findFilaEspera());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", atendimentosPage.getTotalPages());
        model.addAttribute("busca", busca);
        model.addAttribute("status", status);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);

        // Estatísticas
        model.addAttribute("totalAguardando", atendimentoService.contarPorStatus("Aguardando"));
        model.addAttribute("totalEmAndamento", atendimentoService.contarPorStatus("Em Andamento"));
        model.addAttribute("totalFinalizados", atendimentoService.contarPorStatus("Finalizado"));

        if (msg != null && !msg.isBlank()) model.addAttribute("msg", msg);
        if (erro != null && !erro.isBlank()) model.addAttribute("erro", erro);

        return "atendimento/atendimento";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Atendimento atendimento,
                        BindingResult result,
                        RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                ra.addFlashAttribute("erro", "Erro de validação: " + result.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/atendimento";
            }

            atendimentoService.save(atendimento);
            ra.addFlashAttribute("msg", "Atendimento registrado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar atendimento: " + e.getMessage());
        }
        return "redirect:/atendimento";
    }

    @PostMapping("/iniciar/{id}")
    public String iniciar(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            atendimentoService.iniciarAtendimento(id);
            ra.addFlashAttribute("msg", "Atendimento iniciado!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao iniciar atendimento: " + e.getMessage());
        }
        return "redirect:/atendimento";
    }

    @PostMapping("/finalizar")
    public String finalizar(@RequestParam Integer id,
                           @RequestParam String evolucao,
                           RedirectAttributes ra) {
        try {
            atendimentoService.finalizarAtendimento(id, evolucao);
            ra.addFlashAttribute("msg", "Atendimento finalizado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao finalizar atendimento: " + e.getMessage());
        }
        return "redirect:/atendimento";
    }

    @PostMapping("/cancelar")
    public String cancelar(@RequestParam Integer id,
                          @RequestParam String motivo,
                          RedirectAttributes ra) {
        try {
            atendimentoService.cancelarAtendimento(id, motivo);
            ra.addFlashAttribute("msg", "Atendimento cancelado!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao cancelar atendimento: " + e.getMessage());
        }
        return "redirect:/atendimento";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> obterAtendimento(@PathVariable Integer id) {
        try {
            Atendimento atendimento = atendimentoService.findById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("idAtendimento", atendimento.getIdAtendimento());
            response.put("paciente", atendimento.getPaciente());
            response.put("funcionario", atendimento.getFuncionario());
            response.put("dataHoraInicio", atendimento.getDataHoraInicio());
            response.put("dataHoraFim", atendimento.getDataHoraFim());
            response.put("status", atendimento.getStatus());
            response.put("tipoEncaminhamento", atendimento.getTipoEncaminhamento());
            response.put("observacoes", atendimento.getObservacoes());
            response.put("tempoEsperaMinutos", atendimentoService.calcularTempoEsperaMinutos(atendimento));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/paciente/{idPaciente}")
    @ResponseBody
    public ResponseEntity<List<Atendimento>> atendimentosPorPaciente(@PathVariable Integer idPaciente) {
        try {
            List<Atendimento> atendimentos = atendimentoService.findByPaciente(idPaciente);
            return ResponseEntity.ok(atendimentos);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public String deletar(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            atendimentoService.deleteById(id);
            ra.addFlashAttribute("msg", "Atendimento excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir atendimento: " + e.getMessage());
        }
        return "redirect:/atendimento";
    }
}

