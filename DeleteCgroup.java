package priv.cgroup.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.object.Cgroup;
import priv.cgroup.repository.CgroupRepository;
import priv.cgroup.repository.TaskRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeleteCgroup {

    @Autowired
    private final CgroupRepository cgroupRepository;

    @Autowired
    private final TaskRepository taskRepository;

    public DeleteCgroup(CgroupRepository cgroupRepository, TaskRepository taskRepository) {
        this.cgroupRepository = cgroupRepository;
        this.taskRepository = taskRepository;
    }

    /*
        @param name cgroup的名称
        @param hierarchy cgroup所在层级
     */
    public Map<String, Object> deleteCgroup(Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();

        String name = requestBody.get("name").toString();
        int hierarchy = Integer.parseInt(requestBody.get("hierarchy").toString());

        if(name.equals("user")){
            response.put("status", 403);
            response.put("message", "you have no authority to do this operation");
            return response;
        }

        //获取cgroupName
        String cgroupName = name;

        //获取cgroup的路径和cgroupConfigDir的路径
        Map<String, String> path = getPath(hierarchy, name);
        String cgroupPath = path.get("cgroupPath");
        String configDir = path.get("cgroupConfigDir");

        try {
            // 读取cgroupPath + File.seperator + cgroup.procs文件里是否存在活动的进程。
            // TODO 如果存在活动的进程，则中断该服务，等待用户选择确定删除后，前端传来确定继续执行下列操作
            if (hasActiveProcesses(cgroupPath)) {
                response.put("status", 409);  // 409 Conflict 表示需要用户确认
                response.put("message", "There are active processes in the cgroup. Confirm deletion?");
                response.put("needsConfirmation", true);
                return response;
            }

            // 删除cgroup
            Process deleteProcess1 = new ProcessBuilder("sudo", "rmdir", cgroupPath).start();
            deleteProcess1.waitFor();

            // 删除当前cgroup配置文件所在目录（这将会删除所有子文件夹）
            deleteDirectory(new File(configDir));

            // 批量删除数据库中所有cgroupPath前缀含有当前cgroupPath的条目
            cgroupRepository.deleteCgroupByPathPrefix(cgroupPath);
            taskRepository.deleteTaskByPathPrefix(cgroupPath);

            // 删除configDir下名为name.json的配置文件
            File configFile = new File(configDir + "/" + name + ".json");
            if (configFile.exists() && !configFile.delete()) {
                throw new Exception("Failed to delete config file: " + configFile.getAbsolutePath());
            }

            response.put("status", 200);
            response.put("message", "success");
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "Failed to get delete cgroup: " + e.getMessage());
            return response;
        }

        return response;

    }

    // 在数据库中查询，获取cgroup路径和cgroup配置文件的目录
    private Map<String, String> getPath(int hierarchy,String name) {
        Cgroup selectedCgroup = new Cgroup();

        try {
            selectedCgroup = cgroupRepository.selectCgroup(hierarchy, name);
        }catch(Exception e) {
            e.printStackTrace();
        }

        Map<String, String> path = new HashMap<>();
        path.put("cgroupPath", selectedCgroup.getCgroupPath());
        path.put("cgroupConfigDir", selectedCgroup.getCgroupConfigDir());

        return path;
    }

    // 递归删除目录及其内容
    private void deleteDirectory(File directory) throws Exception {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        if (!directory.delete()) {
            throw new Exception("Failed to delete directory: " + directory.getAbsolutePath());
        }
    }

    // 判断 cgroup 中是否存在活动的进程
    private boolean hasActiveProcesses(String cgroupPath) throws IOException {
        File procsFile = new File(cgroupPath + File.separator + "cgroup.procs");
        try (BufferedReader reader = new BufferedReader(new FileReader(procsFile))) {
            return reader.readLine() != null;  // 如果有内容，返回 true，表示存在活动进程
        }
    }
}
