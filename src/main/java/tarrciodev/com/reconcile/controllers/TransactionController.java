package tarrciodev.com.reconcile.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import tarrciodev.com.reconcile.UseCases.UploadTransactionsUseCase;

@RestController
@CrossOrigin(origins = "*") 
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final UploadTransactionsUseCase uploadTransactionsUseCase;

    @PostMapping("/upload")
    public Mono<ResponseEntity<?>> uploadTransactions(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(Map.of("error", "No files uploaded")));
        }

        var response = uploadTransactionsUseCase.execute(files);
        System.out.println(response);
        return response;
    }
}


