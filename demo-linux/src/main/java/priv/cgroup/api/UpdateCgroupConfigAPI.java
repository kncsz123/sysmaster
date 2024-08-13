package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import priv.cgroup.service.UpdateCgroupConfig;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins="true")
public class UpdateCgroupConfigAPI {
    @Autowired
    private UpdateCgroupConfig updateCgroupConfigService;

//    @RequestParam("name") String name,
//    @RequestParam("value") String value,
//    @RequestParam("hierarchy") int hierarchy

    @PutMapping("/api/user/updateCgroupConfig")
    public Map<String, Object> updateCgroupConfig(@RequestBody Map<String, Object> requestBody){
        Map<String, Object> response = new HashMap<String, Object>();
        response = updateCgroupConfigService.updateCgroupConfig(requestBody);
        return response;
    }
}
