package priv.cgroup.service.impl;

import java.util.Map;

public interface DeleteCgroupService {
    public Map<String, Object> deleteCgroup(String cgroupName);
}
