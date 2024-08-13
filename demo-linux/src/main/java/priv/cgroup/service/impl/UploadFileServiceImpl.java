package priv.cgroup.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class UploadFileServiceImpl implements UploadFileService {
    private static final String USER_FILEDIR_PATH = "/home/kncsz/sysmaster/user/file"; // 用 kncsz 测试，SysMaster 改为小写

    @Override
    public Map<String, Object> uploadFile(MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isEmpty()) {
            response.put("status", "error");
            response.put("message", "File name is invalid.");
            return response;
        }

        try {
            Path filePath = Paths.get(USER_FILEDIR_PATH, fileName);

            // 创建文件夹路径，如果不存在则创建
            Files.createDirectories(filePath.getParent());

            // 将上传的文件保存到目标路径
            Files.copy(file.getInputStream(), filePath);

            response.put("status", "success");
            response.put("message", "File uploaded successfully.");
        } catch (IOException e) {
            response.put("status", "error");
            response.put("message", "Failed to upload file: " + e.getMessage());
        }

        return response;
    }
}
