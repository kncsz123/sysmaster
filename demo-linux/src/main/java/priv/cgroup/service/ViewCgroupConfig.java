package priv.cgroup.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.mapper.CgroupMapper;
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

    public Map<String, Object> viewCgroupConfig(String name, String controllerID, int hierarchy) {
        Map<String, Object> response = new HashMap<>();
        String controllerPrefix;
        try {
            // 向数据库查询控制器权限、cgroup路径以及配置文件路径
            Cgroup selectedCgroup = new Cgroup();

            try {
                selectedCgroup = cgroupRepository.selectCgroup(hierarchy, name);
            } catch (Exception e) {
                response.put("status", 500);
                response.put("message", e.getMessage());
                e.printStackTrace();
                return response;
            }

            // 当前cgroup的路径
            String currentPath = selectedCgroup.getCgroupPath();

            // 查询控制器状态
            boolean cpu_stat = selectedCgroup.isCpu_controller_status();
            boolean memory_stat = selectedCgroup.isMemory_controller_status();
            boolean pids_stat = selectedCgroup.isPids_controller_status();
            boolean cpuset_stat = selectedCgroup.isCpuset_controller_status();
            boolean io_stat = selectedCgroup.isIo_controller_status();

            System.out.println(cpu_stat);
            System.out.println(memory_stat);
            System.out.println(pids_stat);
            System.out.println(cpuset_stat);
            System.out.println(io_stat);
            String controllerPath = currentPath;

            // 判断控制器是否开启，根据前端提供的controllerID（01~05），字符串01CPU，02Memory，03PIDS，04CPUSET，05IO，判断要查询哪个控制器。
            if (controllerID.equals("01") && !cpu_stat) {
                controllerPrefix = "cpu";
                response = getControllerConfigValues(controllerPath, controllerPrefix);
            } else if (controllerID.equals("02") && !memory_stat) {
                controllerPrefix = "memory";
                response = getControllerConfigValues(controllerPath, controllerPrefix);
            } else if (controllerID.equals("03") && !pids_stat) {
                controllerPrefix = "pids";
                response = getControllerConfigValues(controllerPath, controllerPrefix);
            } else if (controllerID.equals("04") && !cpuset_stat) {
                controllerPrefix = "cpuset";
                response = getControllerConfigValues(controllerPath, controllerPrefix);
            } else if (controllerID.equals("05") && !io_stat) {
                controllerPrefix = "io";
                response = getControllerConfigValues(controllerPath, controllerPrefix);
            }
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", e.getMessage());
            return response;
        }

        return response;
    }

    private Map<String, Object> getControllerConfigValues(String controllerPath, String controllerPrefix) throws Exception {
        Map<String, Object> configValues = new HashMap<>();

        // 构建find命令来查找文件
        String findCommand = String.format("find %s -maxdepth 1 -type f -name '%s.*'", controllerPath, controllerPrefix);

        // 执行find命令
        ProcessBuilder findProcessBuilder = new ProcessBuilder("/bin/sh", "-c", findCommand);
        Process findProcess = findProcessBuilder.start();

        BufferedReader findReader = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));
        String filePath;

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
            throw new RuntimeException("Failed to get controller config values");
        }

        configValues.put("status", 200);
        configValues.put("message", "Successfully retrieved controller config values");

        findReader.close();

        return configValues;
    }
}
