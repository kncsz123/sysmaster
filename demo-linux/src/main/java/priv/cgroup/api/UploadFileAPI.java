package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import priv.cgroup.service.UploadFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins="true")
public class UploadFileAPI {
    @Autowired
    private UploadFile uploadFileService;

    @PostMapping("/api/user/uploadFile")
    public Map<String, Object> uploadFile(@RequestBody MultipartFile file) {
        Map<String, Object> response = new HashMap<String, Object>();
        response = uploadFileService.uploadFile(file);
        return response;
    }
}
