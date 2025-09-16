package com.deep.studenthousing.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    // delete a single file by public_id
    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    // delete a whole folder
    public void deleteFolder(String folderPath) throws Exception {
        cloudinary.api().deleteResourcesByPrefix(folderPath, ObjectUtils.emptyMap());
        cloudinary.api().deleteFolder(folderPath, ObjectUtils.emptyMap());
    }

}
