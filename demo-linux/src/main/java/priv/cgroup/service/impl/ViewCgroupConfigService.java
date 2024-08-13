package priv.cgroup.service.impl;

import java.util.Map;

public interface ViewCgroupConfigService {
    public Map<String, Object> viewCgroupConfig(String name, String controllerID);
}
