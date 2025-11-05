package br.org.assandef.assandefsystem.controller; // ajuste conforme seu projeto

import br.org.assandef.assandefsystem.model.Material;
import br.org.assandef.assandefsystem.model.SolicitacoesMaterial;
import br.org.assandef.assandefsystem.service.MaterialService;
import br.org.assandef.assandefsystem.service.SolicitacoesMaterialService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse; // se Spring Boot 2.x, troque para javax.servlet.http.HttpServletResponse
import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/almoxarifado/relatorio")
public class RelatorioController {

    private final MaterialService materialService;
    private final SolicitacoesMaterialService solicitacoesMaterialService;

    public RelatorioController(MaterialService materialService,
                               SolicitacoesMaterialService solicitacoesMaterialService) {
        this.materialService = materialService;
        this.solicitacoesMaterialService = solicitacoesMaterialService;
    }

    // GET dados (mantido para consumo via JS se quiser)
    @GetMapping("/dados")
    public Object getDadosRelatorio(
            @RequestParam String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        if ("estoque".equalsIgnoreCase(tipo)) {
            List<Material> materiais = materialService.findAll();
            return materiais; // seu JS/DTO pode serializar conforme já implementado
        }

        if ("solicitacoes".equalsIgnoreCase(tipo)) {
            List<SolicitacoesMaterial> solicitacoes = solicitacoesMaterialService.findByPeriodo(dataInicio, dataFim);
            return solicitacoes;
        }

        return List.of();
    }

    // POST gerar: gera CSV ou PDF e retorna como download
    @PostMapping("/gerar")
    public void gerarRelatorio(
            @RequestParam String tipo,
            @RequestParam String formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            HttpServletResponse response) throws IOException {

        if ("csv".equalsIgnoreCase(formato)) {
            String csv;
            String filename = "relatorio_" + tipo + "_" + LocalDate.now() + ".csv";

            if ("estoque".equalsIgnoreCase(tipo)) {
                List<Material> materiais = materialService.findAll();
                csv = buildCsvEstoque(materiais);
            } else if ("solicitacoes".equalsIgnoreCase(tipo)) {
                List<SolicitacoesMaterial> solicitacoes = solicitacoesMaterialService.findByPeriodo(dataInicio, dataFim);
                csv = buildCsvSolicitacoes(solicitacoes);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tipo inválido");
                return;
            }

            byte[] bytes = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8); // BOM para Excel
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
            return;
        }

