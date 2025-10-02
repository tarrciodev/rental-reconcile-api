package tarrciodev.com.reconcile.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tarrciodev.com.reconcile.entities.ExcelTransaction;
import tarrciodev.com.reconcile.repositories.ExcelTransactionRepository;


@RestController
@RequestMapping("/excel")
@CrossOrigin
@RequiredArgsConstructor
public class GetExcelDataController {
    private final ExcelTransactionRepository excelTransactionRepository;

    @GetMapping
    public List<ExcelTransaction> getAll() {
        return excelTransactionRepository.findAll();
    }
}
