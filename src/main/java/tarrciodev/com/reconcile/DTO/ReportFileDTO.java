package tarrciodev.com.reconcile.DTO;

import org.springframework.core.io.Resource;

public record ReportFileDTO(Resource resource, String contentType) {}