        if ("pdf".equalsIgnoreCase(formato)) {
            String filename = "relatorio_" + tipo + "_" + LocalDate.now() + ".pdf";
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            try {
                Document document = new Document(PageSize.A4.rotate()); // landscape para caber colunas; troque se quiser
                PdfWriter.getInstance(document, response.getOutputStream());
                document.open();

                Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                Paragraph titulo = new Paragraph("Relatório de " + tipo.toUpperCase(), fontTitulo);
                titulo.setAlignment(Element.ALIGN_CENTER);
                document.add(titulo);
                document.add(Chunk.NEWLINE);

                DateTimeFormatter dtfData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter dtfDataHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                if ("estoque".equalsIgnoreCase(tipo)) {
                    List<Material> materiais = materialService.findAll();
                    PdfPTable table = new PdfPTable(5);
                    table.setWidthPercentage(100);
                    addTableHeader(table, new String[]{"Nome", "Categoria", "Quantidade", "Validade", "Fornecedor"});

                    for (Material m : materiais) {
                        table.addCell(orEmpty(m.getNome()));
                        table.addCell(orEmpty(m.getCategoria() != null ? m.getCategoria().getNome() : null));
                        table.addCell(String.valueOf(m.getQuantidadeAtual() != null ? m.getQuantidadeAtual() : 0));
                        table.addCell(m.getDataValidade() != null ? m.getDataValidade().format(dtfData) : "Sem validade");
                        table.addCell(orEmpty(m.getFornecedor()));
                    }
                    document.add(table);

                } else if ("solicitacoes".equalsIgnoreCase(tipo)) {
                    List<SolicitacoesMaterial> solicitacoes = solicitacoesMaterialService.findByPeriodo(dataInicio, dataFim);
                    PdfPTable table = new PdfPTable(8);
                    table.setWidthPercentage(100);
                    addTableHeader(table, new String[]{"ID", "Material", "Quantidade", "Solicitante", "Data", "Tipo Saída", "Status", "Justificativa"});

                    for (SolicitacoesMaterial s : solicitacoes) {
                        table.addCell(String.valueOf(s.getIdSolicitacao() != null ? s.getIdSolicitacao() : ""));
                        table.addCell(orEmpty(s.getMaterial() != null ? s.getMaterial().getNome() : null));
                        table.addCell(String.valueOf(s.getQuantidadeSolicitada() != null ? s.getQuantidadeSolicitada() : 0));
                        table.addCell(orEmpty(s.getFuncionarioSolicitante() != null ? s.getFuncionarioSolicitante().getNomeCompleto() : null));
                        table.addCell(s.getDataSolicitacao() != null ? s.getDataSolicitacao().format(dtfDataHora) : "");
                        table.addCell(orEmpty(s.getTipoSaida()));
                        table.addCell(s.getStatus() != null ? s.getStatus().name() : "");
                        table.addCell(orEmpty(s.getDescricao()));
                    }
                    document.add(table);
                } else {
                    document.add(new Paragraph("Tipo de relatório inválido."));
                }

                document.close();
            } catch (Exception e) {
                // Em caso de erro escrevemos 500 e a mensagem
                response.reset();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao gerar PDF: " + e.getMessage());
            }
            return;
        }

        // formato inválido
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato inválido (use csv ou pdf)");
    }

    // --- Helpers CSV ---
    private String buildCsvEstoque(List<Material> materiais) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nome do Material;Categoria;Quantidade;Validade;Fornecedor\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Material m : materiais) {
            String validade = m.getDataValidade() != null ? m.getDataValidade().format(dtf) : "";
            sb.append(escapeCsv(m.getNome())).append(';')
                    .append(escapeCsv(m.getCategoria() != null ? m.getCategoria().getNome() : "")).append(';')
                    .append(m.getQuantidadeAtual() != null ? m.getQuantidadeAtual() : 0).append(';')
                    .append(escapeCsv(validade)).append(';')
                    .append(escapeCsv(m.getFornecedor())).append('\n');
        }
        return sb.toString();
    }

    private String buildCsvSolicitacoes(List<SolicitacoesMaterial> solicitacoes) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Material;Quantidade;Solicitante;Data;Tipo de Saída;Status;Justificativa\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (SolicitacoesMaterial s : solicitacoes) {
            String data = s.getDataSolicitacao() != null ? s.getDataSolicitacao().format(dtf) : "";
            sb.append(orEmpty(s.getIdSolicitacao())).append(';')
                    .append(escapeCsv(s.getMaterial() != null ? s.getMaterial().getNome() : "")).append(';')
                    .append(s.getQuantidadeSolicitada() != null ? s.getQuantidadeSolicitada() : 0).append(';')
                    .append(escapeCsv(s.getFuncionarioSolicitante() != null ? s.getFuncionarioSolicitante().getNomeCompleto() : "")).append(';')
                    .append(escapeCsv(data)).append(';')
                    .append(escapeCsv(s.getTipoSaida())).append(';')
                    .append(escapeCsv(s.getStatus() != null ? s.getStatus().name() : "")).append(';')
                    .append(escapeCsv(s.getDescricao())).append('\n');
        }
        return sb.toString();
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private String orEmpty(Object o) {
        return o == null ? "" : o.toString();
    }

    // --- Helpers PDF table header ---
    private void addTableHeader(PdfPTable table, String[] headers) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setPhrase(new Phrase(header, font));
            // usa java.awt.Color para evitar ambiguidade no import
            cell.setBackgroundColor(new Color(200, 200, 200));
            table.addCell(cell);
        }
    }
}