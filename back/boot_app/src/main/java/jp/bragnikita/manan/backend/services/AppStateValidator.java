package jp.bragnikita.manan.backend.services;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;

@Component
public class AppStateValidator implements ApplicationRunner, DisposableBean {

    @Value("${app.timeline.storage.root}")
    private String storagePath;


    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path storagePath = Path.of(this.storagePath);
        if (!storagePath.isAbsolute()) {
            storagePath = storagePath.toAbsolutePath();
        }
        File f = storagePath.toFile();
        if (!f.exists() || !f.isDirectory()) {
            throw new StorageException("Storage directory is not exists", storagePath.toString());
        }
    }
}
