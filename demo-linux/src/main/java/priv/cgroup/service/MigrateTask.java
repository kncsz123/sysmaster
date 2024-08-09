package priv.cgroup.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.mapper.CgroupMapper;
import priv.cgroup.mapper.TaskMapper;
import priv.cgroup.object.Cgroup;
import priv.cgroup.object.Task;
import priv.cgroup.repository.CgroupRepository;
import priv.cgroup.repository.TaskRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class MigrateTask {
    @Autowired
    private final TaskRepository taskRepository;

    @Autowired
    private final CgroupRepository cgroupRepository;

    public MigrateTask(TaskRepository taskRepository, CgroupRepository cgroupRepository) {
        this.taskRepository = taskRepository;
        this.cgroupRepository = cgroupRepository;
    }

    /*
       @param pid 进程pid
       @param target 加入的cgroup的名称
       @param hierarchy 加入的cgroup所在层级
     */
    @Transactional
    public Map<String, Object> migrateTask(Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();

        String pid = (requestBody.get("pid") != null) ? requestBody.get("pid").toString() : null;
        String target = (requestBody.get("target") != null) ? requestBody.get("target").toString() : null;
        Integer hierarchy = (requestBody.get("hierarchy") != null) ? Integer.parseInt(requestBody.get("hierarchy").toString()) : null;

        if (pid == null || target == null || hierarchy == null) {
            response.put("status", 400);
            response.put("message", "Missing required parameters.");
            return response;
        }

        try {
            // 检查进程是否存在
            if (!checkProcessExists(pid)) {
                response.put("status", 400);
                response.put("message", "Process with PID " + pid + " does not exist.");
                return response;
            }

            // 查询任务基本信息
            Task selectedTask = taskRepository.selectTask(pid);

            // 获取当前任务所在的组路径
            String currentPath = selectedTask.getPath();

            // 目标组的名称
            String targetCgroupName = target;

            // 查询目标组的路径和配置文件路径
            Map<String, String> path = getPath(hierarchy, targetCgroupName);
            String parentCgroupPath = path.get("cgroupPath");
            String parentConfigDir = path.get("cgroupConfigDir");

            // 拼接为cgroupPath,任务将加入到次路径下
            String cgroupPath = parentCgroupPath;

            // 拼接为cgroupConfigPath,作为配置文件的路径
            String cgroupConfigPath = parentConfigDir + "/" + targetCgroupName + ".json";

            // 将进程迁移到目标 cgroup
            ProcessBuilder migrateProcess = new ProcessBuilder("bash", "-c", "echo " + pid + " > "
                                                                + cgroupPath + "/" + "cgroup.procs");
            int migrateResult = migrateProcess.start().waitFor();

            if (migrateResult == 0) {
                // 如果迁移成功，则向配置文件中加入该任务的条目
                addTaskToConfigFile(cgroupConfigPath, pid);

                // 如果迁移成功，删除原来配置文件中的该条目
                if(currentPath != null) {
                    Cgroup selectedCgroup;

                    try{
                        selectedCgroup = cgroupRepository.selectCgroup(hierarchy, currentPath);
                        String configDir = selectedCgroup.getCgroupConfigDir();
                        String originCgroupName = selectedCgroup.getName();
                        String configPath = configDir + "/" + originCgroupName + ".json";

                        File configFile = new File(configPath);
                        if (configFile.exists() && !configFile.delete()) {
                            throw new Exception("Failed to delete config file: " + configFile.getAbsolutePath());
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                // 向数据库中更新该任务的信息
                taskRepository.updateTask(pid, cgroupPath);

                response.put("status", 200);
                response.put("message", "Process " + pid + " migrated to " + target + " successfully.");
            } else {
                response.put("status", 400);
                response.put("message", "Failed to migrate process " + pid + " to " + target + ".");
            }
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "An error occurred during migration: " + e.getMessage());
            return response;
        }
        return response;
    }

    private boolean checkProcessExists(String pid) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("ps", "-p", pid);
        Process process = pb.start();
        int exitCode = process.waitFor();
        return exitCode == 0;
    }

    // 在数据库中查询，获取父cgroup路径和父cgroup配置文件的目录
    private Map<String, String> getPath(int parentHierarchy,String parentName) {
        Cgroup selectedCgroup;

        selectedCgroup = cgroupRepository.selectCgroup(parentHierarchy, parentName);

        Map<String, String> path = new HashMap<>();
        path.put("cgroupPath", selectedCgroup.getCgroupPath());
        path.put("cgroupConfigDir", selectedCgroup.getCgroupConfigDir());

        return path;
    }

    private void addTaskToConfigFile(String configFilePath, String pid) throws IOException {
        File configFile = new File(configFilePath);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode configContent;

        if (configFile.exists()) {
            String content = new String(Files.readAllBytes(Paths.get(configFilePath)));
            configContent = (ObjectNode) objectMapper.readTree(content);
        } else {
            configContent = objectMapper.createObjectNode();
        }

        ArrayNode tasks;
        if (configContent.has("tasks")) {
            tasks = (ArrayNode) configContent.get("tasks");
        } else {
            tasks = objectMapper.createArrayNode();
            configContent.set("tasks", tasks);
        }

        ObjectNode newTask = objectMapper.createObjectNode();
        newTask.put("pid", pid);
        tasks.add(newTask);

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, configContent);
    }
}
