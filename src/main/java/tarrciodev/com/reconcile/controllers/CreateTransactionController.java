package tarrciodev.com.reconcile.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tarrciodev.com.reconcile.entities.BankStatement;
import tarrciodev.com.reconcile.repositories.BankStatementRepository;


@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/transaction")
public class CreateTransactionController {
    private final BankStatementRepository bankRepo;
   
     @GetMapping
    public ResponseEntity<List<BankStatement>> getAll(){
        return ResponseEntity.ok(bankRepo.findAll());
    }
}
