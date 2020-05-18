package jp.bragnikita.manan.backend.controllers.v1;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.bragnikita.manan.backend.controllers.v1.types.Page;
import jp.bragnikita.manan.backend.controllers.v1.types.Responses;
import jp.bragnikita.manan.backend.controllers.v1.types.Series;
import jp.bragnikita.manan.backend.services.ImageId;
import jp.bragnikita.manan.backend.services.SeriesOperations;
import jp.bragnikita.manan.backend.services.StorageException;
import jp.bragnikita.manan.backend.services.StorageUtils;
import jp.bragnikita.manan.backend.services.files.PageMeta;
import jp.bragnikita.manan.backend.services.files.SeriesMeta;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SeriesController {

    @Value("${app.timeline.storage.root}")
    public String storageLocation;

    private final SeriesOperations operations;

    public SeriesController(SeriesOperations operations) {
        this.operations = operations;
    }


    @GetMapping("/series")
    public Responses.Listing listing(UriComponentsBuilder builder) {
        List<Series.Brief> l = this.operations.allSeriesLock(files -> Arrays.stream(files).sorted(File::compareTo).map(file -> {
            String seriesId = file.getName();
            final SeriesMeta seriesMeta = this.operations.readSeriesMeta(seriesId);
            Series.Brief item = new Series.Brief();
            item.setId(seriesId);
            item.setTitle(seriesMeta.getTitle());
            item.setDescription(seriesMeta.getDescription());
            item.setPublishingStatus(seriesMeta.getPublishingStatus());
            item.setTranslationStatus(seriesMeta.getTranslationStatus());
            if (seriesMeta.getCover() != null) {
                ImageId iId = ImageId.forSeries(seriesId).filename(seriesMeta.getCover()).build().generate();
                item.setCoverId(iId.getId());
                item.setCoverUrl(builder.cloneBuilder().path("/static/" + seriesId + "/" + iId.getFilename()).toUriString());
            }
            return item;
        }).collect(Collectors.toList()));
        return Responses.Listing.fromList(l);
    }

    @GetMapping("/series/{id}")
    public Series.Full one(@PathVariable("id") String seriesId, UriComponentsBuilder uriBuilder) {

        final SeriesMeta seriesMeta = this.operations.readSeriesMeta(seriesId);
        final Series.Full result = new Series.Full();
        final Series.Brief info = new Series.Brief();
        info.setId(seriesId);
        if (seriesMeta.getCover() != null) {
            final ImageId imageId = ImageId.forSeries(seriesId).filename(seriesMeta.getCover()).build().generate();
            info.setCoverId(imageId.getId());
            info.setCoverUrl(uriBuilder.cloneBuilder().path("/static/" + seriesId + "/" + imageId.getFilename()).toUriString());
        }
        info.setTitle(seriesMeta.getTitle());
        info.setDescription(seriesMeta.getDescription());
        info.setPublishingStatus(seriesMeta.getPublishingStatus());
        info.setTranslationStatus(seriesMeta.getTranslationStatus());
        this.operations.lockForSeries(seriesId, path -> {
            Page.Preview[] pages = Arrays.stream(seriesMeta.getPages()).map(s -> {
                Page.Preview p = new Page.Preview();
                ImageId pId = ImageId.forSeries(seriesId).filename(s).build().generate();
                p.setId(pId.getId());
                p.setOriginalFileName(pId.getOriginalFilename());
                p.setPreviewUrl(uriBuilder.cloneBuilder().path("/static/" + seriesId + "/" + pId.getFilename()).toUriString());
                return p;
            }).toArray(Page.Preview[]::new);
            result.setInfo(info);
            result.setPages(pages);
            return "";
        });

        return result;
    }

    @PostMapping("/series")
    public Series.Brief create(@RequestBody Series.CreateUpdate req) {
        final Series.CreateUpdateInfo series = req.getInfo();
        final SeriesMeta meta = new SeriesMeta();
        meta.setTitle(series.getTitle());
        meta.setDescription(series.getDescription());
        meta.createNow();

        String id = this.operations.createNewSeries(file -> {
            StorageUtils.writeJson(file, meta);
        });

        Series.Brief item = new Series.Brief();
        item.setId(id);
        item.setTitle(meta.getTitle());
        item.setDescription(meta.getDescription());
        return item;
    }

    @PutMapping("/series/{id}")
    public void update(@RequestBody Series.CreateUpdate series, @PathVariable("id") String seriesId) {
        this.operations.updateSeries(seriesId, meta -> {
            if (series.getInfo() != null) {
                final Series.CreateUpdateInfo info = series.getInfo();
                meta.setTitle(info.getTitle());
                meta.setDescription(info.getDescription());
                if (info.getCoverId() != null) {
                    ImageId iId = new ImageId(info.getCoverId());
                    meta.setCover(iId.getFilename());
                } else {
                    meta.setCover(null);
                }
                meta.setPublishingStatus(info.getPublishingStatus());
                meta.setTranslationStatus(info.getTranslationStatus());
            }
            if (series.getPages() != null) {
                meta.setPages(series.getPages());
            }
        });
    }

    @PostMapping("/series/{id}/pages")
    public Page.Preview uploadPage(@RequestParam("file") MultipartFile image, @PathVariable("id") String series) throws IOException {
        String filename = Optional.ofNullable(image.getOriginalFilename()).orElse("somefile");

        ImageId id = ImageId.forSeries(series).originalFilename(filename).build().generate();
        String path = operations.lockForSeries(id.getSeriesId(), seriesRoot -> {
            try {
                Files.copy(image.getInputStream(), seriesRoot.resolve(id.getFilename()), StandardCopyOption.REPLACE_EXISTING);
                operations.updateSeries(id.getSeriesId(), seriesMeta -> {
                    seriesMeta.addPage(id.getFilename());
                });
                return Path.of("/static", id.getSeriesId(), id.getFilename()).toString();
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
                ioe.printStackTrace();
                throw new StorageException("save image", ioe);
            }
        });

        Page.Preview page = new Page.Preview();
        page.setOriginalFileName(filename);
        page.setId(id.getId());
        page.setPreviewUrl(path);
        return page;
    }

    @GetMapping("/pages/{id}")
    public Page.Full getPage(@PathVariable("id") String pageId, UriComponentsBuilder uri) throws IOException {
        Page.Full response = new Page.Full();
        response.setId(pageId);
        ImageId id = new ImageId(pageId);
        response.setImageUrl(uri.path(Path.of("/static", id.getSeriesId(), id.getFilename()).toString()).toUriString());
        ObjectMapper mapper = new ObjectMapper();
        final File metaFile = operations.getStorageRoot().resolve(id.getSeriesId()).resolve(id.getMetaFileName()).toFile();
        if (metaFile.exists()) {
            response.setMeta(mapper.readValue(metaFile, PageMeta.class).getMeta());
        }
        return response;
    }

    @PutMapping("/pages/{id}")
    public void updatePage(@PathVariable("id") String pageId, @RequestBody Page.UpdateRequest request) {
        ImageId id = new ImageId(pageId);
        operations.lockForSeries(id.getSeriesId(), path -> {
            ObjectMapper mapper = new ObjectMapper();
            File targetFile = path.resolve(id.getMetaFileName()).toFile();
            PageMeta meta = new PageMeta();
            meta.setImageFilename(id.getFilename());
            meta.setMeta(request.getMeta());
            meta.updateNow();
            try {
                mapper.writer(new DefaultPrettyPrinter()).writeValue(targetFile, meta);
            } catch (Exception e) {
                throw new StorageException("save page meta", targetFile.toPath(), e );
            }
            return meta;
        });
    }

    @DeleteMapping("/pages/{id}")
    public void deletePage(@PathVariable("id") String pageId) {
        ImageId iId = new ImageId(pageId);
        Boolean result = operations.lockForSeries(iId.getSeriesId(), path -> {
            operations.updateSeries(iId.getSeriesId(), seriesMeta -> {
                seriesMeta.removePage(iId.getFilename());
            });
            return FileUtils.deleteQuietly(path.resolve(iId.getFilename()).toFile());
        });
    }

    @DeleteMapping("/series/{id}")
    public void deleteSeries(@PathVariable("id") String series) {
        this.operations.lockForSeries(series, path -> {
            try {
                FileUtils.forceDelete(path.toFile());
                return true;
            } catch (IOException e) {
                throw new StorageException("delete", path, e);
            }
        });
    }

}
