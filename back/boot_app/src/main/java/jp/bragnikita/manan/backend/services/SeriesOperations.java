package jp.bragnikita.manan.backend.services;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jp.bragnikita.manan.backend.services.files.SeriesMeta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Scope("singleton")
public class SeriesOperations {

    @Value("${app.timeline.storage.root}")
    private String storagePath;

    private ReentrantLock globalMutex = new ReentrantLock();

    public String nextSeriesId() {
        globalMutex.lock();
        try {
            return StorageUtils.lookForNextSeriesId(this.getStorageRoot());
        } finally {
            globalMutex.unlock();
        }
    }

    public <T> T allSeriesLock(Function<File[],T> consumer) {
        globalMutex.lock();
        try {
            return consumer.apply(Arrays.stream(StorageUtils.list(this.getStorageRoot(), File::isDirectory))
                    .map(s -> this.getStorageRoot()
                            .resolve(s).toFile()).toArray(File[]::new));
        } finally {
            globalMutex.unlock();
        }
    }

    public SeriesMeta readSeriesMeta(String seriesId) {
        globalMutex.lock();
        try {
            Path seriesPath = this.getStorageRoot().resolve(seriesId);
            if (!seriesPath.toFile().exists())
                throw new StorageException("Series does not exists", seriesId);
            Path metaFile = seriesPath.resolve("__meta.json");
            if (!metaFile.toFile().exists()) {
                throw new StorageException("Series does not exists", seriesId);
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(metaFile.toFile(), SeriesMeta.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new StorageException("Can not read series", e);
        } finally {
            globalMutex.unlock();
        }
    }

    public String createNewSeries(Consumer<File> initializer) {
        globalMutex.lock();
        try {
            String nextId = this.nextSeriesId();
            Path seriesPath = this.getStorageRoot().resolve(nextId);
            Files.createDirectories(seriesPath);
            Path meta = seriesPath.resolve("__meta.json");
            initializer.accept(meta.toFile());
            return nextId;
        } catch (IOException e) {
            throw new StorageException("Can not create series", e);
        } finally {
            globalMutex.unlock();
        }
    }

    public SeriesMeta updateSeries(String seriesId, Consumer<SeriesMeta> updater) {
        globalMutex.lock();
        try {
            Path seriesPath = this.getStorageRoot().resolve(seriesId);
            if (!seriesPath.toFile().exists())
                throw new StorageException("Series does not exists", seriesId);
            Path metaFile = seriesPath.resolve("__meta.json");
            ObjectMapper mapper = new ObjectMapper();
            SeriesMeta meta = mapper.readValue(metaFile.toFile(), SeriesMeta.class);
            updater.accept(meta);
            meta.touch();
            mapper.writer(new DefaultPrettyPrinter()).writeValue(metaFile.toFile(), meta);
            return meta;
        } catch (IOException e) {
            throw new StorageException("Can not create series", e);
        } finally {
            globalMutex.unlock();
        }
    }

    public <T> T lockForSeries(String seriesId, Function<Path, T> handler) {
        globalMutex.lock();
        try {
            Path seriesPath = getStorageRoot().resolve(seriesId);
            if (seriesPath.toFile().exists() && seriesPath.toFile().isDirectory()) {
                return handler.apply(seriesPath);
            }
            throw new StorageException("Series is not exists", seriesId, null);
        } finally {
            globalMutex.unlock();
        }
    }


    public Path getStorageRoot() {
        return Path.of(storagePath).toAbsolutePath();
    }

}
