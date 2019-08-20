package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);


    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse addCategory(String categoryName, int parentId) {
        if(StringUtils.isBlank(categoryName) || parentId < 0){
            return ServerResponse.createByErrorMsg("添加类别参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int resultCount = categoryMapper.insertSelective(category);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMsg("成功添加 类别");
        }
        return ServerResponse.createByErrorMsg("类别添加不成功");
    }


    @Override
    public ServerResponse updateCategoryName(Integer cgId, String cgName){
        if(cgId == null || StringUtils.isBlank(cgName)){
            return ServerResponse.createByErrorMsg("修改分类名，参数错误");
        }
        // 这里只更新某个字段，推荐做法是 新建一个 category 对象，然后将新字段set进去，再调用updateSelective的既有方法进行选择性更新，
        // 就不用单独再开一个新的 数据库操作方法了

        int resultCount = categoryMapper.updateCgNameById(cgId,cgName);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMsg("成功修改分类名");
        }
        return ServerResponse.createByErrorMsg("修改分类名失败");
    }


    @Override
    public ServerResponse<List<Category>> getChildrebParallelCategory(Integer categoryId){
        if(categoryId == null){
            return ServerResponse.createByErrorMsg("参数错误");
        }
        List<Category> categories = categoryMapper.getChildrenCategories(categoryId);
        if(CollectionUtils.isEmpty(categories)){
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categories);
    }


}
