package ut.edu.project.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private Path rootLocation;

    @Value("${app.upload.travel-dir:project/src/main/resources/static/travel_images}")
    private String uploadDir;

    public FileStorageService() {
        // Constructor rỗng - khởi tạo sẽ được thực hiện trong phương thức init()
    }

    @PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(uploadDir);
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        // Tạo tên file duy nhất để tránh trùng lặp
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String fileName = System.currentTimeMillis() + "_" + Math.round(Math.random() * 1000) + fileExtension;

        // Lưu file
        Path filePath = rootLocation.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn tương đối
        return "/travel_images/" + fileName;
    }
}
