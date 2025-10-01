package tarrciodev.com.reconcile.services.transactions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tarrciodev.com.reconcile.entities.BankStatement;
import tarrciodev.com.reconcile.entities.ExcelTransaction;
import tarrciodev.com.reconcile.repositories.BankStatementRepository;
import tarrciodev.com.reconcile.repositories.ExcelTransactionRepository;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final BankStatementRepository bankStatementRepository;
    private final ExcelTransactionRepository excelTransactionRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Formato padronizado para ambos
    private final DateTimeFormatter standardDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Mono<ProcessResult> processAndSaveN8nData(Object n8nResponse) {
        System.out.println("Processando resposta do n8n..." + n8nResponse);
        return Mono.fromCallable(() -> {
            ProcessResult result = new ProcessResult();
            
            String jsonResponse = objectMapper.writeValueAsString(n8nResponse);
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            System.out.println("Processando resposta do n8n...");
            
            if (rootNode.isArray()) {
                for (JsonNode item : rootNode) {
                    // Processa extrato
                    if (item.has("extrato") && item.get("extrato").isArray()) {
                        System.out.println("Encontrado extrato com " + item.get("extrato").size() + " registros");
                        List<BankStatement> bankStatements = processExtratoData(item.get("extrato"));
                        if (!bankStatements.isEmpty()) {
                            List<BankStatement> saved = bankStatementRepository.saveAll(bankStatements);
                            result.setBankStatementsSaved(saved.size());
                            System.out.println("Salvos " + saved.size() + " bank statements");
                        }
                    }
                    
                    // Processa excel
                    if (item.has("excel") && item.get("excel").isArray()) {
                        System.out.println("Encontrado excel com " + item.get("excel").size() + " registros");
                        List<ExcelTransaction> excelTransactions = processExcelData(item.get("excel"));
                        if (!excelTransactions.isEmpty()) {
                            List<ExcelTransaction> saved = excelTransactionRepository.saveAll(excelTransactions);
                            result.setExcelTransactionsSaved(saved.size());
                            System.out.println("Salvos " + saved.size() + " excel transactions");
                        }
                    }
                }
            }
            
            System.out.println("Processamento concluído: " + result.getBankStatementsSaved() + " bank statements, " + result.getExcelTransactionsSaved() + " excel transactions");
            
            // ✅ APENAS SALVA OS DADOS - NÃO FAZ RECONCILIAÇÃO
            System.out.println("✅ Dados salvos com sucesso. Reconciliação deve ser feita separadamente.");
            
            return result;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Processa dados do extrato para BankStatement - converte de "dd/MM/yyyy" para "yyyy-MM-dd"
     * Débitos são sempre negativos, créditos sempre positivos
     */
    private List<BankStatement> processExtratoData(JsonNode extratoArray) {
        List<BankStatement> bankStatements = new ArrayList<>();
        
        for (JsonNode extratoItem : extratoArray) {
            try {
                BankStatement bankStatement = new BankStatement();
                
                // Data no formato "02/01/2025" -> converte para "2025-01-02"
                if (extratoItem.has("data")) {
                    String dataStr = extratoItem.get("data").asText();
                    LocalDate data = LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    bankStatement.setData(data);
                }
                
                if (extratoItem.has("description")) {
                    bankStatement.setDescription(extratoItem.get("description").asText());
                }
                
                // Calcula amount + tag
                AmountResult result = calculateAmount(extratoItem);
                bankStatement.setAmount(result.amount);
                bankStatement.setTag(result.tag);
                
                System.out.printf("BankStatement: %s | %s | %.2f | %s%n", 
                    bankStatement.getData(), bankStatement.getDescription(), result.amount, result.tag);
                
                bankStatements.add(bankStatement);
                
            } catch (Exception e) {
                System.err.println("Erro ao processar item do extrato: " + extratoItem.toString());
                e.printStackTrace();
            }
        }
        
        return bankStatements;
    }

    /**
     * Processa dados do excel para ExcelTransaction - já está em "yyyy-MM-dd"
     * Débitos são sempre negativos, créditos sempre positivos
     */
    private List<ExcelTransaction> processExcelData(JsonNode excelArray) {
        List<ExcelTransaction> excelTransactions = new ArrayList<>();
        
        for (JsonNode excelItem : excelArray) {
            try {
                ExcelTransaction excelTransaction = new ExcelTransaction();
                
                // Data no formato "2025-01-02" (já é o padrão)
                if (excelItem.has("data")) {
                    String dataStr = excelItem.get("data").asText();
                    excelTransaction.setData(LocalDate.parse(dataStr));
                }
                
                if (excelItem.has("description")) {
                    excelTransaction.setDescription(excelItem.get("description").asText());
                }
                
                // Calcula amount + tag
                AmountResult result = calculateAmount(excelItem);
                excelTransaction.setAmount(result.amount);
                excelTransaction.setTag(result.tag);
                
                System.out.printf("ExcelTransaction: %s | %s | %.2f | %s%n", 
                    excelTransaction.getData(), excelTransaction.getDescription(), result.amount, result.tag);
                
                excelTransactions.add(excelTransaction);
                
            } catch (Exception e) {
                System.err.println("Erro ao processar item do excel: " + excelItem.toString());
                e.printStackTrace();
            }
        }
        
        return excelTransactions;
    }

    /**
     * Calcula o amount e define a tag (credito ou debito)
     */
    private AmountResult calculateAmount(JsonNode item) {
        Double credito = null;
        Double debito = null;
        
        if (item.has("credito") && !item.get("credito").isNull()) {
            credito = item.get("credito").asDouble();
        }
        
        if (item.has("debito") && !item.get("debito").isNull()) {
            debito = item.get("debito").asDouble();
        }
        
        if (credito != null && debito != null) {
            double amount = credito - debito;
            String tag = amount >= 0 ? "credito" : "debito";
            return new AmountResult(amount, tag);
        } else if (credito != null) {
            return new AmountResult(credito, "credito");
        } else if (debito != null) {
            return new AmountResult(-debito, "debito");
        } else {
            return new AmountResult(null, null);
        }
    }

    /**
     * Estrutura auxiliar para guardar valor + tag
     */
    private static class AmountResult {
        Double amount;
        String tag;
        AmountResult(Double amount, String tag) {
            this.amount = amount;
            this.tag = tag;
        }
    }

    public static class ProcessResult {
        private int bankStatementsSaved;
        private int excelTransactionsSaved;
        
        public int getBankStatementsSaved() { return bankStatementsSaved; }
        public void setBankStatementsSaved(int bankStatementsSaved) { this.bankStatementsSaved = bankStatementsSaved; }
        public int getExcelTransactionsSaved() { return excelTransactionsSaved; }
        public void setExcelTransactionsSaved(int excelTransactionsSaved) { this.excelTransactionsSaved = excelTransactionsSaved; }
        public int getTotalSaved() { return bankStatementsSaved + excelTransactionsSaved; }
    }
}
