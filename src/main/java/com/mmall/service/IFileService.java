package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: ting
 * Date: 2018-03-13
 * Time: 下午10:00
 */
public interface IFileService {

    String upload(MultipartFile file, String path);




}
