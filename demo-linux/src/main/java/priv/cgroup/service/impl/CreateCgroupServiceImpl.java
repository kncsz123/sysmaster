package priv.cgroup.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.Process;
import java.util.HashMap;
import java.util.Map;

@Service
public class CreateCgroupServiceImpl implements CreateCgroupService {

    @Override
    public Map<String, Object> createCgroup(String name) {
        Map<String, Object> response = new HashMap<>();

        //获取用户创建的cgroup名称
        String cgroupName = name;

        //获取当前所在的cgroup目录
        String currentCgroupPath = "";

        try {
            // 读取当前进程所在的cgroup
            Process pwdProcess = new ProcessBuilder("bash", "-c", "pwd").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(pwdProcess.getInputStream()));
            currentCgroupPath = reader.readLine();
            reader.close();
            pwdProcess.waitFor();

            // 拼接路径以创建cgroup
            String cgroupPath = currentCgroupPath + "/" + cgroupName;

            // 检查当前cgroup是否存在
            Process checkProcess = new ProcessBuilder("test", "-d", cgroupPath).start();
            int checkResult = checkProcess.waitFor();

            if (checkResult == 0) {
                // 如果存在，函数返回，阻止用户创建
                response.put("status", 400);
                response.put("message", "Cgroup " + cgroupName + " already exists.");
                response.put("cgroupName", null);
                return response;
            }

            // 创建cgroup
            Process createProcess = new ProcessBuilder("mkdir", "-p", cgroupPath).start();
            createProcess.waitFor();

            // 检查创建状态
            Process verifyProcess = new ProcessBuilder("test", "-d", cgroupPath).start();
            int exitcode = verifyProcess.waitFor();

            // 返回结果给前端
            if (exitcode == 0) {
                response.put("status", 200);
                response.put("message", "Cgroup " + cgroupName + " created successfully.");
                response.put("cgroupName", cgroupName);

                // 创建配置文件
                createConfigFile(cgroupName);
            } else {
                response.put("status", 400);
                response.put("message", "Failed to create cgroup " + cgroupName);
                response.put("cgroupName", null);
            }
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "An error occurred: " + e.getMessage());
            response.put("cgroupName", null);
        }

        return response;
    }

    //创建配置文件函数
    private void createConfigFile(String cgroupName) throws Exception {
        String configFilePath = "/home/kncsz/SysMaster/conf/userconf/" + cgroupName + ".json";
        File configFile = new File(configFilePath);

        // 如果父目录不存在，则创建目录
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }

        // 创建配置内容
        Map<String, Object> configContent = new HashMap<>();
        configContent.put("cgroupName", cgroupName);
        configContent.put("settings", new HashMap<String, Object>());

        // 使用 Jackson 库将配置内容写入 JSON 文件
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, configContent);
    }
}
