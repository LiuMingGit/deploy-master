package com.bsoft.deploy.service;

import com.bsoft.deploy.dao.entity.App;
import com.bsoft.deploy.dao.mapper.AppMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * desc
 * Created on 2018/8/23.
 *
 * @author yangl
 */
@Service
public class AppService {
    @Autowired
    AppMapper appMapper;

    public List<App> loadApps() {
        return appMapper.loadApps();
    }


    public void saveApp(App app) {
        appMapper.save(app);
    }

    public void updateApp(App app) {
        appMapper.update(app);
    }

    public void deleteApp(int appId) {
        appMapper.delete(appId);
    }
}
