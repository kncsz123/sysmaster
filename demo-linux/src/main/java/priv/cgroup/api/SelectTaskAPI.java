package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import priv.cgroup.service.SelectTask;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins="true")
public class SelectTaskAPI {
    @Autowired
    private SelectTask selectTaskService;

    @GetMapping("/api/user/selectTask")
    public Map<String, Object> selectUserTask(){
        Map<String, Object> response = new HashMap<>();
        response = selectTaskService.selectUserTask();
        return response;
    }
}
