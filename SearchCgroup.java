package priv.cgroup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.repository.CgroupRepository;
import priv.cgroup.object.Cgroup;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchCgroup {

    @Autowired
    private CgroupRepository cgroupRepository;

    public Map<String, Object> search(String name, String startTime, String endTime) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 转换日期格式
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日");
            DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd E HH:mm:ss");

            List<Cgroup> cgroups;

            // 如果只有 name
            if (name != null && !name.isEmpty() && (startTime == null || startTime.isEmpty()) && (endTime == null || endTime.isEmpty())) {
                cgroups = cgroupRepository.findByName(name);

                // 如果只有 startTime 和 endTime
            } else if ((name == null || name.isEmpty()) && startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
                LocalDateTime startDate = LocalDateTime.parse(startTime, inputFormatter);
                LocalDateTime endDate = LocalDateTime.parse(endTime, inputFormatter);

                cgroups = cgroupRepository.findByDateRange(startDate.format(dbFormatter), endDate.format(dbFormatter));

                // 如果三个参数都有
            } else if (name != null && !name.isEmpty() && startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty()) {
                LocalDateTime startDate = LocalDateTime.parse(startTime, inputFormatter);
                LocalDateTime endDate = LocalDateTime.parse(endTime, inputFormatter);

                cgroups = cgroupRepository.findByNameAndDateRange(name, startDate.format(dbFormatter), endDate.format(dbFormatter));

                // 如果都为空或没有匹配条件
            } else {
                response.put("status", 400);
                response.put("message", "Invalid parameters");
                response.put("cgroups", null);
                return response;
            }

            response.put("status", 200);
            response.put("message", "success");
            response.put("cgroups", cgroups);

        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", e.getMessage());
            response.put("cgroups", null);
        }

        return response;
    }
}
