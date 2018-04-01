package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: ting
 * Date: 2018-03-12
 * Time: 下午10:09
 */
public interface IProductService {

    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse getProductList(int pageNum, int pageSize);

    ServerResponse searchProduct(String productName, Integer productId, int pageNum, int pageSize);


}
