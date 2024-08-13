package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import priv.cgroup.service.ViewTaskResourceUsage;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins="true")
public class ViewTaskResourceUsageAPI {
    @Autowired
    private ViewTaskResourceUsage viewTaskResourceUsageService;

    @GetMapping("/api/user/viewTaskResourceUsage")
    public Map<String, Object> viewTaskResourceUsage(@RequestParam ("pid") String pid){
        Map<String, Object> response = new HashMap<String, Object>();
        response = viewTaskResourceUsageService.viewTaskResourceUsage(pid);
        return response;
    }
}
