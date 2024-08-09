package priv.cgroup.service;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import priv.cgroup.object.Cgroup;
import priv.cgroup.repository.CgroupRepository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class CreateCgroup {

    private static final Logger logger = Logger.getLogger(CreateCgroup.class.getName());

    @Autowired
    private final CgroupRepository cgroupRepository;

    public CreateCgroup(CgroupRepository cgroupRepository) {
        this.cgroupRepository = cgroupRepository;
    }

    /*
        @param name 用户创建的组名
        @param parentName 父cgroup的名称
        @param parentHierarchy 父cgroup所在层级
     */

    //TODO
//    /**
//     * 生成一个 Shell 脚本，用于为特定用户创建 cgroup 及相关配置。
//     *
//     * @param filePath        Shell 脚本的文件路径
//     * @param userName        用户名
//     * @param cgroupName      cgroup 名称
//     * @param cgroupPath      cgroup 路径
//     * @param cgroupConfigDir cgroup 配置目录
//     * @param hierarchy       cgroup 层级
//     * @throws IOException 如果在创建或写入文件时发生错误
//     */
    public Map<String, Object> createCgroup(Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();

        String name = requestBody.get("name").toString();
        String parentName = requestBody.get("parentName").toString();
        int parentHierarchy = Integer.parseInt(requestBody.get("parentHierarchy").toString());

        // 获取用户创建的cgroup名称
        String cgroupName = name;

        // 子cgroup的层级
        int childHierarchy = parentHierarchy + 1;

        // 获取父cgroup的路径和父cgroupConfigDir的路径
        Map<String, String> path;
        try {
            path = getPath(parentHierarchy, parentName);
            if (path.isEmpty()) {
                throw new Exception("Parent cgroup not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 500);
            response.put("message", e.getMessage());
            response.put("name", null);
            response.put("cgroupPath", null);
            response.put("cgroupConfigDir", null);
            response.put("hierarchy", null);
            return response;
        }

        String parentCgroupPath = path.get("cgroupPath");
        String parentConfigDir = path.get("cgroupConfigDir");

        // 拼接为cgroupPath
        String cgroupPath = parentCgroupPath + "/" + cgroupName;

        // 拼接为cgroupConfigDir
        String cgroupConfigDir = parentConfigDir + "/" + cgroupName;

        // 检查当前层级是否已存在同名的cgroup
        if (isCgroupNameExists(childHierarchy, cgroupName)) {
            response.put("status", 400);
            response.put("message", "A cgroup with the same name already exists in the current hierarchy.");
            response.put("name", null);
            response.put("cgroupPath", null);
            response.put("cgroupConfigDir", null);
            response.put("hierarchy", null);
            return response;
        }

        try {
            // 尝试创建cgroup
            Process createProcess = new ProcessBuilder("sudo", "mkdir", "-p", cgroupPath).start();
            createProcess.waitFor();

            // 在父cgroupConfigDir以name创建目录，并在该目录下创建对应的json配置文件，文件名为name.json
            // 并将cgroup的基本信息写入配置文件，然后在数据库中加入cgroup信息

            // 获取创建时间
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd E HH:mm:ss");
            String datestamp = now.format(formatter);

            // 更新数据库
            createConfigFile(cgroupName, cgroupPath, cgroupConfigDir, childHierarchy);
            Cgroup insertCgroup = new Cgroup();
            insertCgroup.setName(name);
            insertCgroup.setCgroupPath(cgroupPath);
            insertCgroup.setCgroupConfigDir(cgroupConfigDir);
            insertCgroup.setDatestamp(datestamp);
            insertCgroup.setDescription("default");
            insertCgroup.setHierarchy(childHierarchy);
            insertCgroup.setCpu_controller_status(true);
            insertCgroup.setMemory_controller_status(true);
            insertCgroup.setIo_controller_status(true);
            insertCgroup.setPids_controller_status(true);
            insertCgroup.setCpuset_controller_status(true);
            cgroupRepository.save(insertCgroup);

            // 开启控制器使用权
            List<String> controllers = new ArrayList<>();
            controllers.add("cpu");
            controllers.add("io");
            controllers.add("pids");
            controllers.add("cpuset");
            controllers.add("memory");

            String subtree_controlPath = parentCgroupPath + "/cgroup.subtree_control";
            for (String controller : controllers) {
                String command = String.format("echo +%s > %s", controller, subtree_controlPath);
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
                    Process process = processBuilder.start();
                    process.waitFor();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 返回正常结果，包含status, message, name, cgroupPath, cgroupConfigPath
            response.put("status", 200);
            response.put("message", "success");
            response.put("name", name);
            response.put("cgroupPath", cgroupPath);
            response.put("cgroupConfigDir", cgroupConfigDir);
            response.put("hierarchy", childHierarchy);
        } catch (Exception e) {
            // 返回异常结果，包含status, message, name, cgroupPath, cgroupConfigPath
            response.put("status", 500);
            response.put("message", e.getMessage());
            response.put("name", null);
            response.put("cgroupPath", null);
            response.put("cgroupConfigDir", null);
            response.put("hierarchy", null);
            return response;
        }
        return response;
    }

    // 检查当前层级是否已存在同名的cgroup
    private boolean isCgroupNameExists(int hierarchy, String name) {
        Specification<Cgroup> specification = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("hierarchy"), hierarchy),
                criteriaBuilder.equal(root.get("name"), name)
        );
        return cgroupRepository.findOne(specification).isPresent();
    }

    // 创建配置文件函数
    private void createConfigFile(String cgroupName, String cgroupPath, String cgroupConfigDir, int hierarchy) throws Exception {
        File configDir = new File(cgroupConfigDir);

        if (!configDir.exists()) {
            boolean dirCreated = configDir.mkdirs();
            if (!dirCreated) {
                throw new Exception("Failed to create directory: " + cgroupConfigDir);
            }
        }

        // 创建指向配置文件的File对象
        File configFile = new File(configDir + "/" + cgroupName + ".json");
        try {
            // 创建配置
            if (configFile.createNewFile()) {
                System.out.println("文件创建成功: " + configFile.getAbsolutePath());
            } else {
                System.out.println("文件已存在: " + configFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.out.println("文件创建失败: " + e.getMessage());
        }

        // 创建配置内容
        Map<String, Object> configContent = new HashMap<>();
        configContent.put("name", cgroupName);
        configContent.put("cgroupPath", cgroupPath);
        configContent.put("cgroupConfigDir", cgroupConfigDir);
        configContent.put("hierarchy", hierarchy);

        // 使用 Jackson 库将配置内容写入 JSON 文件
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, configContent);
    }

    // 在数据库中查询，获取父cgroup路径和父cgroup配置文件的目录
    private Map<String, String> getPath(int hierarchy, String name) throws Exception {
        Cgroup selectedCgroup = cgroupRepository.selectCgroup(hierarchy, name);
        Map<String, String> path = new HashMap<>();

        if (StrUtil.isEmptyIfStr(selectedCgroup)) {
            throw new Exception("Parent cgroup not found or invalid.");
        } else {
            path.put("cgroupPath", selectedCgroup.getCgroupPath());
            path.put("cgroupConfigDir", selectedCgroup.getCgroupConfigDir());
        }

        return path;
    }

//    public void generateShellScript(String filePath, String userName, String cgroupName, String cgroupPath, String cgroupConfigDir, int hierarchy) throws IOException {
//        File scriptFile = new File(filePath);
//
//        // 确保脚本文件的目录存在
//        File parentDir = scriptFile.getParentFile();
//        if (!parentDir.exists()) {
//            parentDir.mkdirs();
//        }
//
//        // 创建并写入 Shell 脚本内容
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile))) {
//            writer.write("#!/bin/bash\n");
//            writer.write("\n");
//
//            // 写入用于创建 cgroup 目录的命令
//            writer.write(String.format("sudo mkdir -p %s\n", cgroupPath));
//            writer.write(String.format("sudo chown -R %s:%s %s\n", userName, userName, cgroupPath));
//
//            // 写入用于创建配置目录的命令
//            writer.write(String.format("sudo mkdir -p %s\n", cgroupConfigDir));
//            writer.write(String.format("sudo chown -R %s:%s %s\n", userName, userName, cgroupConfigDir));
//
//            // 写入用于创建配置文件的命令
//            String configFilePath = cgroupConfigDir + "/" + cgroupName + ".json";
//            writer.write(String.format("sudo touch %s\n", configFilePath));
//            writer.write(String.format("sudo chown %s:%s %s\n", userName, userName, configFilePath));
//
//            // 写入 JSON 配置内容到配置文件
//            writer.write(String.format("echo '{\n"));
//            writer.write(String.format("  \"name\": \"%s\",\n", cgroupName));
//            writer.write(String.format("  \"cgroupPath\": \"%s\",\n", cgroupPath));
//            writer.write(String.format("  \"cgroupConfigDir\": \"%s\",\n", cgroupConfigDir));
//            writer.write(String.format("  \"hierarchy\": %d\n", hierarchy));
//            writer.write("}' | sudo tee " + configFilePath + "\n");
//
//            writer.write("\n");
//            writer.write("echo \"Cgroup and configuration file created for user " + userName + ".\"\n");
//        }
//
//        // 确保生成的 Shell 脚本有执行权限
//        ProcessBuilder pb = new ProcessBuilder("chmod", "+x", filePath);
//        pb.start();
//    }
//
//    // 原有的其他方法...
//
//    // 在 createCgroup 方法中或其他地方调用 generateShellScript 方法
//    public Map<String, Object> createCgroupForUser(Map<String, Object> requestBody) {
//        Map<String, Object> response = new HashMap<>();
//
//        String name = requestBody.get("name").toString();
//        String parentName = requestBody.get("parentName").toString();
//        int parentHierarchy = Integer.parseInt(requestBody.get("parentHierarchy").toString());
//        String userName = requestBody.get("userName").toString(); // 用户名
//
//        // ... 其他逻辑保持不变
//
//        try {
//            // 创建 cgroup 逻辑...
//
//            // 调用 generateShellScript 方法生成 Shell 脚本
//            String scriptPath = "/path/to/scripts/create_cgroup_" + name + ".sh"; // 根据需求定义路径
//            generateShellScript(scriptPath, userName, name, cgroupPath, cgroupConfigDir, childHierarchy);
//
//            // ... 其他逻辑保持不变
//
//            response.put("status", 200);
//            response.put("message", "success");
//            response.put("name", name);
//            response.put("cgroupPath", cgroupPath);
//            response.put("cgroupConfigDir", cgroupConfigDir);
//            response.put("hierarchy", childHierarchy);
//        } catch (Exception e) {
//            response.put("status", 500);
//            response.put("message", e.getMessage());
//            response.put("name", null);
//            response.put("cgroupPath", null);
//            response.put("cgroupConfigDir", null);
//            response.put("hierarchy", null);
//        }
//        return response;
//    }
}



/*
        方案2：new Cgroup().setName();
        Example.of()
        cgroupRepository.findOne(E)

        方案3：Specification<Cgroup> sp = (root,query,builder)->{
          return   query.where(builder.equal(root.get("name"), name),builder.equal(root.get("hierarchy"), hierarchy)).getRestriction();
        };
        Optional<Cgroup> c = cgroupRepository.findOne(sp);
*/