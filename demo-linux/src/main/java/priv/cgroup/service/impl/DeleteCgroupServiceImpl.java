package priv.cgroup.service.impl;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeleteCgroupServiceImpl implements DeleteCgroupService {
    @Override
    public Map<String, Object> deleteCgroup(String cgroupName) {
        Map<String, Object> response = new HashMap<>();

        // 获取当前所在的工作目录
        String currentCgroupPath = "";
        try {
            Process pwdProcess = new ProcessBuilder("pwd").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(pwdProcess.getInputStream()));
            currentCgroupPath = reader.readLine();
            reader.close();
            pwdProcess.waitFor();
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "Failed to get current working directory: " + e.getMessage());
            return response;
        }

        // 拼接路径以删除cgroup
        String cgroupPath = currentCgroupPath + "/" + cgroupName;

        try {
            // 检查当前cgroup是否存在
            Process checkProcess = new ProcessBuilder("test", "-d", cgroupPath).start();
            int checkResult = checkProcess.waitFor();

            if (checkResult != 0) {
                // 如果不存在，函数返回，阻止用户删除
                response.put("status", 400);
                response.put("message", "Cgroup " + cgroupName + " does not exist.");
                return response;
            }

            // 删除cgroup
            Process deleteProcess = new ProcessBuilder("rmdir", cgroupPath).start();
            deleteProcess.waitFor();

            // 检查删除状态
            Process verifyProcess = new ProcessBuilder("test", "-d", cgroupPath).start();
            int exitcode = verifyProcess.waitFor();

            // 返回结果给前端
            if (exitcode != 0) {
                response.put("status", 200);
                response.put("message", "Cgroup " + cgroupName + " deleted successfully.");
            } else {
                response.put("status", 400);
                response.put("message", "Failed to delete cgroup " + cgroupName);
            }
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "An error occurred: " + e.getMessage());
        }

        return response;
    }
}
