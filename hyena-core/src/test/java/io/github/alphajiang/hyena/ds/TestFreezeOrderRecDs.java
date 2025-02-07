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

package io.github.alphajiang.hyena.ds;

import io.github.alphajiang.hyena.HyenaTestBase;
import io.github.alphajiang.hyena.biz.point.PointBuilder;
import io.github.alphajiang.hyena.ds.service.FreezeOrderRecDs;
import io.github.alphajiang.hyena.model.po.FreezeOrderRecPo;
import io.github.alphajiang.hyena.model.po.PointRecPo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class TestFreezeOrderRecDs extends HyenaTestBase {
    @Autowired
    private FreezeOrderRecDs freezeOrderRecDs;

    @Autowired
    private PointBuilder pointBuilder;

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    public void test_closeByIdList() {
        String orderNo = UUID.randomUUID().toString();
        List<FreezeOrderRecPo> foList = new ArrayList<>();
        for (long i = 0L; i < 5L; i++) {
            PointRecPo rec = new PointRecPo();
            rec.setId(i + 1);
            FreezeOrderRecPo fo = pointBuilder.buildFreezeOrderRec(super.getUserPoint(),
                    rec, null, orderNo, i + 1, i);
            foList.add(fo);
        }
        this.freezeOrderRecDs.batchInsert(super.getPointType(), foList);

        List<FreezeOrderRecPo> list = this.freezeOrderRecDs.getFreezeOrderRecList(super.getPointType(),
                super.getUserPoint().getId(), null, orderNo);
        List<Long> idList = list.stream().map(FreezeOrderRecPo::getId).collect(Collectors.toList());
        this.freezeOrderRecDs.closeByIdList(super.getPointType(), idList);
    }
}
