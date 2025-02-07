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
import io.github.alphajiang.hyena.ds.service.PointRecLogDs;
import io.github.alphajiang.hyena.model.po.PointRecLogPo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class TestPointRecLogDs extends HyenaTestBase {

    @Autowired
    private PointRecLogDs pointRecLogDs;

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    public void test_batchInsert() {
        List<PointRecLogPo> logs = new ArrayList<>();
        PointRecLogPo log1 = new PointRecLogPo();
        log1.setUsed(123L);
        logs.add(log1);
        this.pointRecLogDs.batchInsert(super.getPointType(), logs);


        PointRecLogPo log2 = new PointRecLogPo();
        log2.setUsed(456L);
        logs.add(log2);
        this.pointRecLogDs.batchInsert(super.getPointType(), logs);
    }

    @Test
    public void test_addPointRecLog() {
        PointRecLogPo log1 = new PointRecLogPo();
        log1.setUsed(111L);

        this.pointRecLogDs.addPointRecLog(super.getPointType(), log1);
    }
}
