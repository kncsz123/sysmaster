package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import priv.cgroup.service.CreateCgroup;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "true")//允许跨域
public class CreateCgroupAPI {

    @Autowired
    private CreateCgroup createCgroupService;

//    @RequestParam(value = "name") String name,
//    @RequestParam(value = "parentName") String parentName,
//    @RequestParam(value = "parentHierarchy") int parentHierarchy
    @PostMapping("/api/user/createCgroup")
    public Map<String, Object> createCgroup(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<String, Object>();
        response = createCgroupService.createCgroup(requestBody);
        return response;
    }
}

