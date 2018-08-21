package com.bsoft.deploy.file;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/**
 * 监测目标目录文件的改变
 * Created on 2018/8/6.
 *
 * @author yangl
 */
public class FileWatcher {

    public void watch(String path) {
        Path dir = Paths.get(path);
        try {
            WatchService watcher = dir.getFileSystem().newWatchService();
            dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                WatchKey watchKey = watcher.take();
                List<WatchEvent<?>> events = watchKey.pollEvents();
                for (WatchEvent event : events) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        System.out.println("Modify: " + event.context().toString());
                    }
                }
                if (!watchKey.reset()) {
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
