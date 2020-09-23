package com.markerhub.mapper;

import com.markerhub.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 关注公众号：MarkerHub
 * @since 2020-05-25
 */
@Mapper
@Repository
public interface BlogMapper extends BaseMapper<Blog> {
  Blog getById(@Param("id") Serializable id);
}
