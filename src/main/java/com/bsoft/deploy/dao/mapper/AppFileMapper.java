package com.bsoft.deploy.dao.mapper;

import com.bsoft.deploy.dao.entity.App;
import com.bsoft.deploy.dao.entity.FileDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 应用文件
 * Created on 2018/8/13.
 *
 * @author yangl
 */
@Mapper
public interface AppFileMapper {
    /**
     * 获取文件对象
     *
     * @param appId
     * @param path
     * @return
     */
    @Select("select id,mark from base_app_file where appId=#{appId} and path=#{path}")
    FileDTO loadAppFile(@Param("appId") int appId, @Param("path") String path);

    /**
     * 插入文件对象
     *
     * @param fileDTO
     * @return
     */
    @Insert({"insert into base_app_file(appId, filename, path, mark, optime,sign) values(#{appId}, #{filename}, #{path},#{mark}, #{optime, jdbcType=TIMESTAMP},0)"})
    @Options(useGeneratedKeys = true)
    int saveAppFile(FileDTO fileDTO);

    /**
     * 更新文件标记和最后修改时间
     *
     * @param fileDTO
     * @return
     */
    @Update({"update base_app_file set optime=#{optime, jdbcType=TIMESTAMP},mark=#{mark} where id=#{id}"})
    int updateAppFile(FileDTO fileDTO);

    /**
     * 根据id删除文件
     *
     * @param id
     */
    @Delete({"delete base_app_file where id=#{id}"})
    void deleteById(int id);

    @Select("select path from base_app where appId=#{appId}")
    String findPathById(int id);

    @Select("select appId,appName,path from base_app")
    List<App> loadApps();

}
