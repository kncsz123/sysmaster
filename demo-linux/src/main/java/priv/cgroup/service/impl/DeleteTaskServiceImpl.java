package priv.cgroup.service.impl;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeleteTaskServiceImpl implements DeleteTaskService {

    @Override
    public Map<String, Object> deleteTask(String name) {
        Map<String, Object> response = new HashMap<>();

        // 获取当前所在的工作目录
        String currentTaskPath = "";
        try {
            Process pwdProcess = new ProcessBuilder("pwd").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(pwdProcess.getInputStream()));
            currentTaskPath = reader.readLine();
            reader.close();
            pwdProcess.waitFor();
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "Failed to get current working directory: " + e.getMessage());
            return response;
        }

        // 拼接路径以删除任务
        String taskPath = currentTaskPath + "/" + name;

        try {
            // 检查当前任务是否存在
            Process checkProcess = new ProcessBuilder("test", "-d", taskPath).start();
            int checkResult = checkProcess.waitFor();

            if (checkResult != 0) {
                // 如果不存在，函数返回，阻止用户删除
                response.put("status", 400);
                response.put("message", "Task " + name + " does not exist.");
                return response;
            }

            // 删除任务
            Process deleteProcess = new ProcessBuilder("rmdir", taskPath).start();
            deleteProcess.waitFor();

            // 检查删除状态
            Process verifyProcess = new ProcessBuilder("test", "-d", taskPath).start();
            int exitcode = verifyProcess.waitFor();

            // 返回结果给前端
            if (exitcode != 0) {
                response.put("status", 200);
                response.put("message", "Task " + name + " deleted successfully.");
            } else {
                response.put("status", 400);
                response.put("message", "Failed to delete task " + name);
            }
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "An error occurred: " + e.getMessage());
        }

        return response;
    }
}
