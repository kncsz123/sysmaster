package priv.cgroup.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class ViewTaskResourceUsage {

    public Map<String, Object> viewTaskResourceUsage(String pid) {
        Map<String, Object> response = new HashMap<>();
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", "top -b -n 1 -p " + pid + " | grep '^ *" + pid + " '");

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;

            // Read and print the error stream
            while ((line = errorReader.readLine()) != null) {
                System.err.println("ERROR: " + line);
            }

            // Read the output stream
            while ((line = reader.readLine()) != null) {
                System.out.println("OUTPUT: " + line);
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 12) {
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
                String TIME = parts[10];
                String COMMAND = parts[11];

                // Debugging: Print each part to verify parsing correctness
                System.out.println("PID: " + PID + ", USER: " + USER + ", PR: " + PR + ", NI: " + NI
                        + ", VIRT: " + VIRT + ", RES: " + RES + ", SHR: " + SHR + ", S: " + S
                        + ", CPU: " + CPU + ", MEM: " + MEM + ", TIME: " + TIME + ", COMMAND: " + COMMAND);

                // 返回结果
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
                response.put("time", TIME);
                response.put("command", COMMAND);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                response.put("status", 400);
                response.put("message", "Process terminated with error: " + exitCode);
            } else {
                response.put("status", 200);
                response.put("message", "success");
            }
        } catch (IOException e) {
            response.put("status", 500);
            response.put("message", "fail: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}
