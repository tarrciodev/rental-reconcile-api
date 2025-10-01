package tarrciodev.com.reconcile.services.reports;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import tarrciodev.com.reconcile.DTO.ReportFileDTO;

@Service
public class DownLoadReportService {

    private static final String DEFAULT_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public ReportFileDTO execute(String fileName) {
        try {
            Path filePath = Paths.get("upload").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = DEFAULT_CONTENT_TYPE;
                }

                return new ReportFileDTO(resource, contentType);
            }
            return null; // controller decide retornar 404
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar arquivo: " + fileName, e);
        }
    }

}
