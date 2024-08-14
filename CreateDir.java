package priv.cgroup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.repository.FileRepository;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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

            String concatPath = "/home/kncsz/SysMaster/file/user" + path + "/" + name;
            File file = new File(concatPath);

            if(!file.exists()){
                if(file.mkdirs()){
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
