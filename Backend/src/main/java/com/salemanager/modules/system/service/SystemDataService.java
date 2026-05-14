package com.salemanager.modules.system.service;

import com.salemanager.modules.system.param.AppendDataParam;

import java.util.Map;

public interface SystemDataService {

    Map<String, Object> clearData();

    Map<String, Object> initData();

    Map<String, Object> appendData(AppendDataParam param);
}
