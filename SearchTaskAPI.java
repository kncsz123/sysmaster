package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import priv.cgroup.service.SearchTask;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "true")//允许跨域
public class SearchTaskAPI {
    @Autowired
    private SearchTask searchTaskService;

    @PostMapping("/api/user/searchTask")
    public Map<String, Object> searchTask(@RequestParam("name") String name,
                                          @RequestParam("startTime") String startTime,
                                          @RequestParam("endTime") String endTime) {
        Map<String, Object> response = new HashMap<String, Object>();
        response = searchTaskService.search(name, startTime, endTime);
        return response;
    }
}
