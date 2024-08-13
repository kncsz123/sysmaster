package priv.cgroup.service.impl;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class CreateTaskServiceImpl implements CreateTaskService {
    @Override
    public Map<String, Object> createTask(String command) {
        Map<String, Object> response = new HashMap<>();
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);

        try {
            // 启动进程
            Process process = processBuilder.start();

            // 获取进程的 PID
            long pid = process.pid();

            // 创建新线程读取进程输出
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line); // 打印进程的标准输出（可选）
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // 返回 PID 给前端
            response.put("status", 200);
            response.put("pid", pid);
        } catch (IOException e) {
            response.put("status", 500); // 错误 - I/O 异常
            response.put("pid", null);
        }

        return response;
    }
}
