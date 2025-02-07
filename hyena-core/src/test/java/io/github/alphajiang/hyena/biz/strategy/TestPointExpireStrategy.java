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

package io.github.alphajiang.hyena.biz.strategy;

import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.strategy.PointStrategy;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.model.dto.PointRec;
import io.github.alphajiang.hyena.model.param.ListPointRecParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TestPointExpireStrategy extends TestPointStrategyBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointExpireStrategy.class);


    @Autowired
    private PointStrategy pointExpireStrategy;

    @Autowired
    private PointRecDs pointRecDs;

    @Test
    public void test_expirePoint() throws InterruptedException {
        ListPointRecParam param = new ListPointRecParam();
        param.setUid(super.uid).setStart(0L).setSize(1);
        Thread.sleep(100L);
        List<PointRec> recList = this.pointRecDs.listPointRec(super.getPointType(), param);
        PointRec rec = recList.get(0);

        long number = rec.getAvailable();
        long resultAvailable = this.point.getPoint() - number;
        PointUsage usage = new PointUsage();
        usage.setType(super.getPointType()).setRecId(rec.getId())
                .setUid(this.uid).setPoint(number).setNote("test_expirePoint");
        PointPo result = this.pointExpireStrategy.process(usage);
        logger.info("result = {}", result);
        Assertions.assertEquals(this.point.getPoint().longValue() - number, result.getPoint().longValue());
        Assertions.assertEquals(resultAvailable, result.getAvailable().longValue());
        Assertions.assertEquals(0L, result.getUsed().longValue());
        Assertions.assertEquals(0L, result.getFrozen().longValue());
        Assertions.assertEquals(number, result.getExpire().longValue());

        Thread.sleep(100L);
        PointRecPo resultRec = this.pointRecDs.getById(super.getPointType(), rec.getId(), false);
        logger.info("resultRec = {}", resultRec);
        Assertions.assertFalse(resultRec.getEnable());
        Assertions.assertTrue(resultRec.getAvailable().longValue() == 0L);
        Assertions.assertTrue(resultRec.getExpire().longValue() == number);
    }
}
