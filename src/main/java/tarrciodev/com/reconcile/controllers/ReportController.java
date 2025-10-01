package tarrciodev.com.reconcile.controllers;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tarrciodev.com.reconcile.DTO.ReportFileDTO;
import tarrciodev.com.reconcile.services.reports.DownLoadReportService;


@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final  DownLoadReportService downLoadReportService;

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadReport(@PathVariable String fileName) {
        try {
            ReportFileDTO report = downLoadReportService.execute(fileName);

            if (report == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(report.contentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + report.resource().getFilename() + "\"")
                    .body(report.resource());

        } catch (Exception e) {
            System.err.println("💥 Erro no download: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
