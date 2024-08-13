package priv.cgroup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.repository.FileRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeleteFile {
    @Autowired
    private final FileRepository fileRepository;


    public DeleteFile(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    //异常
    public Map<String, Object> deleteFile(Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<String, Object>();

        try{
            String path = requestBody.get("path").toString();

            // 删除文件
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("rm", "-rf", path); // 注意安全性，rm -rf 命令会递归删除文件夹及其内容，慎用！
            Process process = builder.start();
            int exitCode = process.waitFor();

            // 删除成功则删除数据库中对应条目
            if (exitCode != 0) {
                fileRepository.deleteByPath(path);
                response.put("status", 200);
                response.put("message", "File deleted successfully");
            }else{
                response.put("status", 400);
                response.put("message", "error occured while deleting file");
            }
        }catch(IOException | InterruptedException e){
            response.put("status", 500);
            response.put("message", "Bad Request");
        }
        
        return response;
    }
}
