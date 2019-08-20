package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

public interface ICategoryService {

    ServerResponse addCategory(String categoryName, int parentId);

    ServerResponse updateCategoryName(Integer cgId, String cgName);

    ServerResponse<List<Category>> getChildrebParallelCategory(Integer categoryId);

}
