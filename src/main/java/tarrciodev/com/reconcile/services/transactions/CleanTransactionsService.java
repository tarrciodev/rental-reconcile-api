package tarrciodev.com.reconcile.services.transactions;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tarrciodev.com.reconcile.repositories.BankStatementRepository;
import tarrciodev.com.reconcile.repositories.ExcelTransactionRepository;

@RequiredArgsConstructor
@Service
public class CleanTransactionsService {
    private final BankStatementRepository bankStatementRepository;
    private final ExcelTransactionRepository excelTransactionRepository;

    public void execute(){
        this.bankStatementRepository.deleteAll();
        this.excelTransactionRepository.deleteAll();
    }
}
