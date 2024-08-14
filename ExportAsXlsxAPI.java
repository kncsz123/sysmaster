package priv.cgroup.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import priv.cgroup.service.ExportAsXlsx;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "true")
public class ExportAsXlsxAPI {

    @Autowired
    private ExportAsXlsx exportAsXlsxService;

    @PostMapping("/api/user/exportAsXlsx")
    public Map<String, Object> exportToXlsx(@RequestBody List<Map<String, Object>> data,
                                            @RequestParam("filename") String filename,
                                            HttpServletResponse response)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        exportAsXlsxService.exportToXlsx(data, response, filename);
        return result;
    }
}
