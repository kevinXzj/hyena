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

import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.ds.service.PointLogService;
import io.github.alphajiang.hyena.ds.service.PointRecLogService;
import io.github.alphajiang.hyena.ds.service.PointRecService;
import io.github.alphajiang.hyena.ds.service.PointService;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.type.CalcType;
import io.github.alphajiang.hyena.model.type.PointStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 增加积分
 */
@Component
public class PointIncreaseStrategy extends AbstractPointStrategy {
    private static final Logger logger = LoggerFactory.getLogger(PointIncreaseStrategy.class);

    @Autowired
    private PointService pointService;

    @Autowired
    private PointLogService pointLogService;

    @Autowired
    private PointRecService pointRecService;

    @Autowired
    private PointRecLogService pointRecLogService;

    @Override
    public CalcType getType() {
        return CalcType.INCREASE;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public PointPo process(PointUsage usage) {
        logger.info("increase. usage = {}", usage);
        super.preProcess(usage);
        var cusPoint = this.pointService.getCusPoint(usage.getType(), usage.getUid(), true);
        if (cusPoint == null) {
            this.pointService.addPoint(usage.getType(), usage.getUid(), usage.getPoint());
        } else {
            var point2Update = new PointPo();
            point2Update.setPoint(cusPoint.getPoint() + usage.getPoint())
                    .setAvailable(cusPoint.getAvailable() + usage.getPoint())
                    .setId(cusPoint.getId());
            this.pointService.update(usage.getType(), point2Update);
        }
        cusPoint = this.pointService.getCusPoint(usage.getType(), usage.getUid(), false);
        var pointRec = this.pointRecService.addPointRec(usage, cusPoint.getId());
        var recLog = this.pointRecLogService.addLogByRec(usage.getType(), PointStatus.INCREASE,
                pointRec, usage.getPoint(), usage.getNote());
        var recLogs = List.of(recLog);
        this.pointLogService.addPointLog(usage.getType(), cusPoint, usage.getPoint(),
                usage.getTag(), usage.getExtra(), recLogs);
        return cusPoint;
    }
}
