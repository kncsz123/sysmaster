package priv.cgroup.service;

import cn.hutool.core.util.StrUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.mapper.FileMapper;
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
        此方法无请求参数
     */
    public Map<String, Object> selectFile() {
        Map<String, Object> response = new HashMap<>();
        try{
            List<File> selectedFile = fileRepository.findAll();
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
