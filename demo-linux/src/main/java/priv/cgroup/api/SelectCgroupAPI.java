package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import priv.cgroup.service.SelectCgroup;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "true")
public class SelectCgroupAPI {
    @Autowired
    private SelectCgroup selectCgroupService;

    @GetMapping("/api/user/selectCgroup")
    public Map<String, Object> selectCgroup(){
        Map<String, Object> response = new HashMap<>();
        response = selectCgroupService.selectCgroup();
        return response;
    }

}
