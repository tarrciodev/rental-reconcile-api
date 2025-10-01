package tarrciodev.com.reconcile.services.transactions;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tarrciodev.com.reconcile.entities.ExcelTransaction;
import tarrciodev.com.reconcile.repositories.ExcelTransactionRepository;

@Service
@RequiredArgsConstructor
public class ExcelTransactionService {

    private final ExcelTransactionRepository excelTransactionRepository;

    public List<ExcelTransaction> saveAll(List<ExcelTransaction> excelTransactions) {
        return excelTransactionRepository.saveAll(excelTransactions);
    }
}
