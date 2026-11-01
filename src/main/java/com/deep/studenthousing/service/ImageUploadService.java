package com.deep.studenthousing.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ImageUploadService {

    private final Cloudinary cloudinary;

    public ImageUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file, Long ownerId, Long propertyId) throws IOException{
        String folderPath = String.format("student-housing/owner_%d/property_%d", ownerId, propertyId);

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", folderPath));

        return uploadResult.get("secure_url").toString();
    }

    public List<String> uploadMultipleImages(MultipartFile[] files, Long ownerId, Long propertyId) throws IOException{
        validate(files);
        List<String> urls = new ArrayList<>();
        for(MultipartFile file : files){
            if (file == null || file.isEmpty()) continue;
            urls.add(uploadImage(file, ownerId, propertyId));
        }
        return urls;
    }

    private static final int MAX_FILES_PER_UPLOAD = 10;
    private static final long MAX_FILE_BYTES = 10L * 1024 * 1024;
    private static final java.util.Set<String> ALLOWED_TYPES =
            java.util.Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    /**
     * Rejects anything that is not a real, reasonably sized image. The browser's
     * Content-Type header is only a claim, so the first bytes of the file
     * (its "magic number") are checked as well.
     *
     * @throws IllegalArgumentException with a message that is safe to show to the user
     */
    public void validate(MultipartFile[] files) throws IOException {
        if (files == null) return;
        int count = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            if (++count > MAX_FILES_PER_UPLOAD) {
                throw new IllegalArgumentException("You can upload at most " + MAX_FILES_PER_UPLOAD + " images at a time.");
            }
            String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
            if (!ALLOWED_TYPES.contains(type)) {
                throw new IllegalArgumentException("Only JPEG, PNG, WebP or GIF images are allowed.");
            }
            if (file.getSize() > MAX_FILE_BYTES) {
                throw new IllegalArgumentException("Each image must be smaller than 10MB.");
            }
            byte[] head = new byte[12];
            int read;
            try (java.io.InputStream in = file.getInputStream()) {
                read = in.readNBytes(head, 0, head.length);
            }
            if (!looksLikeImage(head, read)) {
                throw new IllegalArgumentException("One of the files is not a valid image.");
            }
        }
    }

    private static boolean looksLikeImage(byte[] h, int n) {
        if (n < 4) return false;
        boolean jpeg = (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF;
        boolean png = (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G';
        boolean gif = h[0] == 'G' && h[1] == 'I' && h[2] == 'F' && h[3] == '8';
        boolean webp = n >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
        return jpeg || png || gif || webp;
    }

    public void deleteImage(String imageUrl) {
        try {
            // Extract public_id from Cloudinary URL
            String publicId = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));
            cloudinary.uploader().destroy("student-housing/" + publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
