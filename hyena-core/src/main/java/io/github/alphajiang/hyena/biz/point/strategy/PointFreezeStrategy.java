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

package io.github.alphajiang.hyena.biz.point.strategy;

import io.github.alphajiang.hyena.biz.calculator.CostCalculator;
import io.github.alphajiang.hyena.biz.calculator.PointRecCalculator;
import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.biz.point.PointBuilder;
import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.PointLogDs;
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.exception.HyenaNoPointException;
import io.github.alphajiang.hyena.model.po.*;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.model.vo.PointRecCalcResult;
import io.github.alphajiang.hyena.model.vo.PointVo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class PointFreezeStrategy extends AbstractPointStrategy {

    @Autowired
    private PointDs pointDs;

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecCalculator pointRecCalculator;

    @Autowired
    private PointRecLogDs pointRecLogDs;

    @Autowired
    private PointFlowService pointFlowService;


    @Autowired
    private CostCalculator costCalculator;

    @Autowired
    private PointBuilder pointBuilder;


    @Override
    public CalcType getType() {
        return CalcType.FREEZE;
    }

    @Override
    public PointOpResult processPoint(PointUsage usage, PointCache pointCache) {
        PointVo curPoint = pointCache.getPoint();
        if (curPoint.getAvailable() < usage.getPoint()) {
            log.warn("no enough available point. usage = {}, curPoint = {}", usage, curPoint);
            throw new HyenaNoPointException("no enough available point", Level.WARN);
        }

        curPoint.setSeqNum(curPoint.getSeqNum() + 1)
                .setAvailable(curPoint.getAvailable() - usage.getPoint())
                .setFrozen(curPoint.getFrozen() + usage.getPoint());
        PointPo point2Update = new PointPo();
        point2Update.setAvailable(curPoint.getAvailable())
                .setFrozen(curPoint.getFrozen()).setSeqNum(curPoint.getSeqNum())
                .setId(curPoint.getId());


        PointLogPo pointLog = this.pointBuilder.buildPointLog(PointOpType.FREEZE, usage, curPoint);
        long gap = usage.getPoint();
        long cost = 0L;
        List<PointRecLogPo> recLogs = new ArrayList<>();


        LoopResult recLogsRet = this.freezePointLoop(usage, pointCache,
                pointLog, gap);
        gap = gap - recLogsRet.getDelta();
        cost = cost + recLogsRet.getDeltaCost();
        recLogs.addAll(recLogsRet.getRecLogs());
        log.debug("gap = {}", gap);


        if (gap != 0L) {
            log.warn("no enough available point! gap = {}", gap);
        }
        if (cost > 0L) {
            pointLog.setDeltaCost(cost).setFrozenCost(pointLog.getFrozenCost() + cost);
            curPoint.setFrozenCost(curPoint.getFrozenCost() + cost);
            point2Update.setFrozenCost(curPoint.getFrozenCost());
        }

        pointFlowService.updatePoint(usage.getType(), point2Update);

        pointFlowService.updatePointRec(usage.getType(), recLogsRet.getRecList4Update());
        pointFlowService.addFreezeOrderRec(usage.getType(), recLogsRet.getForList());
        pointFlowService.addFlow(usage, pointLog, recLogs);

        pointCache.setUpdateTime(new Date());
        //return curPoint;
        PointOpResult ret = new PointOpResult();
        BeanUtils.copyProperties(curPoint, ret);
        ret.setOpPoint(recLogsRet.getDelta())
                .setOpCost(recLogsRet.getDeltaCost());
        return ret;
    }


    private LoopResult freezePointLoop(PointUsage usage, PointCache pointCache,
                                       PointLogPo pointLog, long expected) {
        log.info("freeze. type = {}, uid = {}, expected = {}",
                usage.getType(), pointCache.getPoint().getUid(), expected);

        LoopResult result = new LoopResult();
        long sum = 0L;
        long deltaCost = 0L;
        List<PointRecPo> recList4Update = new ArrayList<>();
        List<PointRecLogPo> recLogs = new ArrayList<>();
        List<FreezeOrderRecPo> forList = new ArrayList<>();
        for (PointRecPo rec : pointCache.getPoint().getRecList()) {
            long gap = expected - sum;
            if (gap < 1L) {
                log.warn("gap = {} !!!", gap);
                break;
            } else if (rec.getAvailable() < 1L) {
                // do nothing
            } else if (rec.getAvailable() < gap) {
                sum += rec.getAvailable();
                long delta = rec.getAvailable();
                PointRecCalcResult calcResult = this.pointRecCalculator.freezePoint(rec, delta);
                recList4Update.add(calcResult.getRec4Update());
                deltaCost += calcResult.getDeltaCost();
                FreezeOrderRecPo fo = pointBuilder.buildFreezeOrderRec(pointCache.getPoint(),
                        rec, usage.getOrderType(), usage.getOrderNo(), delta, deltaCost);
                forList.add(fo);
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, delta, calcResult.getDeltaCost());
                recLogs.add(recLog);
            } else {
                sum += gap;
                PointRecCalcResult calcResult = this.pointRecCalculator.freezePoint(rec, gap);
                recList4Update.add(calcResult.getRec4Update());
                deltaCost += calcResult.getDeltaCost();
                FreezeOrderRecPo fo = pointBuilder.buildFreezeOrderRec(pointCache.getPoint(),
                        rec, usage.getOrderType(), usage.getOrderNo(), gap, deltaCost);
                forList.add(fo);
                var recLog = this.pointBuilder.buildRecLog(rec, pointLog, gap, calcResult.getDeltaCost());
                recLogs.add(recLog);
                break;
            }
        }
        result.setDelta(sum).setDeltaCost(deltaCost)
                .setRecList4Update(recList4Update)
                .setRecLogs(recLogs)
                .setForList(forList);
        log.debug("result = {}", result);
        return result;
    }


}
