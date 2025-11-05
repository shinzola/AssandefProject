package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.dto.AtendimentoDto;
import br.org.assandef.assandefsystem.dto.EvolucaoDto;
import br.org.assandef.assandefsystem.dto.PrescricaoDto;
import br.org.assandef.assandefsystem.model.*;
import br.org.assandef.assandefsystem.repository.FuncionarioRepository;
import br.org.assandef.assandefsystem.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/atendimentos")
@RequiredArgsConstructor
public class AtendimentoController {

    private static final String REDIRECT_PROFISSIONAL = "redirect:/profissional";

    private final AtendimentoService atendimentoService;
    private final PacienteService pacienteService;
    private final EvolucaoService evolucaoService;
    private final PrescricaoService prescricaoService;
    private final FuncionarioRepository funcionarioRepository;

    @GetMapping
    public String listarAtendimentos(Model model,
                                     @ModelAttribute("msg") String msg,
                                     @ModelAttribute("erro") String erro) {
        List<Atendimento> atendimentos = atendimentoService.findAll();
        model.addAttribute("atendimentos", atendimentos);

        if (msg != null && !msg.isBlank()) model.addAttribute("msg", msg);
        if (erro != null && !erro.isBlank()) model.addAttribute("erro", erro);

        return "profissional/profissional";
    }

    @PostMapping("/iniciar")
    public String iniciarAtendimento(@ModelAttribute AtendimentoDto dto,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes ra) {
        try {
            String loginUsuario = userDetails.getUsername();
            Funcionario funcionarioLogado = funcionarioRepository.findByLogin(loginUsuario)
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado: " + loginUsuario));

            Atendimento atendimento = new Atendimento();

            // Buscar paciente
            // Corrigido: O DTO de williamucha tinha 'idPaciente' e 'pacienteId', usei 'idPaciente'
            Paciente paciente = pacienteService.findById(dto.getIdPaciente() != null ? dto.getIdPaciente() : dto.getPacienteId());
            atendimento.setPaciente(paciente);
            atendimento.setFuncionario(funcionarioLogado);
            atendimento.setDataHoraInicio(LocalDateTime.now());
            atendimento.setStatus("EM_ANDAMENTO");
            atendimento.setTipoEncaminhamento(dto.getTipoEncaminhamento());

            atendimentoService.save(atendimento);
            ra.addFlashAttribute("msg", "Atendimento iniciado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao iniciar atendimento: " + e.getMessage());
        }
        return REDIRECT_PROFISSIONAL;
    }

    @PostMapping("/finalizar/{id}")
    public String finalizarAtendimento(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            Atendimento atendimento = atendimentoService.findById(id);
            atendimento.setDataHoraFim(LocalDateTime.now());
            atendimento.setStatus("FINALIZADO");
            atendimentoService.save(atendimento);
            ra.addFlashAttribute("msg", "Atendimento finalizado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao finalizar atendimento: " + e.getMessage());
        }
        return REDIRECT_PROFISSIONAL;
    }

    @ResponseBody
    @GetMapping("/fila")
    public ResponseEntity<List<Atendimento>> listarFilaEspera() {
        try {
            List<Atendimento> fila = atendimentoService.findAll().stream()
                    .filter(a -> "EM_ESPERA".equals(a.getStatus()) || "EM_ANDAMENTO".equals(a.getStatus()))
                    .toList();
            return ResponseEntity.ok(fila);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @ResponseBody
    @GetMapping("/meus")
    public ResponseEntity<List<Atendimento>> meuAtendimentos(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String loginUsuario = userDetails.getUsername();
            Funcionario funcionarioLogado = funcionarioRepository.findByLogin(loginUsuario)
                    .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

            List<Atendimento> meusAtendimentos = atendimentoService.findAll().stream()
                    .filter(a -> a.getFuncionario().getIdFuncionario().equals(funcionarioLogado.getIdFuncionario()))
                    .toList();
            return ResponseEntity.ok(meusAtendimentos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @ResponseBody
    @GetMapping("/{id}")
    public ResponseEntity<Atendimento> obter(@PathVariable("id") Integer id) {
        try {
            Atendimento atendimento = atendimentoService.findById(id);
            return ResponseEntity.ok(atendimento);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ResponseBody
    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<Atendimento>> listarPorPaciente(@PathVariable("idPaciente") Integer idPaciente) {
        try {
            List<Atendimento> atendimentos = atendimentoService.findAll().stream()
                    .filter(a -> a.getPaciente().getIdPaciente().equals(idPaciente))
                    .toList();
            return ResponseEntity.ok(atendimentos);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // =====================
    // EVOLUÇÃO
    // =====================
    @PostMapping("/{id}/evolucao")
    public String adicionarEvolucao(@PathVariable("id") Integer idAtendimento,
                                    @ModelAttribute EvolucaoDto dto,
                                    RedirectAttributes ra) {
        try {
            Atendimento atendimento = atendimentoService.findById(idAtendimento);

            Evolucao evolucao = new Evolucao();
            evolucao.setAtendimento(atendimento);
            evolucao.setDescricao(dto.getDescricao());
            evolucao.setDataHoraRegistro(LocalDateTime.now());

            evolucaoService.save(evolucao);
            ra.addFlashAttribute("msg", "Evolução registrada com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao registrar evolução: " + e.getMessage());
        }
        return REDIRECT_PROFISSIONAL;
    }

    @ResponseBody
    @GetMapping("/{id}/evolucoes")
    public ResponseEntity<List<Evolucao>> listarEvolucoes(@PathVariable("id") Integer idAtendimento) {
        try {
            List<Evolucao> evolucoes = evolucaoService.findAll().stream()
                    .filter(e -> e.getAtendimento().getIdAtendimento().equals(idAtendimento))
                    .toList();
            return ResponseEntity.ok(evolucoes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // =====================
    // PRESCRIÇÃO
    // =====================
    @PostMapping("/evolucao/{idEvolucao}/prescricao")
    public String adicionarPrescricao(@PathVariable("idEvolucao") Integer idEvolucao,
                                      @ModelAttribute PrescricaoDto dto,
                                      RedirectAttributes ra) {
        try {
            Evolucao evolucao = evolucaoService.findById(idEvolucao);

            Prescricao prescricao = new Prescricao();
            prescricao.setEvolucao(evolucao);
            prescricao.setTipo(dto.getTipo());
            prescricao.setDescricao(dto.getDescricao());

            prescricaoService.save(prescricao);
            ra.addFlashAttribute("msg", "Prescrição adicionada com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao adicionar prescrição: " + e.getMessage());
        }
        return REDIRECT_PROFISSIONAL;
    }

    @ResponseBody
    @GetMapping("/evolucao/{idEvolucao}/prescricoes")
    public ResponseEntity<List<Prescricao>> listarPrescricoes(@PathVariable("idEvolucao") Integer idEvolucao) {
        try {
            List<Prescricao> prescricoes = prescricaoService.findAll().stream()
                    .filter(p -> p.getEvolucao().getIdEvolucao().equals(idEvolucao))
                    .toList();
            return ResponseEntity.ok(prescricoes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            atendimentoService.deleteById(id);
            ra.addFlashAttribute("msg", "Atendimento excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir atendimento: " + e.getMessage());
        }
        return REDIRECT_PROFISSIONAL;
    }
}