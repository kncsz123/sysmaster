package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import priv.cgroup.service.DeleteCgroup;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "true")//允许跨域
public class DeleteCgroupAPI {
    @Autowired
    private DeleteCgroup deleteCgroupService;

//    @RequestParam("name") String name,
//    @RequestParam("hierarchy") int hierarchy
    @DeleteMapping("/api/user/deleteCgroup")
    public Map<String, Object> deleteCgroup(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<String, Object>();
        response = deleteCgroupService.deleteCgroup(requestBody);
        return response;
    }
}
