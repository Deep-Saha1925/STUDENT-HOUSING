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
        System.out.println(propertyId);
        String folderPath = String.format("student-housing/owner_%d/property_%d", ownerId, propertyId);

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", folderPath));

        return uploadResult.get("secure_url").toString();
    }

    public List<String> uploadMultipleImages(MultipartFile[] files, Long ownerId, Long propertyId) throws IOException{
        List<String> urls = new ArrayList<>();
        for(MultipartFile file : files){
            urls.add(uploadImage(file, ownerId, propertyId));
        }
        return urls;
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
