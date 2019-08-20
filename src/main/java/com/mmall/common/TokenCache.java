package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TokenCache {

    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);
    public static final String TOKEN_PREFIX = "token_";

    // LRU算法 处理缓存
    // guava 中的本地缓存
    private static LoadingCache<String,String> loadingCache = CacheBuilder.newBuilder()
            .initialCapacity(1000)
            .maximumSize(10000)         // 超过这个值 调用LRU算法淘汰
            .expireAfterAccess(12,TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                @Override
                // 默认数据加载实现，如果get没有值时，就调用这个方法实现
                public String load(String s) throws Exception {
                    return "null";
                }
            });     // 防止 null 已出现的空指针现象，用字符串代替

    public static void setKey(String key, String value){
        loadingCache.put(key,value);
    }

    public static String getKey(String key) {
        try {
            String value = loadingCache.get(key);
            if("null".equals(value)){
                return null;
            }
            return value;
        } catch (ExecutionException e) {
            logger.error("localCache get error: " + e);
        }
        return null;
    }

}
