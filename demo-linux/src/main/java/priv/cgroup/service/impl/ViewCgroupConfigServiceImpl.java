package priv.cgroup.service.impl;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class ViewCgroupConfigServiceImpl implements ViewCgroupConfigService {

    @Override
    public Map<String, Object> viewCgroupConfig(String name, String controllerID) {
        Map<String, Object> response = new HashMap<>();

        //向数据库查询控制器权限

        try {
            //获取当前的工作路径
            String currentPath = getCurrentWorkingDirectory();

            //获取查询路径
            String controllerPath = "";

            //根据前端提供的controllerID（01~05），字符串01CPU，02Memory，03PIDS，04CPUSET，05IO，判断要查询哪个控制器。
            //判断该控制器是否有权限
            //查找对应控制器下的所有文件及其对应的值，返回结果
            if (controllerID == "01") {
                controllerPath = currentPath + "/" + "cpu";
                response = getControllerConfigValues(controllerPath);
            }else if (controllerID == "02") {
                controllerPath = currentPath + "/" + "memory";
                response = getControllerConfigValues(controllerPath);
            }else if(controllerID == "03") {
                controllerPath = currentPath + "/" + "pids";
                response = getControllerConfigValues(controllerPath);
            }else if(controllerID == "04") {
                controllerPath = currentPath + "/" + "cpuset";
                response = getControllerConfigValues(controllerPath);
            }else {
                controllerPath = currentPath + "/" + "io";
                response = getControllerConfigValues(controllerPath);
            }

            //返回结果给前端，只返回与控制器有关的文件和值
            return response;
        }catch (Exception e) {
            response.put("status", 400);
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

    private Map<String, Object> getControllerConfigValues(String controllerPath) throws Exception {
        Map<String, Object> configValues = new HashMap<>();
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c",
                "find " + controllerPath + " -type f -exec cat {} + -exec echo \"=== End of {} ===\" \\;");
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        String currentFile = null;
        StringBuilder fileContent = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("=== End of ")) {
                if (currentFile != null) {
                    configValues.put(currentFile, fileContent.toString().trim());
                }
                currentFile = line.substring(12, line.length() - 4).trim(); // 获取文件路径
                fileContent.setLength(0); // 重置内容
            } else {
                fileContent.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to get controller config values");
        }

        return configValues;
    }
}
