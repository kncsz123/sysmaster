package priv.cgroup.service.impl;

import java.util.Map;

public interface MigrateTaskService {
    public Map<String, Object> migrateTask(String pid, String target);
}
