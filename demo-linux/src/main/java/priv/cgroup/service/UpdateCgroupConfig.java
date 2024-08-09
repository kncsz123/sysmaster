package priv.cgroup.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.mapper.CgroupMapper;
import priv.cgroup.object.Cgroup;
import priv.cgroup.repository.CgroupRepository;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UpdateCgroupConfig {

    private static final Logger logger = Logger.getLogger(UpdateCgroupConfig.class.getName());

    @Autowired
    private final CgroupRepository cgroupRepository;

    public UpdateCgroupConfig(CgroupRepository cgroupRepository) {
        this.cgroupRepository = cgroupRepository;
    }

    @Transactional
    public Map<String, Object> updateCgroupConfig(Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        // 控制器
        String name = requestBody.get("name").toString();

        // 当前层级
        int hierarchy = Integer.parseInt(requestBody.get("hierarchy").toString());

        // cgroup配置对象
        Map<String, String> config = (Map<String, String>) requestBody.get("config");

        try {
            // 向数据库查询
            Cgroup selectedCgroup = cgroupRepository.selectCgroup(hierarchy, name);

            // cgroup路径
            String cgroupPath = selectedCgroup.getCgroupPath();

            // 配置文件路径
            String configPath = selectedCgroup.getCgroupConfigDir() + "/" + name + ".json";

            String key = config.get("key");
            String value = config.get("value");

            // 控制器路径
            String controllerPath = cgroupPath + "/" + key;

            // 使用 echo 命令写入值
            String command = String.format("echo %s > %s", value, controllerPath);
            Process updateProcess = new ProcessBuilder("bash", "-c", command).start();

            // 等待命令执行完毕
            int exitCode = updateProcess.waitFor();

            if (exitCode != 0) {
                response.put("status", 400);
                response.put("message", "Failed to update cgroup config for " + key);
                return response;
            }

            // 更新JSON配置文件
            File configFile = new File(configPath);
            Map<String, Object> configMap;

            if (configFile.exists()) {
                // 读取现有配置文件
                configMap = objectMapper.readValue(configFile, HashMap.class);
            } else {
                configMap = new HashMap<>();
            }

            // 更新settings
            Map<String, String> currentSettings = (Map<String, String>) configMap.getOrDefault("settings", new HashMap<String, String>());
            currentSettings.putAll(config);
            configMap.put("settings", currentSettings);

            // 写回配置文件
            objectMapper.writeValue(configFile, configMap);

            response.put("status", 200);
            response.put("message", "Updated cgroup config successfully.");
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "An error occurred: " + e.getMessage());
            logger.log(Level.SEVERE, "An error occurred", e);
        }

        return response;
    }
}
