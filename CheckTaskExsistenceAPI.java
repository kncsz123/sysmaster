package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import priv.cgroup.service.CheckTaskExsistence;

import java.util.HashMap;
import java.util.Map;

@RestController//解析为json而不是视图
@CrossOrigin(origins = "true")//允许跨域
public class CheckTaskExsistenceAPI {
    @Autowired
    private CheckTaskExsistence checkTaskExsistenceService;

    @GetMapping("/api/user/checkTaskExsistence")
    public void checkTaskExsistence() {
        checkTaskExsistenceService.checkTaskExsistence();
    }
}
