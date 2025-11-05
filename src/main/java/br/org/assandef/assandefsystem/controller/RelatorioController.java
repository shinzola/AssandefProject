package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Doador;
import br.org.assandef.assandefsystem.model.Material;
import br.org.assandef.assandefsystem.model.SolicitacoesMaterial;
import br.org.assandef.assandefsystem.service.DoadorService;
import br.org.assandef.assandefsystem.service.MaterialService;
import br.org.assandef.assandefsystem.service.SolicitacoesMaterialService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping({"/almoxarifado/relatorio", "/doadores/relatorio"})
public class RelatorioController {

    private final MaterialService materialService;
    private final SolicitacoesMaterialService solicitacoesMaterialService;
    private final DoadorService doadorService;  // novo serviço

    public RelatorioController(MaterialService materialService,
                               SolicitacoesMaterialService solicitacoesMaterialService,
                               DoadorService doadorService) {
        this.materialService = materialService;
        this.solicitacoesMaterialService = solicitacoesMaterialService;
        this.doadorService = doadorService;
    }

    // GET dados (mantido para consumo via JS se quiser)
    @GetMapping("/dados")
    public Object getDadosRelatorio(
            @RequestParam String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        if ("Estoque    ".equalsIgnoreCase(tipo)) {
            return materialService.findAll();
        }

        if ("Solicitações".equalsIgnoreCase(tipo)) {
            return solicitacoesMaterialService.findByPeriodo(dataInicio, dataFim);
        }

        if ("doadores".equalsIgnoreCase(tipo)) {
            return doadorService.findByDataCadastroBetween(dataInicio, dataFim);
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
            } else if ("doadores".equalsIgnoreCase(tipo)) {
                List<Doador> doadores = doadorService.findByDataCadastroBetween(dataInicio, dataFim);
                csv = buildCsvDoadores(doadores);
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
                Document document = new Document(PageSize.A4.rotate()); // Paisagem
                PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

                // Evento de página para cabeçalho e rodapé
                writer.setPageEvent(new PdfPageEventHelper() {
                    public void onEndPage(PdfWriter writer, Document document) {
                        try {
                            String logoPath = "static/img/assandef-logo.png";
                            java.net.URL logoUrl = getClass().getClassLoader().getResource(logoPath);
                            if (logoUrl != null) {
                                com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(logoUrl);
                                logo.scaleToFit(60, 30);
                                logo.setAbsolutePosition(40, document.getPageSize().getHeight() - 50);
                                writer.getDirectContent().addImage(logo);
                            }

                            ColumnText.showTextAligned(
                                    writer.getDirectContent(),
                                    Element.ALIGN_CENTER,
                                    new Phrase("Relatório de " + tipo.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)),
                                    (document.right() + document.left()) / 2,
                                    document.getPageSize().getHeight() - 40,
                                    0
                            );

                            ColumnText.showTextAligned(
                                    writer.getDirectContent(),
                                    Element.ALIGN_CENTER,
                                    new Phrase("Página " + writer.getPageNumber(), FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY)),
                                    (document.right() + document.left()) / 2,
                                    20,
                                    0
                            );
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                document.open();

                if (dataInicio != null || dataFim != null) {
                    String periodo = "Período: " +
                            (dataInicio != null ? dataInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "início") +
                            " até " +
                            (dataFim != null ? dataFim.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "hoje");
                    Paragraph periodoPar = new Paragraph(periodo, FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY));
                    periodoPar.setAlignment(Element.ALIGN_CENTER);
                    document.add(periodoPar);
                    document.add(Chunk.NEWLINE);
                }

                DateTimeFormatter dtfData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter dtfDataHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                if ("Estoque".equalsIgnoreCase(tipo)) {
                    List<Material> materiais = materialService.findAll();
                    PdfPTable table = new PdfPTable(new float[]{3, 2, 1, 2, 2});
                    table.setWidthPercentage(100);
                    addTableHeader(table, new String[]{"Nome", "Categoria", "Quantidade", "Validade", "Fornecedor"});

                    for (Material m : materiais) {
                        table.addCell(createBodyCell(orEmpty(m.getNome())));
                        table.addCell(createBodyCell(orEmpty(m.getCategoria() != null ? m.getCategoria().getNome() : null)));
                        table.addCell(createBodyCell(String.valueOf(m.getQuantidadeAtual() != null ? m.getQuantidadeAtual() : 0)));
                        table.addCell(createBodyCell(m.getDataValidade() != null ? m.getDataValidade().format(dtfData) : "Sem validade"));
                        table.addCell(createBodyCell(orEmpty(m.getFornecedor())));
                    }
                    document.add(table);

                } else if ("Solicitações".equalsIgnoreCase(tipo)) {
                    List<SolicitacoesMaterial> solicitacoes = solicitacoesMaterialService.findByPeriodo(dataInicio, dataFim);
                    PdfPTable table = new PdfPTable(new float[]{2, 1, 2, 2, 1, 2, 2});
                    table.setWidthPercentage(100);
                    addTableHeader(table, new String[]{"Material", "Quantidade", "Solicitante", "Data", "Tipo Saída", "Status", "Justificativa"});

                    for (SolicitacoesMaterial s : solicitacoes) {
                        table.addCell(createBodyCell(orEmpty(s.getMaterial() != null ? s.getMaterial().getNome() : null)));
                        table.addCell(createBodyCell(String.valueOf(s.getQuantidadeSolicitada() != null ? s.getQuantidadeSolicitada() : 0)));
                        table.addCell(createBodyCell(orEmpty(s.getFuncionarioSolicitante() != null ? s.getFuncionarioSolicitante().getNomeCompleto() : null)));
                        table.addCell(createBodyCell(s.getDataSolicitacao() != null ? s.getDataSolicitacao().format(dtfDataHora) : ""));
                        table.addCell(createBodyCell(orEmpty(s.getTipoSaida())));
                        table.addCell(createBodyCell(s.getStatus() != null ? s.getStatus().name() : ""));
                        table.addCell(createBodyCell(orEmpty(s.getDescricao())));
                    }
                    document.add(table);

                } else if ("doadores".equalsIgnoreCase(tipo)) {
                    List<Doador> doadores = doadorService.findByDataCadastroBetween(dataInicio, dataFim);
                    PdfPTable table = new PdfPTable(new float[]{2,2, 2, 3, 1, 2, 2,2});
                    table.setWidthPercentage(100);
                    addTableHeader(table, new String[]{"Nome", "CPF/CNPJ", "Telefone", "Email", "Mensalidade", "Data Nascimento", "Sexo","Data cadastro"});

                    DateTimeFormatter dtfNascimento = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    DateTimeFormatter dtfCadastro = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    for (Doador d : doadores) {
                        table.addCell(createBodyCell(orEmpty(d.getNome())));
                        table.addCell(createBodyCell(orEmpty(d.getCpfCnpj())));
                        table.addCell(createBodyCell(orEmpty(d.getTelefone())));
                        table.addCell(createBodyCell(orEmpty(d.getEmail())));
                        table.addCell(createBodyCell(d.getMensalidade() != null ? String.format("R$ %.2f", d.getMensalidade()) : ""));
                        table.addCell(createBodyCell(d.getDataNascimento() != null ? d.getDataNascimento().format(dtfNascimento) : ""));
                        table.addCell(createBodyCell(orEmpty(d.getSexo())));
                        table.addCell(createBodyCell(orEmpty(d.getDataCadastro()!= null ? d.getDataCadastro().format(dtfCadastro) : "")));
                    }
                    document.add(table);

                } else {
                    document.add(new Paragraph("Tipo de relatório inválido."));
                }

                document.close();
            } catch (Exception e) {
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
        sb.append("Material;Quantidade;Solicitante;Data;Tipo de Saída;Status;Justificativa\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (SolicitacoesMaterial s : solicitacoes) {
            String data = s.getDataSolicitacao() != null ? s.getDataSolicitacao().format(dtf) : "";
            sb.append(escapeCsv(s.getMaterial() != null ? s.getMaterial().getNome() : "")).append(';')
                    .append(s.getQuantidadeSolicitada() != null ? s.getQuantidadeSolicitada() : 0).append(';')
                    .append(escapeCsv(s.getFuncionarioSolicitante() != null ? s.getFuncionarioSolicitante().getNomeCompleto() : "")).append(';')
                    .append(escapeCsv(data)).append(';')
                    .append(escapeCsv(s.getTipoSaida())).append(';')
                    .append(escapeCsv(s.getStatus() != null ? s.getStatus().name() : "")).append(';')
                    .append(escapeCsv(s.getDescricao())).append('\n');
        }
        return sb.toString();
    }

    private String buildCsvDoadores(List<Doador> doadores) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nome;CPF/CNPJ;Telefone;Email;Mensalidade;Data de Nascimento;Sexo;Data Cadastrada\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Doador d : doadores) {
            sb.append(escapeCsv(d.getNome())).append(';')
                    .append(escapeCsv(d.getCpfCnpj())).append(';')
                    .append(escapeCsv(d.getTelefone())).append(';')
                    .append(escapeCsv(d.getEmail())).append(';')
                    .append(d.getMensalidade() != null ? String.format("%.2f", d.getMensalidade()) : "").append(';')
                    .append(d.getDataNascimento() != null ? d.getDataNascimento().format(dtf) : "").append(';')
                    .append(escapeCsv(d.getSexo())).append(';')
                    .append(d.getDataCadastro() != null ? d.getDataCadastro().format(dtf) : "").append('\n');

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
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(new Color(70, 130, 180)); // azul moderno
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            // padding maior
            cell.setPaddingTop(8f);
            cell.setPaddingBottom(8f);

            // altura mínima para não ficar espremido
            cell.setMinimumHeight(26f);

            table.addCell(cell);
        }
        // garante que a primeira linha será repetida como cabeçalho em páginas seguintes
        table.setHeaderRows(1);
    }

    // helper para criar células do corpo com mais espaço e wrap amigável
    private PdfPCell createBodyCell(String text) {
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Phrase phrase = new Phrase();
        phrase.setLeading(14f);
        phrase.add(new Chunk(text == null ? "" : text, bodyFont));

        PdfPCell cell = new PdfPCell(phrase);
        cell.setPaddingTop(6f);
        cell.setPaddingBottom(6f);
        cell.setPaddingLeft(4f);
        cell.setPaddingRight(4f);
        cell.setMinimumHeight(20f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setNoWrap(false);
        return cell;
    }
}