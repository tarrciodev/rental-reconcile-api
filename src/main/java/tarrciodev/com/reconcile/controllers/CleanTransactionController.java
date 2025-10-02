package tarrciodev.com.reconcile.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tarrciodev.com.reconcile.repositories.BankStatementRepository;
import tarrciodev.com.reconcile.repositories.ExcelTransactionRepository;

@RestController
@RequestMapping("/transactions")
@CrossOrigin
@RequiredArgsConstructor
public class CleanTransactionController {
    private final BankStatementRepository bankStatementRepository;
    private final ExcelTransactionRepository excelTransactionRepository;

    @DeleteMapping("/clean")
    public void clean() {
        bankStatementRepository.deleteAllInBatch();
        excelTransactionRepository.deleteAllInBatch();

        System.out.println("Database cleaned successfully");
    }
}
