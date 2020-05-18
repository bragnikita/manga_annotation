package jp.bragnikita.manan.backend.services;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.Arrays;

@Getter
public class ImageId {

    private String id;

    public ImageId(String id) {
        this.id = id;
    }

    public String getSeriesId() {
        return id.split("__")[0];
    }

    public String getFilename() {
        String ext = FilenameUtils.getExtension(id);
        String basename = FilenameUtils.getBaseName(id);
        String[] parts = basename.split("__");
        return parts[1] + "__" + parts[parts.length-1] + "." + ext;
    }

    public String getMetaFileName() {
        return FilenameUtils.getBaseName(this.getFilename()) + ".json";
    }

    public String getOriginalFilename() {
        String ext = FilenameUtils.getExtension(id);
        String basename = FilenameUtils.getBaseName(id);
        String[] parts = basename.split("__");
        return parts[1] + "." + ext;
    }

    @Builder
    public static class ImageIdGenerator {
        private String series;
        private String originalFilename;
        private String filename;


        public ImageId generate() {
            String filenamePart = filename;
            if (this.filename == null) {
                filenamePart =  FilenameUtils.getBaseName(originalFilename) + "__" + RandomStringUtils.randomAlphanumeric(5) + "." + FilenameUtils.getExtension(originalFilename);
            }
            return new ImageId(series + "__" + filenamePart);
        }
    }

    public static ImageIdGenerator.ImageIdGeneratorBuilder forSeries(String seriesId) {
        return ImageIdGenerator.builder().series(seriesId);
    }

    public static ImageIdGenerator.ImageIdGeneratorBuilder forSeries(int id) {
        return ImageIdGenerator.builder().series(StringUtils.leftPad(Integer.toString(id), 3, "0"));
    }

    public static ImageId fromPath(Path p) {
        final int nameCount = p.getNameCount();
        return ImageIdGenerator.builder().series(p.getName(nameCount-2).toString()).filename(p.getName(nameCount-1).toString()).build().generate();
    }
}
