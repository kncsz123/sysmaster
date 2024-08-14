package priv.cgroup.service;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.object.File;
import priv.cgroup.repository.FileRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SelectFile {
    @Autowired
    private final FileRepository fileRepository;

    public SelectFile(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    /*
        @RequestParam("path") String Path
     */
    public Map<String, Object> selectFile(String path) {
        Map<String, Object> response = new HashMap<>();
        try{
            // 通过文件或目录的父目录路径进行查询
            String concatPath = "/home/kncsz/SysMaster/file/user" + path;
            List<File> selectedFile = fileRepository.findByPath(concatPath);
            if(!StrUtil.isEmptyIfStr(selectedFile)){
                response.put("status", 200);
                response.put("message", "success");
                response.put("file", selectedFile);
            }else{
                response.put("status", 400);
                response.put("message", "error");
                response.put("file", null);
            }
        }catch(Exception e){
            response.put("status", 500);
            response.put("message", e.getMessage());
            response.put("file", null);
            return response;
        }
        return response;
    }
}
