package tarrciodev.com.reconcile.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tarrciodev.com.reconcile.repositories.BankStatementRepository;
import tarrciodev.com.reconcile.repositories.ExcelTransactionRepository;

@RestController
@RequestMapping("/clean")
@RequiredArgsConstructor
public class CleanDatabaseController {
    private final BankStatementRepository bankStatementRepository;
    private final ExcelTransactionRepository excelTransactionRepository;

    @DeleteMapping
    public void clean() {
        bankStatementRepository.deleteAll();
        excelTransactionRepository.deleteAll();

        System.out.println("Database cleaned successfully");
    }
}
