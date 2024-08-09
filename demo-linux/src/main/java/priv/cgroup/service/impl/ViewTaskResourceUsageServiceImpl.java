package priv.cgroup.service.impl;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class ViewTaskResourceUsageServiceImpl implements ViewTaskResourceUsageService {

    @Override
    public Map<String, Object> viewTaskResourceUsage() {
        //获取top命令返回的资源使用率参数
        Map<String, Object> response = new HashMap<String, Object>();
        ProcessBuilder processBuilder = new ProcessBuilder("top","-b","-n","1");

        try{
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            boolean capture = false;

            // 跳过前几行直到达到进程列表部分
//            for (int i = 0; i < 7; i++) {
//                reader.readLine();
//            }

            while ((line = reader.readLine()) != null) {
                if(line.startsWith("PID")){
                    capture = true;
                    continue;
                }
                if(capture){
                    String[] parts = line.trim().split("\\s+");
                    if(parts.length < 12){
                        continue;
                    }
                    String PID = parts[0];
                    String USER = parts[1];
                    String PR = parts[2];
                    String NI = parts[3];
                    String VIRT = parts[4];
                    String RES = parts[5];
                    String SHR = parts[6];
                    String S = parts[7];
                    String CPU = parts[8];
                    String MEM = parts[9];
                    String COMMAND = parts[11];

                    //返回结果
                    Map<String, String> taskUsage = new HashMap<>();
                    response.put("pid", PID);
                    response.put("user", USER);
                    response.put("pr", PR);
                    response.put("ni", NI);
                    response.put("virt", VIRT);
                    response.put("res", RES);
                    response.put("shr", SHR);
                    response.put("s", S);
                    response.put("cpu%", CPU);
                    response.put("mem%", MEM);
                    response.put("command", COMMAND);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                response.put("status", 400);
            } else {
                response.put("status", 200);
            }
        } catch (IOException e) {
            response.put("status", 400);
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //将参数加入到response中返回
        return response;
    }
}
