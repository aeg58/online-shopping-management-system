package com.osms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service("productImages")
public class ProductImageService {

    public static final long MAX_BYTES = 2L * 1024 * 1024;
    private static final List<String> EXTENSIONS = List.of("jpg", "png", "webp");
    private static final String PLACEHOLDER = "/images/products/placeholder.svg";

    private final Path uploadDir;

    public ProductImageService(@Value("${osms.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir, "products").toAbsolutePath().normalize();
    }

    public Path getUploadDir() {
        return uploadDir;
    }

    public String url(int productId) {
        for (String ext : EXTENSIONS) {
            Path file = uploadDir.resolve(productId + "." + ext);
            if (Files.isRegularFile(file)) {
                long version = file.toFile().lastModified();
                return "/uploads/products/" + productId + "." + ext + "?v=" + version;
            }
        }
        String bundledPhoto = "/images/products/" + productId + ".webp";
        if (new ClassPathResource("static" + bundledPhoto).exists()) {
            return bundledPhoto;
        }
        return PLACEHOLDER;
    }

    public String check(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            return null;
        }
        if (photo.getSize() > MAX_BYTES) {
            throw new ProductFormException("The photo can be at most 2 MB");
        }
        byte[] head = new byte[12];
        int read;
        try (InputStream in = photo.getInputStream()) {
            read = in.readNBytes(head, 0, head.length);
        } catch (IOException e) {
            throw new ProductFormException("The photo could not be read");
        }
        String ext = detect(head, read);
        if (ext == null) {
            throw new ProductFormException("The photo must be a JPG, PNG or WebP image");
        }
        return ext;
    }

    public void save(int productId, MultipartFile photo, String ext) throws IOException {
        Files.createDirectories(uploadDir);
        Path target = uploadDir.resolve(productId + "." + ext);
        Path temp = Files.createTempFile(uploadDir, "upload-", ".tmp");
        try (InputStream in = photo.getInputStream()) {
            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temp);
        }
        for (String other : EXTENSIONS) {
            if (!other.equals(ext)) {
                Files.deleteIfExists(uploadDir.resolve(productId + "." + other));
            }
        }
    }

    private static String detect(byte[] b, int n) {
        if (n >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (n >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "png";
        }
        if (n >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }
}
