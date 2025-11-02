package br.org.assandef.assandefsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads/boletos}")
    private String uploadDir;

    /**
     * Salva o arquivo PDF e retorna o caminho relativo
     *
     * @param file Arquivo enviado pelo usuário
     * @param idDoador ID do doador (para organizar em pastas)
     * @param idBoleto ID do boleto (para nome único do arquivo)
     * @return Caminho relativo: "doador_1/2025-01/boleto_123.pdf"
     */
    public String salvarBoleto(MultipartFile file, Integer idDoador, Integer idBoleto) throws IOException {
        System.out.println("📁 FileStorageService.salvarBoleto() chamado!");
        System.out.println("   - idDoador: " + idDoador);
        System.out.println("   - idBoleto: " + idBoleto);

        // Validações
        if (file.isEmpty()) {
            throw new IOException("Arquivo vazio");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        System.out.println("   - Nome original: " + originalFilename);

        // Validar extensão
        if (!originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IOException("Apenas arquivos PDF são permitidos");
        }

        // Validar tamanho (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IOException("Arquivo muito grande. Máximo: 10MB");
        }

        // Criar estrutura de pastas: uploads/boletos/doador_{id}/{ano-mes}/
        String anoMes = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String subDiretorio = "doador_" + idDoador + "/" + anoMes;
        Path diretorioDestino = Paths.get(uploadDir, subDiretorio);

        System.out.println("   - Diretório destino: " + diretorioDestino.toAbsolutePath());

        // AQUI: Cria automaticamente as pastas se não existirem!
        Files.createDirectories(diretorioDestino);
        System.out.println("   ✅ Diretório criado/verificado");

        // Nome do arquivo: boleto_{id}_{uuid}.pdf (para evitar duplicatas)
        String nomeArquivo = "boleto_" + idBoleto + "_" + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
        Path arquivoDestino = diretorioDestino.resolve(nomeArquivo);

        System.out.println("   - Arquivo destino: " + arquivoDestino.toAbsolutePath());

        // Copiar arquivo para o destino
        Files.copy(file.getInputStream(), arquivoDestino, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("   ✅ Arquivo copiado com sucesso!");

        // Retornar caminho relativo (para salvar no banco)
        String caminhoRelativo = subDiretorio + "/" + nomeArquivo;
        System.out.println("   - Caminho relativo: " + caminhoRelativo);

        return caminhoRelativo;
    }

    /**
     * Deleta o arquivo do disco
     *
     * @param caminhoRelativo Caminho salvo no banco: "doador_1/2025-01/boleto_123.pdf"
     */
    public void deletarBoleto(String caminhoRelativo) throws IOException {
        if (caminhoRelativo == null || caminhoRelativo.isBlank()) {
            return; // Sem arquivo para deletar
        }

        Path arquivo = Paths.get(uploadDir, caminhoRelativo);

        if (Files.exists(arquivo)) {
            Files.delete(arquivo);
            System.out.println("✅ Arquivo deletado: " + arquivo);
        }
    }

    /**
     * Retorna o Path completo do arquivo
     *
     * @param caminhoRelativo Caminho salvo no banco
     * @return Path absoluto do arquivo
     */
    public Path obterArquivo(String caminhoRelativo) {
        return Paths.get(uploadDir, caminhoRelativo);
    }
}