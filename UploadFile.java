package priv.cgroup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import priv.cgroup.object.File;
import priv.cgroup.repository.FileRepository;

import java.io.IOException;
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

    private static final String USER_FILEDIR_PATH = "/home/kncsz/SysMaster/user/file"; // kncsz为用户名占位符

    public UploadFile(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    /*
        @param file 文件
     */
    public Map<String, Object> uploadFile(MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        String fileName = file.getOriginalFilename();
        java.io.File fileForCheck = new java.io.File(USER_FILEDIR_PATH + fileName);

        // 判断文件与否
        if(!file.getResource().isFile()){
            response.put("status", 400);
            response.put("message", "Please select a file.");
            return response;
        }

        // 判断文件大小,超过50M阻止上传
        if (file.getSize() > 50 * 1024 * 1024) {
            response.put("status", 400);
            response.put("message", "File is too large.");
        }

        // 文件名无效
        if (fileName == null || fileName.isEmpty()) {
            response.put("status", 400);
            response.put("message", "File name is invalid.");
            return response;
        }

        // 判断文件是否为空
        if(file.isEmpty()){
            response.put("status", 400);
            response.put("message", "File can not be empty.");
            return response;
        }

        // 判断是否上传重复文件
        if(fileForCheck.exists()){
            response.put("status", 400);
            response.put("message", "File already exists.");
            return response;
        }

        try {

            // 指向上传到服务器上的文件目录
            Path filePath = Paths.get(USER_FILEDIR_PATH, fileName);

            // 创建文件夹路径，如果不存在则创建
            Files.createDirectories(filePath.getParent());

            // 将上传的文件保存到目标路径
            Files.copy(file.getInputStream(), filePath);

            // 如果不是可执行文件,则回退上传
            if(!fileForCheck.canExecute()){
                Files.delete(filePath);
                response.put("status", 400);
                response.put("message", "File is not executable.");
                return response;
            }

            // TODO
            // 判断文件是否存在病毒
//            if(!isSafe(fileForCheck)){
//                Files.delete(filePath);
//                response.put("status", 400);
//                response.put("message", "File is not safe");
//                return response;
//            }

            // 创建时间戳
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd E HH:mm:ss");
            String timestamp = now.format(formatter);

            // 获取文件大小
            long size = fileForCheck.length();

            // 将文件信息写入数据库
            File insertedFile = new File(fileName, USER_FILEDIR_PATH,  timestamp, timestamp, size);
            fileRepository.save(insertedFile);
            System.out.println("File uploaded successfully at: " + timestamp);

            // 成功结果
            response.put("status", 200);
            response.put("message", "File uploaded successfully.");
        } catch (IOException e) {
            // 失败结果
            response.put("status", 500);
            response.put("message", "Failed to upload file: " + e.getMessage());
            return response;
        }

        return response;
    }

//    private boolean isSafe(java.io.File fileForCheck) {
//        //TODO
//        /*
//            调用防病毒软件接口对文件进行安全性检查
//         */
//    }
}
