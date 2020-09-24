package com.markerhub.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markerhub.common.lang.Result;
import com.markerhub.entity.Blog;
import com.markerhub.service.BlogService;
import com.markerhub.util.ShiroUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 关注公众号：MarkerHub
 * @since 2020-05-25
 */
@RestController
public class BlogController {

    @Autowired
    BlogService blogService;
    @Value("${spring.servlet.multipart.location}")
    private String FILE_UPLOAD_PATH;

    @GetMapping("/blogs")
    public Result list(@RequestParam(defaultValue = "1") Integer currentPage) {

        Page page = new Page(currentPage, 5);
        IPage pageData = blogService.page(page, new QueryWrapper<Blog>().orderByDesc("created"));

        return Result.succ(pageData);
    }

    @GetMapping("/blog/{id}")
    public Result detail(@PathVariable(name = "id") Long id) {
        Blog blog = blogService.getById(id);
        Assert.notNull(blog, "该博客已被删除");

        return Result.succ(blog);
    }

    @RequiresAuthentication
    @PostMapping("/blog/edit")
    public Result edit(@Validated @RequestBody Blog blog) {

        Blog temp = null;
        if (blog.getId() != null) {
            temp = blogService.getById(blog.getId());
            // 只能编辑自己的文章
            System.out.println(ShiroUtil.getProfile().getId());
            Assert.isTrue(temp.getUserId().longValue() == ShiroUtil.getProfile().getId().longValue(), "没有权限编辑");

        } else {

            temp = new Blog();
            temp.setUserId(ShiroUtil.getProfile().getId());
            temp.setCreated(LocalDateTime.now());
            temp.setStatus(0);
        }

        BeanUtil.copyProperties(blog, temp, "id", "userId", "created", "status");
        blogService.saveOrUpdate(temp);

        return Result.succ(null);
    }

    @RequestMapping(value = "/uploadFiles", method = RequestMethod.POST)
    public Result handleFileUpload(
        @RequestParam MultipartFile[] fileUpload, HttpServletRequest request) throws Exception {
        File dirPath = new File(FILE_UPLOAD_PATH);
        if (!dirPath.exists()) {
            dirPath.mkdirs();
        }
        String fileName = "";
        for (MultipartFile aFile : fileUpload) {
            // 存储上传的文件
            fileName =  aFile.getOriginalFilename();
            aFile.transferTo(new File(FILE_UPLOAD_PATH + fileName));
        }
        String schema = request.getScheme();
        String serverName = request.getServerName();
        int portNumber = request.getServerPort();
        return Result.succ(schema + "://" + serverName + ":" + portNumber + "/files/" + fileName);
    }

    @GetMapping("/files/{fileName}")
    public void downloadFile(@PathVariable String fileName, HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding(request.getCharacterEncoding());
        response.setContentType("application/octet-stream");
        FileInputStream in = null;
        try {
            File file = new File(FILE_UPLOAD_PATH + fileName);
            in = new FileInputStream(file);
            ServletOutputStream outs = response.getOutputStream();
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
//            IOUtils.copy(fis,response.getOutputStream());
            int bit = 256;
            int i = 0;
            try {
                while ((bit) >= 0) {
                    bit = in.read();
                    outs.write(bit);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace(System.out);
            }
            outs.flush();
            outs.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}
