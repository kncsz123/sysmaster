package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import priv.cgroup.service.ChangeTaskStatus;

import java.util.HashMap;
import java.util.Map;

@RestController//解析为json而不是视图
@CrossOrigin(origins = "true")//允许跨域
public class ChangeTaskStatusAPI {
    @Autowired
    private ChangeTaskStatus changeTaskStatusService;

    @PutMapping("/api/user/changeTaskStatus")
    public Map<String, Object> changeTaskStatus(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<String, Object>();
        response = changeTaskStatusService.changeTaskStatus(requestBody);
        return response;
    }
}
