package priv.cgroup.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import priv.cgroup.mapper.FileMapper;
import priv.cgroup.object.File;
import priv.cgroup.repository.FileRepository;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class UploadFile {

    @Autowired
    private final FileRepository fileRepository;

    private static final String USER_FILEDIR_PATH = "/home/kncsz/SysMaster/user/file"; // 用 kncsz 测试，SysMaster 改为小写

    public UploadFile(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    /*
        @param file 文件
     */
    public Map<String, Object> uploadFile(MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        String fileName = file.getOriginalFilename();

        if (fileName == null || fileName.isEmpty()) {
            response.put("status", 400);
            response.put("message", "File name is invalid.");
            return response;
        }

        try {
            Path filePath = Paths.get(USER_FILEDIR_PATH, fileName);

            // 创建文件夹路径，如果不存在则创建
            Files.createDirectories(filePath.getParent());

            // 将上传的文件保存到目标路径
            Files.copy(file.getInputStream(), filePath);

            // 创建时间戳
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd E HH:mm:ss");
            String timestamp = now.format(formatter);

            // 将文件信息写入数据库
            File insertedFile = new File(fileName, USER_FILEDIR_PATH,  timestamp, timestamp);
            fileRepository.save(insertedFile);
            System.out.println("File uploaded successfully at: " + timestamp);

            // 成功结果
            response.put("status", 200);
            response.put("message", "File uploaded successfully.");
        } catch (IOException e) {
            // 失败结果
            e.printStackTrace();
            response.put("status", 400);
            response.put("message", "Failed to upload file: " + e.getMessage());
            return response;
        }
//        } catch (FileAlreadyExistsException e) {
//
//        }

        return response;
    }
}
