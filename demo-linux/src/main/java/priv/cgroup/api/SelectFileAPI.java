package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import priv.cgroup.service.SelectFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins="true")
public class SelectFileAPI {
    @Autowired
    private SelectFile selectFileService;

    @GetMapping("/api/user/selectFile")
    public Map<String, Object> selectFile() {
        Map<String, Object> response = new HashMap<>();
        response = selectFileService.selectFile();
        return response;
    }
}
