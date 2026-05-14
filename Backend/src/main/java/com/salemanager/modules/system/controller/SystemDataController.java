package com.salemanager.modules.system.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.system.param.AppendDataParam;
import com.salemanager.modules.system.service.SystemDataService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/system/data")
@Validated
public class SystemDataController {

    private static final Logger log = LoggerFactory.getLogger(SystemDataController.class);

    @Autowired
    private SystemDataService systemDataService;

    @PostMapping("/clear")
    public Result<Map<String, Object>> clearData() {
        log.info("clearData called");
        return Result.success(systemDataService.clearData());
    }

    @GetMapping("/init")
    public Result<Map<String, Object>> initData() {
        log.info("initData called");
        return Result.success(systemDataService.initData());
    }

    @PostMapping("/append")
    public Result<Map<String, Object>> appendData(@Valid @RequestBody AppendDataParam param) {
        log.info("appendData module={}, count={}", param.getModule(), param.getCount());
        return Result.success(systemDataService.appendData(param));
    }

    @GetMapping("/append")
    public Result<Map<String, Object>> appendDataGet(@RequestParam(defaultValue = "all") String module,
                                                      @RequestParam(defaultValue = "10") int count) {
        AppendDataParam param = new AppendDataParam();
        param.setModule(module);
        param.setCount(count);
        log.info("appendData GET module={}, count={}", module, count);
        return Result.success(systemDataService.appendData(param));
    }
}
