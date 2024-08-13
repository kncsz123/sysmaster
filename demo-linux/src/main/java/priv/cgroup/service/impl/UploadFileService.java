package priv.cgroup.service.impl;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UploadFileService {
    public Map<String, Object> uploadFile(MultipartFile file);
}
