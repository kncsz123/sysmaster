package priv.cgroup.service.impl;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class UpdateCgroupConfigServiceImpl implements UpdateCgroupConfigService {

    @Override
    public Map<String, Object> updateCgroupConfig(String value) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 解析前端传过来的键值对
            Map.Entry<String, String> entry = parseKeyValue(value);
            String key = entry.getKey();
            String configValue = entry.getValue();

            // 获取当前所在的 cgroup 目录
            String currentCgroupPath = getCurrentWorkingDirectory();

            // 拼接路径以定位到具体的 cgroup 控制器文件
            String cgroupFilePath = currentCgroupPath + "/" + key;

            // 使用 echo 命令写入值
            String command = String.format("echo %s > %s", configValue, cgroupFilePath);
            Process updateProcess = new ProcessBuilder("bash", "-c", command).start();

            // 等待命令执行完毕
            int exitCode = updateProcess.waitFor();

            if (exitCode == 0) {
                response.put("status", 200);
                response.put("message", "Updated cgroup config successfully.");
            } else {
                response.put("status", 400);
                response.put("message", "Failed to update cgroup config.");
            }
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "An error occurred: " + e.getMessage());
        }

        return response;
    }

    private String getCurrentWorkingDirectory() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("pwd");
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to get current working directory");
        }

        return output.toString().trim();
    }

    private Map.Entry<String, String> parseKeyValue(String keyValue) {
        String[] parts = keyValue.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid key-value format. Expected format: 'key:value'");
        }
        return new HashMap.SimpleEntry<>(parts[0], parts[1]);
    }
}
