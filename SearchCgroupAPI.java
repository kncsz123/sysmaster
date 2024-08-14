package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import priv.cgroup.service.SearchCgroup;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "true")//允许跨域
public class SearchCgroupAPI {
    @Autowired
    private SearchCgroup searchCgroupService;

    @PostMapping("/api/user/Cgroup")
    public Map<String, Object> searchCgroup(@RequestParam("name") String name,
                                            @RequestParam("startTime") String startTime,
                                            @RequestParam("endTime") String endTime) {
        Map<String, Object> response = new HashMap<String, Object>();
        response = searchCgroupService.search(name, startTime, endTime);
        return response;
    }
}
