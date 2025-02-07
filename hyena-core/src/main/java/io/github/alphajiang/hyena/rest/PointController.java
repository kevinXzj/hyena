/*
 *  Copyright (C) 2019 Alpha Jiang. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.github.alphajiang.hyena.rest;

import io.github.alphajiang.hyena.aop.Idempotent;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.PointUsageBuilder;
import io.github.alphajiang.hyena.biz.point.PointUsageFacade;
import io.github.alphajiang.hyena.biz.point.strategy.PointMemCacheService;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.base.ObjectResponse;
import io.github.alphajiang.hyena.model.dto.PointLog;
import io.github.alphajiang.hyena.model.dto.PointRec;
import io.github.alphajiang.hyena.model.dto.PointRecLog;
import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
import io.github.alphajiang.hyena.model.param.*;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.SortOrder;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.utils.CollectionUtils;
import io.github.alphajiang.hyena.utils.DateUtils;
import io.github.alphajiang.hyena.utils.LoggerHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

@RestController
@Api(value = "积分相关的接口", tags = "积分")
@RequestMapping("/hyena/point")
public class PointController {

    private static final Logger logger = LoggerFactory.getLogger(PointController.class);

    @Autowired
    private PointUsageFacade pointUsageFacade;

    @Autowired
    private PointDs pointDs;

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private PointRecLogDs pointRecLogDs;

    @Autowired
    private PointMemCacheService pointMemCacheService;

    @ApiOperation(value = "获取积分信息")
    @GetMapping(value = "/getPoint", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointPo> getPoint(
            HttpServletRequest request,
            @ApiParam(value = "积分类型", example = "score") @RequestParam(defaultValue = "default") String type,
            @ApiParam(value = "用户ID") @RequestParam String uid) {
        logger.info(LoggerHelper.formatEnterLog(request));
        var ret = this.pointMemCacheService.getPoint(type, uid, false);
        ObjectResponse<PointPo> res = new ObjectResponse<>(ret.getPointCache().getPoint());
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }


    @ApiOperation(value = "获取积分列表")
    @PostMapping(value = "/listPoint", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ListResponse<PointPo> listPoint(HttpServletRequest request,
                                           @RequestBody ListPointParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);

        param.setSorts(List.of(SortParam.as("pt.id", SortOrder.desc)));
        var res = this.pointDs.listPoint4Page(param);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }


    @ApiOperation(value = "获取变更明细列表")
    @PostMapping(value = "/listPointLog", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ListResponse<PointLog> listPointLog(
            HttpServletRequest request,
            @RequestBody ListPointLogParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
        if (CollectionUtils.isEmpty(param.getSorts())) {
            param.setSorts(List.of(SortParam.as("log.id", SortOrder.desc)));
        }
        var res = this.pointLogDs.listPointLog4Page(param);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @ApiOperation(value = "获取记录列表")
    @GetMapping(value = "/listPointRecord", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ListResponse<PointRec> listPointRecord(
            HttpServletRequest request,
            @ApiParam(value = "积分类型", example = "score") @RequestParam(defaultValue = "default") String type,
            @ApiParam(value = "用户ID") @RequestParam(required = false) String uid,
            @ApiParam(value = "标签") @RequestParam(required = false) String tag,
            @RequestParam(required = false) Boolean enable,
            @ApiParam(value = "请求记录的开始") @RequestParam(defaultValue = "0") long start,
            @ApiParam(value = "请求记录数量") @RequestParam(defaultValue = "10") int size) {
        logger.info(LoggerHelper.formatEnterLog(request));

        ListPointRecParam param = new ListPointRecParam();
        param.setUid(uid).setTag(tag);
        param.setEnable(enable).setSorts(List.of(SortParam.as("rec.id", SortOrder.desc)))
                .setStart(start).setSize(size);
        var res = this.pointRecDs.listPointRec4Page(type, param);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @ApiOperation(value = "获取记录历史明细列表")
    @GetMapping(value = "/listPointRecordLog", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ListResponse<PointRecLog> listPointRecordLog(
            HttpServletRequest request,
            @ApiParam(value = "积分类型", example = "score") @RequestParam(defaultValue = "default") String type,
            @ApiParam(value = "用户ID") @RequestParam(required = false) String uid,
            @ApiParam(value = "变更流水号") @RequestParam(required = false) Long seqNum,
            @ApiParam(value = "recId") @RequestParam(defaultValue = "0") long recId,
            @ApiParam(value = "标签") @RequestParam(required = false) String tag,
            @RequestParam(required = false) Boolean enable,
            @ApiParam(value = "请求记录的开始") @RequestParam(defaultValue = "0") long start,
            @ApiParam(value = "请求记录数量") @RequestParam(defaultValue = "10") int size) {
        logger.info(LoggerHelper.formatEnterLog(request));

        ListPointRecLogParam param = new ListPointRecLogParam();
        param.setUid(uid).setRecId(recId).setTag(tag);
        if (seqNum != null) {
            param.setSeqNum(seqNum);
        }
        param.setEnable(enable).setSorts(List.of(SortParam.as("log.id", SortOrder.desc)))
                .setStart(start).setSize(size);
        var res = this.pointRecLogDs.listPointRecLog4Page(type, param);


        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @Idempotent(name = "increase-point")
    @ApiOperation(value = "增加用户积分")
    @PostMapping(value = "/increase", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointPo> increasePoint(HttpServletRequest request,
                                                 @RequestBody @NotNull PointIncreaseParam param) {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointIncreaseParam(param);
        PointPo ret = this.pointUsageFacade.increase(usage);
        ObjectResponse<PointPo> res = new ObjectResponse<>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        debugPerformance(request, startTime);
        return res;
    }

    @Idempotent(name = "decrease-point")
    @ApiOperation(value = "消费用户积分")
    @PostMapping(value = "/decrease", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointOpResult> decreasePoint(HttpServletRequest request,
                                                       @RequestBody PointOpParam param) {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointOpParam(param);
        PointOpResult ret = this.pointUsageFacade.decrease(usage);
        ObjectResponse<PointOpResult> res = new ObjectResponse<>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        debugPerformance(request, startTime);
        return res;
    }


    @Idempotent(name = "decreaseFrozen-point")
    @ApiOperation(value = "消费已冻结的用户积分")
    @PostMapping(value = "/decreaseFrozen", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointOpResult> decreaseFrozenPoint(HttpServletRequest request,
                                                             @RequestBody PointDecreaseParam param) {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointDecreaseParam(param);

        PointOpResult ret = this.pointUsageFacade.decreaseFrozen(usage);
        ObjectResponse<PointOpResult> res = new ObjectResponse<>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        debugPerformance(request, startTime);
        return res;
    }


    @Idempotent(name = "freeze-point")
    @ApiOperation(value = "冻结用户积分")
    @PostMapping(value = "/freeze", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointOpResult> freezePoint(HttpServletRequest request,
                                                     @RequestBody PointOpParam param) {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);

        PointUsage usage = PointUsageBuilder.fromPointOpParam(param);
        PointOpResult cusPoint = this.pointUsageFacade.freeze(usage);
        ObjectResponse<PointOpResult> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        debugPerformance(request, startTime);
        return res;
    }

    @Idempotent(name = "unfreeze-point")
    @ApiOperation(value = "解冻用户积分")
    @PostMapping(value = "/unfreeze", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointOpResult> unfreezePoint(HttpServletRequest request,
                                                       @RequestBody PointUnfreezeParam param) {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);

        PointUsage usage = PointUsageBuilder.fromPointUnfreezeParam(param);
        PointOpResult cusPoint = this.pointUsageFacade.unfreeze(usage);

        ObjectResponse<PointOpResult> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        debugPerformance(request, startTime);
        return res;
    }

    @Idempotent(name = "cancel-point")
    @ApiOperation(value = "撤销用户积分")
    @PostMapping(value = "/cancel", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointOpResult> cancelPoint(HttpServletRequest request,
                                                     @RequestBody PointCancelParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);

        PointUsage usage = PointUsageBuilder.fromPointCancelParam(param);
        PointOpResult cusPoint = this.pointUsageFacade.cancel(usage);

        ObjectResponse<PointOpResult> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @Idempotent(name = "freeze-cost")
    @ApiOperation(value = "按成本冻结")
    @PostMapping(value = "/freezeCost", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointOpResult> freezeCost(HttpServletRequest request,
                                                    @RequestBody PointFreezeParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
//        if (param.getUnfreezePoint() != null && param.getUnfreezePoint() < 0L) {
//            throw new HyenaParameterException("invalid parameter: unfreezePoint");
//        }
        PointUsage usage = PointUsageBuilder.fromPointFreezeParam(param);
        PointOpResult cusPoint = this.pointUsageFacade.freezeCost(usage);
        ObjectResponse<PointOpResult> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @Idempotent(name = "unfreeze-cost")
    @ApiOperation(value = "按成本解冻")
    @PostMapping(value = "/unfreezeCost", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointOpResult> unfreezeCost(HttpServletRequest request,
                                                      @RequestBody PointUnfreezeParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointUnfreezeParam(param);
        PointOpResult cusPoint = this.pointUsageFacade.unfreezeCost(usage);
        ObjectResponse<PointOpResult> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @Idempotent(name = "refund")
    @ApiOperation(value = "退款")
    @PostMapping(value = "/refund", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<PointOpResult> refund(HttpServletRequest request,
                                                @RequestBody PointRefundParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointRefundParam(param);
        usage.setUnfreezePoint(param.getUnfreezePoint());
        PointOpResult cusPoint = this.pointUsageFacade.refund(usage);
        ObjectResponse<PointOpResult> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }


    @ApiOperation(value = "获取时间段内总共增加的积分数量")
    @GetMapping(value = "/getIncreasedPoint", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ObjectResponse<Long> getIncreasedPoint(
            HttpServletRequest request,
            @ApiParam(value = "积分类型", example = "score") @RequestParam(defaultValue = "default") String type,
            @ApiParam(value = "用户ID") @RequestParam(required = false) String uid,
            @ApiParam(value = "开始时间", example = "2019-03-25 18:35:21") @RequestParam(required = false, value = "start") String strStart,
            @ApiParam(value = "结束时间", example = "2019-04-26 20:15:31") @RequestParam(required = false, value = "end") String strEnd) {
        logger.info(LoggerHelper.formatEnterLog(request));
        try {
            Calendar calStart = DateUtils.fromYyyyMmDdHhMmSs(strStart);
            Calendar calEnd = DateUtils.fromYyyyMmDdHhMmSs(strEnd);
            var ret = this.pointRecDs.getIncreasedPoint(type, uid, calStart.getTime(), calEnd.getTime());

            ObjectResponse<Long> res = new ObjectResponse<>(ret);
            logger.info(LoggerHelper.formatLeaveLog(request) + " ret = {}", ret);
            return res;
        } catch (ParseException e) {
            logger.warn(e.getMessage(), e);
            throw new HyenaParameterException("参数错误, 时间格式无法解析");
        }
    }

    @ApiOperation(value = "禁用帐号")
    @PostMapping(value = "/disableAccount", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse disableAccount(
            HttpServletRequest request,
            @ApiParam(value = "积分类型", example = "score") @RequestParam(defaultValue = "default") String type,
            @ApiParam(value = "用户ID") @RequestParam String uid) {
        logger.info(LoggerHelper.formatEnterLog(request));
        this.pointDs.disableAccount(type, uid);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return BaseResponse.success();
    }

    private void debugPerformance(HttpServletRequest request, long startTime) {
        long curTime = System.nanoTime();
        if (curTime - startTime > 2000L * 1000000) {
            logger.warn("延迟过大...{}. url = {}",
                    (curTime - startTime) / 1000000,
                    request.getRequestURI());
        }
    }
}
