package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import priv.cgroup.service.CreateDir;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins="true")
public class CreateDirAPI {
    @Autowired
    private CreateDir createDirService;
    @PostMapping("/api/user/createDirectory")
    public Map<String, Object> createDirectory(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<String, Object>();
        response = createDirService.createDirectory(requestBody);
        return response;
    }
}
