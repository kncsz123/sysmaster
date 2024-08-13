package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import priv.cgroup.service.CreateTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "true")
public class CreateTaskAPI {
    @Autowired
    public CreateTask createTaskService;

//    @RequestParam ("command") List<String> commands

    @PostMapping("/api/user/createTask")
    public Map<String, Object> createTask(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<String, Object>();
        response = createTaskService.createTask(requestBody) ;
        return response;
    }
}
