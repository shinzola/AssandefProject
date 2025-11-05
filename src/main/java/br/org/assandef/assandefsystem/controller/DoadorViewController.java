/*Testee */
package br.org.assandef.assandefsystem.controller;


import br.org.assandef.assandefsystem.model.Boleto;
import br.org.assandef.assandefsystem.model.Doador;
import br.org.assandef.assandefsystem.model.StatusBoleto;
import br.org.assandef.assandefsystem.service.BoletoService;
import br.org.assandef.assandefsystem.service.DoadorService;
import br.org.assandef.assandefsystem.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/doadores")
@RequiredArgsConstructor
public class DoadorViewController {

    private final DoadorService doadorService;
    private final BoletoService boletoService;
    private final FileStorageService fileStorageService;

    // Página única
    @GetMapping
    public String pagina(Model model,
                         @ModelAttribute("msg") String msg,
                         @ModelAttribute("erro") String erro) {

        List<Doador> doadores = doadorService.findAll();
        List<Boleto> boletos = boletoService.findAll();

        model.addAttribute("doadores", doadores);
        model.addAttribute("boletos", boletos);

        // Para usar th:object no modal
        model.addAttribute("doador", new Doador());
        model.addAttribute("boleto", new Boleto());

        if (msg != null && !msg.isBlank()) model.addAttribute("msg", msg);
        if (erro != null && !erro.isBlank()) model.addAttribute("erro", erro);

        return "doadores/donation";
    }
    // dentro de DoadorViewController (usa os mesmos serviços já injetados)
    @GetMapping("/newdonation")
    public String novaPaginaDoador(Model model,
                                   @ModelAttribute("msg") String msg,
                                   @ModelAttribute("erro") String erro) {

        List<Doador> doadores = doadorService.findAll();
        List<Boleto> boletos = boletoService.findAll();

        model.addAttribute("doadores", doadores);
        model.addAttribute("boletos", boletos);
        model.addAttribute("doador", new Doador());
        model.addAttribute("boleto", new Boleto());

        if (msg != null && !msg.isBlank()) model.addAttribute("msg", msg);
        if (erro != null && !erro.isBlank()) model.addAttribute("erro", erro);

        return "doadores/newdonation";
    }

    // ======================
    // DOADOR
    // ======================
    @PostMapping("/salvar")
    public String salvarDoador(@ModelAttribute Doador doador, RedirectAttributes ra) {
        try {
            // Verifica se já existe um doador com os mesmos dados
            boolean exists = doadorService.existsByCpfCnpjOrEmailOrTelefone(
                    doador.getCpfCnpj(), doador.getEmail(), doador.getTelefone());

            if (exists) {
                ra.addFlashAttribute("erro", "Já existe um cadastro com este CPF/CNPJ, email ou telefone.");
                return "redirect:/doadores/newdonation";
            }

            // Se não existe, salva o novo doador
            boolean isNew = (doador.getIdDoador() == null);
            doadorService.save(doador);
            ra.addFlashAttribute("msg", isNew
                    ? "Doador cadastrado com sucesso!"
                    : "Doador atualizado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar doador: " + e.getMessage());
        }
        return "redirect:/doadores/newdonation";
    }

    @GetMapping("/deletar/{id}")
    public String deletarDoador(@PathVariable("id") Integer idDoador, RedirectAttributes ra) {
        try {
            doadorService.deleteById(idDoador);
            ra.addFlashAttribute("msg", "Doador excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir doador: " + e.getMessage());
        }
        return "redirect:/doadores";
    }

