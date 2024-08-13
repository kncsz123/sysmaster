package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import priv.cgroup.service.DeleteTask;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins="true")
public class DeleteTaskAPI {
    @Autowired
    private DeleteTask deleteTaskService;

//    @RequestParam("pid") String pid
    @DeleteMapping("/api/user/deleteTask")
    public Map<String, Object> deleteTask(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        response = deleteTaskService.deleteTask(requestBody);
        return response;
    }
}
