package jp.bragnikita.manan.backend.services;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

public class StorageUtils {

    public static FilenameFilter filterByExtension(String... extensions) {
        return (dir, name) -> FilenameUtils.isExtension(name, extensions);
    }

    public static String[] list(Path dir) {
        return StorageUtils.list(dir, (dir1, name) -> true);
    }

    public static String[] listSubdir(Path dir) {
        return StorageUtils.list(dir, File::isDirectory);
    }

    public static String[] list(Path dir, FilenameFilter filter) {
        String[] list = dir.toFile().list(filter);
        if (list == null) {
            throw new StorageException("%s is not directory", dir.toString());
        }
        return list;
    }

    public static String[] list(Path dir, FileFilter filter) {
        File[] list = dir.toFile().listFiles(filter);
        if (list == null) {
            throw new StorageException("%s is not directory", dir.toString());
        }
        return Arrays.stream(list).map(File::getName).toArray(String[]::new);
    }

    public static FilenameFilter reverseFilter(FilenameFilter filter) {
        return (dir, name) -> !filter.accept(dir, name);
    }

    public static String lookForUniqueName(Path dir, String initialName) {
        if (!dir.resolve(initialName).toFile().exists()) {
            return initialName;
        }
        final String filename = FilenameUtils.getBaseName(initialName);
        final String ext = FilenameUtils.getExtension(initialName);
        int counter = 0;
        File f;
        String nextFilename;
        do {
            counter += 1;
            nextFilename = String.format("%s(%d).%s", filename, counter, ext);
            f = dir.resolve(nextFilename).toFile();
        } while (f.exists());
        return nextFilename;
    }

    public static String lookForNextPostingId(Path dir) {
        Optional<Integer> max = Arrays.stream(list(dir, filterByExtension("json")))
                .map(FilenameUtils::getBaseName)
                .map(s -> StringUtils.stripStart(s, "0"))
                .filter(NumberUtils::isParsable)
                .map(NumberUtils::createInteger)
                .max(Integer::compareTo);
        int next = max.orElse(0) + 1;
        return String.format("%02d", next);
    }

    public static String lookForNextSeriesId(Path dir) {
        Optional<Integer> max = Arrays.stream(listSubdir(dir))
                .map(s -> StringUtils.stripStart(s, "0"))
                .filter(NumberUtils::isParsable)
                .map(NumberUtils::createInteger)
                .max(Integer::compareTo);
        int next = max.orElse(0) + 1;
        return String.format("%03d", next);
    }

    public static void writeJson(File file, Object target) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writer(new DefaultPrettyPrinter()).writeValue(file, target);
        } catch (IOException e) {
            throw new StorageException("Can not write to file", file.toPath().toString(), e);
        }
    }
}
