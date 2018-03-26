package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.common.ServerResponse;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: ting
 * Date: 2018-03-13
 * Time: 下午10:01
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService{
    private static Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);


    @Override
    public String upload(MultipartFile file, String path){
        String fileName = file.getOriginalFilename();
        //获取扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        //重命名上传文件，防止用户上传同名文件导致上传报错
        String uploadFileName = UUID.randomUUID().toString() + "." +fileExtensionName;
        logger.info("开始上传文件:{}，上传至:{},新文件名为:{}",fileName,path,uploadFileName);
        File fileDir = new File(path);
        if(!fileDir.exists()){
            //赋予可写权限
            fileDir.setWritable(true);
            //可创建多层文件夹
            fileDir.mkdirs();
        }

        File targetFile = new File(path, uploadFileName);
        try {
            //转存文件到指定的路径
            file.transferTo(targetFile);

            //将targetFile上传到ftp服务器上,Guavas将MultipartFile转成list
            boolean isSuccess = FTPUtil.uploadFile(Lists.newArrayList(targetFile));

            if(!isSuccess){
                ServerResponse.createByErrorMessage("连接ftp失败");
            }
            //上传完成后，删除upload下文件夹
            targetFile.delete();


        } catch (IOException e) {
            logger.error("上传文件异常");
            return null;
        }
        return  targetFile.getName();
    }

}
