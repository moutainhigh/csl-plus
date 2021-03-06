package com.csl.plus.res.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csl.plus.annotation.SysLog;
import com.csl.plus.audit.entity.ReviewLog;
import com.csl.plus.res.entity.ResFinance;
import com.csl.plus.res.service.IResFinanceService;
import com.csl.plus.utils.CommonResult;
import com.csl.plus.utils.ValidatorUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 金融需求表
 *
 * @author David
 * @email
 * @date 2020-02-15 22:12:07
 */
@Slf4j
@RestController
@Api(tags = "/api/ResFinanceController", description = "金融需求表管理")
@RequestMapping("res/finance")
public class ResFinanceController {
    @Autowired
    private IResFinanceService resFinanceService;

    /**
     * 列表
     */
    @SysLog(MODULE = "cms", REMARK = "根据条件查询列表")
    @ApiOperation("根据条件查询列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('res:resfinance:list')")
    public Object getResFinanceByPage(ResFinance entity, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        try {
            IPage<ResFinance> page = new Page<ResFinance>(pageNum, pageSize);
            page.setRecords(resFinanceService.getList());
            return new CommonResult()
                    .success(page);
        } catch (Exception e) {
            log.error("根据条件查询所有金融需求表列表：%s", e.getMessage(), e);
        }
        return new CommonResult().failed();
    }


    /**
     * 信息
     */
    /**
     @SysLog(MODULE = "cms", REMARK = "根据条件查询金融需求表列表")
     @ApiOperation("根据条件查询金融需求表列表")
     @GetMapping("/info/{id}")
     @PreAuthorize("hasAuthority('res:resfinance:info')") public R info(@PathVariable("id") Long id){
     ResFinanceEntity resFinance = resFinanceService.getById(id);

     return R.ok().put("resFinance", resFinance);
     }
     */
    /**
     * 保存
     */
    @SysLog(MODULE = "cms", REMARK = "保存金融需求表")
    @ApiOperation("保存金融需求表")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('res:resfinance:save')")
    public Object save(@RequestBody ResFinance entity) {
        try {
            if (resFinanceService.saves(entity)) {
                return new CommonResult().success();
            }
        } catch (Exception e) {
            log.error("保存帮助表：%s", e.getMessage(), e);
            return new CommonResult().failed();
        }
        return new CommonResult().failed();
    }

    /**
     * 修改
     */
    @SysLog(MODULE = "cms", REMARK = "修改金融需求表")
    @ApiOperation("修改金融需求表")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('res:resfinance:update')")
    public Object update(@RequestBody ResFinance entity) {
        try {
            if (resFinanceService.updateById(entity)) {
                return new CommonResult().success();
            }
        } catch (Exception e) {
            log.error("更新帮助表：%s", e.getMessage(), e);
            return new CommonResult().failed();
        }
        return new CommonResult().failed();
    }

    /**
     * 删除
     */
    @SysLog(MODULE = "cms", REMARK = "删除金融需求表")
    @ApiOperation("删除金融需求表")
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('res:resfinance:delete')")
    public Object delete(@ApiParam("id") @PathVariable Long id) {
        try {
            if (ValidatorUtils.empty(id)) {
                return new CommonResult().paramFailed("帮助表id");
            }
            if (resFinanceService.removeById(id)) {
                return new CommonResult().success();
            }
        } catch (Exception e) {
            log.error("删除帮助表：%s", e.getMessage(), e);
            return new CommonResult().failed();
        }
        return new CommonResult().failed();
    }

    @SysLog(MODULE = "cms", REMARK = "查询金融需求表明细")
    @ApiOperation("查询金融需求表明细")
    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('res:resfinance:read')")
    public Object getResFinanceById(@ApiParam("新闻表id") @PathVariable Long id) {
        try {
            if (ValidatorUtils.empty(id)) {
                return new CommonResult().paramFailed("金融需求表id");
            }
            ResFinance object = resFinanceService.getById(id);
            return new CommonResult().success(object);
        } catch (Exception e) {
            log.error("查询金融需求表明细：%s", e.getMessage(), e);
            return new CommonResult().failed();
        }
    }

    @ApiOperation("批量修改审核状态")
    @PostMapping(value = "/update/verifyStatus")
    @ResponseBody
    @SysLog(MODULE = "res", REMARK = "批量修改审核状态")
    @PreAuthorize("hasAuthority('res:resfinance:update')")
    public Object updateVerifyStatus(@RequestParam("ids") Long ids,
                                     @RequestParam("verifyStatus") Integer verifyStatus,
                                     @RequestParam("detail") String detail) {
        int count = resFinanceService.updateVerifyStatus(ids, verifyStatus, detail);
        if (count > 0) {
            return new CommonResult().success(count);
        } else {
            return new CommonResult().failed();
        }
    }


    @ApiOperation("根据id获取审核信息")
    @GetMapping(value = "/fetchVList/{id}/{sysGroup}")
    @ResponseBody
    @SysLog(MODULE = "res", REMARK = "据id获取审核信息")
    @PreAuthorize("hasAuthority('res:resfinance:read')")
    public Object fetchVList(@PathVariable Long id, @PathVariable String sysGroup) {
        List<ReviewLog> list = resFinanceService.getVertifyRecord(id, sysGroup);
        return new CommonResult().success(list);
    }
}
