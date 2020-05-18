package jp.bragnikita.manan.backend.services.files;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Getter @Setter @NoArgsConstructor
public class PageMeta {
    private String imageFilename;
    private String updatedAt;
    private JsonNode meta;

    public void updateNow() {
        this.updatedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}
