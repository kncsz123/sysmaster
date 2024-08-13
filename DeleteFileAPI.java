package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import priv.cgroup.service.DeleteFile;
import priv.cgroup.service.DeleteTask;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins="true")
public class DeleteFileAPI {
    @Autowired
    private DeleteFile deleteFileService;

    //    @RequestParam("pid") String pid
    @DeleteMapping("/api/user/deleteFile")
    public Map<String, Object> deleteFile(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        response = deleteFileService.deleteFile(requestBody);
        return response;
    }
}
