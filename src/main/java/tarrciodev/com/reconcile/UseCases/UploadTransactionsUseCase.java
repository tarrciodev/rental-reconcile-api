package tarrciodev.com.reconcile.UseCases;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import tarrciodev.com.reconcile.DTO.ReconciliationSummaryDTO;
import tarrciodev.com.reconcile.services.reconciliations.ReconciliationService;
import tarrciodev.com.reconcile.services.transactions.FileProcessingService;
import tarrciodev.com.reconcile.services.transactions.TransactionService;

@Service
@RequiredArgsConstructor
public class UploadTransactionsUseCase {

    private final FileProcessingService fileProcessingService;
    private final TransactionService transactionService;
    private final ReconciliationService reconciliationService;
    public Mono<ResponseEntity<?>> execute(MultipartFile[] files) {
        return fileProcessingService.uploadToN8n(files) 
                .flatMap(n8nResponse -> 
                    transactionService.processAndSaveN8nData(n8nResponse) 
                        .map(result -> {
                            Map<String, Object> responseBody = new HashMap<>();
                            responseBody.put("success", true);
                            responseBody.put("message", "Data processed and saved successfully");
                            responseBody.put("bankStatementsSaved", result.getBankStatementsSaved());
                            responseBody.put("excelTransactionsSaved", result.getExcelTransactionsSaved());
                            responseBody.put("totalRecordsSaved", result.getTotalSaved());
                            List<ReconciliationSummaryDTO> reconciliation = reconciliationService.reconcile();
                            responseBody.put("reconciliation", reconciliation);
                            return ResponseEntity.ok(responseBody);
                        })
                );
    }
}
