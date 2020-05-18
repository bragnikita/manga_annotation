package jp.bragnikita.manan.backend.services.files;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Getter @Setter @NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SeriesMeta {
    String title;
    String description;
    String cover;
    String creator;
    String owner;
    String createdAt;
    String updatedAt;
    String translationStatus;
    String publishingStatus;
    String[] pages = new String[]{};

    public void touch() {
        this.updatedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    public void createNow() {
        this.createdAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.touch();
    }

    public void addPage(String filename) {
        this.pages = ArrayUtils.add(this.pages, filename);
    }

    public void removePage(String filename) {
        this.pages = ArrayUtils.removeElement(this.pages, filename);
    }
}
