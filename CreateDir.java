package priv.cgroup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.repository.FileRepository;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CreateDir {
    @Autowired
    private  final FileRepository fileRepository;

    public CreateDir(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public Map<String, Object> createDirectory(Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();

        try{
            String path = requestBody.get("path").toString();
            String name = requestBody.get("name").toString();

            String concatPath = "/home/kncsz/SysMaster/file/user" + path;
            File newFile = new File(concatPath);

            // 检查是否存在同名的目录
            List<priv.cgroup.object.File> existingDirectories = fileRepository.findByPath(concatPath);
            boolean isNameConflict = existingDirectories.stream()
                    .anyMatch(file -> file.getName().equals(name) && file.getType().equals("directory"));

            if (isNameConflict) {
                response.put("status", 409);
                response.put("message", "A directory with the same name already exists in the same path.");
                return response;
            }

            if(!newFile.exists()){
                if(newFile.mkdirs()){
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd E HH:mm:ss");
                    String datestamp = now.format(formatter);
                    priv.cgroup.object.File insertedFile = new priv.cgroup.object.File(name, concatPath, datestamp, datestamp, 0, "directory");
                    fileRepository.save(insertedFile);
                    response.put("status", 200);
                    response.put("message","success");
                }
            }
        }catch(Exception e){
            response.put("status", 200);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