    // JSON para preencher modal de edição
    @ResponseBody
    @GetMapping("/{id}")
    public ResponseEntity<Doador> obterDoador(@PathVariable("id") Integer idDoador) {
        Doador doador = doadorService.findById(idDoador);
        if (doador == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(doador);
    }

    // Listar boletos de um doador específico
    @ResponseBody
    @GetMapping("/{id}/boletos")
    public ResponseEntity<List<Boleto>> listarBoletosPorDoador(@PathVariable("id") Integer idDoador) {
        try {
            List<Boleto> boletos = boletoService.findByDoador(idDoador);
            return ResponseEntity.ok(boletos);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ======================
    // BOLETO (COM UPLOAD)
    // ======================
    @PostMapping("/boleto/salvar")
    public String salvarBoleto(
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @ModelAttribute Boleto boleto,
            RedirectAttributes ra) {
        try {
            // DEBUG: Log do arquivo recebido
            System.out.println("🔍 DEBUG - Arquivo recebido:");
            System.out.println("   - arquivo == null? " + (arquivo == null));
            if (arquivo != null) {
                System.out.println("   - arquivo.isEmpty()? " + arquivo.isEmpty());
                System.out.println("   - Nome original: " + arquivo.getOriginalFilename());
                System.out.println("   - Tamanho: " + arquivo.getSize() + " bytes");
                System.out.println("   - Content-Type: " + arquivo.getContentType());
            }

            // Resolver a entidade Doador se veio apenas o ID
            if (boleto.getDoador() != null && boleto.getDoador().getIdDoador() != null) {
                Integer idDoador = boleto.getDoador().getIdDoador();
                Doador doador = doadorService.findById(idDoador);
                boleto.setDoador(doador);
            }

            // Define status padrão se não informado
            if (boleto.getStatus() == null) {
                boleto.setStatus(StatusBoleto.PENDENTE);
            }

            // Salvar primeiro para obter o ID do boleto
            Boleto boletoSalvo = boletoService.save(boleto);
            System.out.println("✅ Boleto salvo no banco com ID: " + boletoSalvo.getIdBoleto());

            // Se enviou arquivo, fazer upload
            if (arquivo != null && !arquivo.isEmpty()) {
                System.out.println("📤 Iniciando upload do arquivo...");
                String caminhoArquivo = fileStorageService.salvarBoleto(
                        arquivo,
                        boletoSalvo.getDoador().getIdDoador(),
                        boletoSalvo.getIdBoleto()
                );
                System.out.println("✅ Arquivo salvo em: " + caminhoArquivo);

                boletoSalvo.setPdfBoleto(caminhoArquivo);
                boletoService.save(boletoSalvo); // Atualizar com o caminho do arquivo
                System.out.println("✅ Caminho atualizado no banco");
            } else {
                System.out.println("⚠️ NENHUM ARQUIVO FOI ENVIADO!");
            }

            ra.addFlashAttribute("msg", boleto.getIdBoleto() == null
                    ? "Boleto cadastrado com sucesso!"
                    : "Boleto atualizado com sucesso!");
        } catch (Exception e) {
            System.err.println("❌ ERRO ao salvar boleto: " + e.getMessage());
            e.printStackTrace();
            ra.addFlashAttribute("erro", "Erro ao salvar boleto: " + e.getMessage());
        }
        return "redirect:/doadores";
    }

    @GetMapping("/boleto/deletar/{id}")
    public String deletarBoleto(@PathVariable("id") Integer idBoleto, RedirectAttributes ra) {
        try {
            Boleto boleto = boletoService.findById(idBoleto);

            // Deletar arquivo do disco se existir
            if (boleto.getPdfBoleto() != null && !boleto.getPdfBoleto().isBlank()) {
                try {
                    fileStorageService.deletarBoleto(boleto.getPdfBoleto());
                } catch (Exception e) {
                    System.err.println("Erro ao deletar arquivo: " + e.getMessage());
                }
            }

            // Deletar registro do banco
            boletoService.deleteById(idBoleto);
            ra.addFlashAttribute("msg", "Boleto excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir boleto: " + e.getMessage());
        }
        return "redirect:/doadores";
    }

    // JSON para preencher modal de edição
    @ResponseBody
    @GetMapping("/boleto/{id}")
    public ResponseEntity<Boleto> obterBoleto(@PathVariable("id") Integer idBoleto) {
        Boleto boleto = boletoService.findById(idBoleto);
        if (boleto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(boleto);
    }

    // Download/Visualização do PDF
    @GetMapping("/boleto/download/{id}")
    public ResponseEntity<Resource> downloadBoleto(@PathVariable("id") Integer idBoleto) {
        try {
            Boleto boleto = boletoService.findById(idBoleto);

            if (boleto.getPdfBoleto() == null || boleto.getPdfBoleto().isBlank()) {
                return ResponseEntity.notFound().build();
            }

            Path arquivo = fileStorageService.obterArquivo(boleto.getPdfBoleto());
            Resource resource = new UrlResource(arquivo.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + arquivo.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Atualizar status do boleto
    @GetMapping("/boleto/marcar-pago/{id}")
    public String marcarBoletoComoPago(@PathVariable("id") Integer idBoleto, RedirectAttributes ra) {
        try {
            Boleto boleto = boletoService.findById(idBoleto);
            boleto.setStatus(StatusBoleto.PAGO);
            boletoService.save(boleto);
            ra.addFlashAttribute("msg", "Boleto marcado como pago!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao atualizar boleto: " + e.getMessage());
        }
        return "redirect:/doadores";
    }

    @GetMapping("/boleto/marcar-vencido/{id}")
    public String marcarBoletoComoVencido(@PathVariable("id") Integer idBoleto, RedirectAttributes ra) {
        try {
            Boleto boleto = boletoService.findById(idBoleto);
            boleto.setStatus(StatusBoleto.VENCIDO);
            boletoService.save(boleto);
            ra.addFlashAttribute("msg", "Boleto marcado como vencido!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao atualizar boleto: " + e.getMessage());
        }
        return "redirect:/doadores";
    }
}