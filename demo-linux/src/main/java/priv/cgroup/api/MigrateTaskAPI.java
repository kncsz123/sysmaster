package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import priv.cgroup.service.MigrateTask;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins="true")
public class MigrateTaskAPI {
    @Autowired
    private MigrateTask migrateTaskService;

//    @RequestParam("taskPID") String pid,
//    @RequestParam("targetCgroup") String target,
//    @RequestParam("hierarchy")int hierarchy

    @PutMapping("/api/user/migrateTask")
    public Map<String, Object> migrateTask(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<String, Object>();
        response = migrateTaskService.migrateTask(requestBody);
        return response;
    }
}