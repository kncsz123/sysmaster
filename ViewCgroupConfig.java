package priv.cgroup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.object.Cgroup;
import priv.cgroup.repository.CgroupRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class ViewCgroupConfig {

    @Autowired
    private final CgroupRepository cgroupRepository;

    public ViewCgroupConfig(CgroupRepository cgroupRepository) {
        this.cgroupRepository = cgroupRepository;
    }

    public Map<String, Object> viewCgroupConfig(String name, String controllerID, int hierarchy, int filter) {
        Map<String, Object> response = new HashMap<>();
        String controllerPrefix;
        try {
            // 向数据库查询控制器权限、cgroup路径以及配置文件路径
            Cgroup selectedCgroup;

            try {
                selectedCgroup = cgroupRepository.selectCgroup(hierarchy, name);
            } catch (Exception e) {
                response.put("status", 500);
                response.put("message", e.getMessage());
                return response;
            }

            // 当前cgroup的路径
            String currentPath = selectedCgroup.getCgroupPath();

            // 查询控制器状态
            boolean cpu_stat = selectedCgroup.isCpu_controller_status();
            boolean cpuset_stat = selectedCgroup.isCpuset_controller_status();
            boolean memory_stat = selectedCgroup.isMemory_controller_status();
            boolean io_stat = selectedCgroup.isIo_controller_status();
            boolean pids_stat = selectedCgroup.isPids_controller_status();

            String controllerPath = currentPath;

            // 0表示不需要过滤read-only文件，1表示过滤read-only文件
            // 判断控制器是否开启，根据前端提供的controllerID（01~05），字符串01CPU，02Memory，03PIDS，04CPUSET，05IO，判断要查询哪个控制器。
            if (controllerID.equals("01") && cpu_stat) {
                controllerPrefix = "cpu";
                response = getControllerConfigValues(controllerPath, controllerPrefix,filter);
            } else if (controllerID.equals("02") && memory_stat) {
                controllerPrefix = "memory";
                response = getControllerConfigValues(controllerPath, controllerPrefix,filter);
            } else if (controllerID.equals("03") && pids_stat) {
                controllerPrefix = "pids";
                response = getControllerConfigValues(controllerPath, controllerPrefix,filter);
            } else if (controllerID.equals("04") && cpuset_stat) {
                controllerPrefix = "cpuset";
                response = getControllerConfigValues(controllerPath, controllerPrefix,filter);
            } else if (controllerID.equals("05") && io_stat) {
                controllerPrefix = "io";
                response = getControllerConfigValues(controllerPath, controllerPrefix,filter);
            }
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", e.getMessage());
            return response;
        }

        return response;
    }

    private Map<String, Object> getControllerConfigValues(String controllerPath, String controllerPrefix, int filter) throws Exception {
        Map<String, Object> configValues = new HashMap<>();

        // 构建find命令来查找文件
        String findCommand = String.format("find %s -maxdepth 1 -type f -name '%s.*'", controllerPath, controllerPrefix);

        // 执行find命令
        ProcessBuilder findProcessBuilder = new ProcessBuilder("/bin/sh", "-c", findCommand);
        Process findProcess = findProcessBuilder.start();

        BufferedReader findReader = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));
        String filePath;

        if(filter == 1){
            while ((filePath = findReader.readLine()) != null) {
                // 使用ls -l命令获取文件的权限
                String lsCommand = String.format("ls -l %s", filePath);
                ProcessBuilder lsProcessBuilder = new ProcessBuilder("/bin/sh", "-c", lsCommand);
                Process lsProcess = lsProcessBuilder.start();

                BufferedReader lsReader = new BufferedReader(new InputStreamReader(lsProcess.getInputStream()));
                String lsOutput = lsReader.readLine(); // 只需要第一行的输出

                // 检查权限字符串（第二到第四个字符是用户的权限部分）
                if (lsOutput != null) {
                    String permissions = lsOutput.substring(1, 4); // 获取权限部分，例如 "r-x"
                    if (!permissions.contains("w")) { // 如果没有写权限
                        continue; // 跳过这个文件，不添加到 configValues 中
                    }
                }

                // 执行cat命令读取文件内容
                String command = String.format("cat %s", filePath);
                ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", command);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder fileContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.append(line).append(System.lineSeparator());
                }
                reader.close();

                String fileName = filePath.substring(filePath.lastIndexOf('/') + 1); // 保留前缀
                configValues.put(fileName, fileContent.toString().trim());
            }

            int exitCode = findProcess.waitFor();
            if (exitCode != 0) {
                configValues.put("status", 400);
                configValues.put("message", "Error finding controller");
                return configValues;
            }

            configValues.put("status", 200);
            configValues.put("message", "Successfully retrieved controller config values");

            findReader.close();
        }else{
            while ((filePath = findReader.readLine()) != null) {
                // 执行cat命令读取文件内容
                String command = String.format("cat %s", filePath);
                ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", command);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder fileContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.append(line).append(System.lineSeparator());
                }
                reader.close();

                String fileName = filePath.substring(filePath.lastIndexOf('/') + 1); // 保留前缀
                configValues.put(fileName, fileContent.toString().trim());
            }

            int exitCode = findProcess.waitFor();
            if (exitCode != 0) {
                configValues.put("status", 400);
                configValues.put("message", "Error finding controller");
                return configValues;
            }

            configValues.put("status", 200);
            configValues.put("message", "Successfully retrieved controller config values");

            findReader.close();

            return configValues;
        }
        return configValues;
    }
}
