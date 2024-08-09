package priv.cgroup.service.impl;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MigrateTaskServiceImpl implements MigrateTaskService {

    @Override
    public Map<String, Object> migrateTask(String pid, String target) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检查进程是否存在
            if (!checkProcessExists(pid)) {
                response.put("status", 400);
                response.put("message", "Process with PID " + pid + " does not exist.");
                return response;
            }

            // 检查目标 cgroup 是否存在
            String targetPath = "/sys/fs/cgroup/" + target;
            if (!checkCgroupExists(targetPath)) {
                response.put("status", 400);
                response.put("message", "Target cgroup " + target + " does not exist.");
                return response;
            }

            // 将进程迁移到目标 cgroup
            ProcessBuilder migrateProcess = new ProcessBuilder("bash", "-c", "echo " + pid + " > " + targetPath + "/tasks");
            int migrateResult = migrateProcess.start().waitFor();

            if (migrateResult == 0) {
                response.put("status", 200);
                response.put("message", "Process " + pid + " migrated to " + target + " successfully.");
            } else {
                response.put("status", 500);
                response.put("message", "Failed to migrate process " + pid + " to " + target + ".");
            }
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "An error occurred during migration: " + e.getMessage());
        }
        return response;
    }

    private boolean checkProcessExists(String pid) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("ps", "-p", pid);
        Process process = pb.start();
        int exitCode = process.waitFor();
        return exitCode == 0;
    }

    private boolean checkCgroupExists(String path) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("test", "-d", path);
        Process process = pb.start();
        int exitCode = process.waitFor();
        return exitCode == 0;
    }
}
