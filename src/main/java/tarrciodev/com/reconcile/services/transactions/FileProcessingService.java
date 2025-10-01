package tarrciodev.com.reconcile.services.transactions;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.TimeoutException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final WebClient webClient;

    @Value("${n8n.webhook.url}")
    private String n8nWebhookUrl;

    public Mono<Object> uploadToN8n(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Mono.error(new IllegalArgumentException("No files uploaded"));
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        for (MultipartFile file : files) {
            builder.part("files", file.getResource())
                   .filename(file.getOriginalFilename())
                   .contentType(MediaType.parseMediaType(file.getContentType()));
        }

        return webClient.post()
                .uri(n8nWebhookUrl)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(throwable -> {
                    Map<String, Object> errorResponse = new HashMap<>();

                    if (throwable instanceof TimeoutException) {
                        errorResponse.put("error", "Processing timeout");
                        errorResponse.put("message", "File processing took longer than 4 minutes.");
                    } else if (throwable instanceof ReadTimeoutException) {
                        errorResponse.put("error", "Read timeout");
                        errorResponse.put("message", "The connection to the processing service timed out.");
                    } else {
                        errorResponse.put("error", "Failed to process files");
                        errorResponse.put("message",
                                throwable.getMessage() != null ? throwable.getMessage() : "Unknown error occurred");
                    }

                    errorResponse.put("timestamp", Instant.now().toString());
                    return Mono.just(errorResponse);
                });
    }
}
