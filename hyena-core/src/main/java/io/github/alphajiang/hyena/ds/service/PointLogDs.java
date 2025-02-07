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

package io.github.alphajiang.hyena.ds.service;

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.ds.mapper.PointLogMapper;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.dto.PointLog;
import io.github.alphajiang.hyena.model.param.ListPointLogParam;
import io.github.alphajiang.hyena.model.po.PointLogPo;
import io.github.alphajiang.hyena.utils.HyenaAssert;
import io.github.alphajiang.hyena.utils.TableNameHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;

@Service
public class PointLogDs {
    private static final Logger logger = LoggerFactory.getLogger(PointLogDs.class);

    @Autowired
    private PointLogMapper pointLogMapper;

    @Autowired
    private PointTableDs pointTableDs;




    public void addPointLog(@NonNull String type, @NotNull PointLogPo pointLog) {
        String tableName = TableNameHelper.getPointTableName(type);

        this.pointLogMapper.addPointLog(tableName, pointLog);
    }

    public void batchInsert(@NonNull String type, @NotNull List<PointLogPo> pointLogList) {
        String tableName = TableNameHelper.getPointTableName(type);
        this.pointLogMapper.batchInsert(tableName, pointLogList);
    }


    @Transactional
    public ListResponse<PointLog> listPointLog4Page(ListPointLogParam param) {
        logger.debug("param = {}", param);
        var list = this.listPointLog(param);
        var total = this.countPointLog(param);
        var ret = new ListResponse<>(list, total);
        return ret;
    }

    public List<PointLog> listPointLog(ListPointLogParam param) {
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
        HyenaAssert.isTrue(pointTableDs.isTableExists(pointTableName), HyenaConstants.RES_CODE_PARAMETER_ERROR,
                "type not exist");
        return this.pointLogMapper.listPointLog(pointTableName, param);
    }

    public long countPointLog(ListPointLogParam param) {
        String pointTableName = TableNameHelper.getPointTableName(param.getType());
        Long ret = this.pointLogMapper.countPointLog(pointTableName, param);
        return ret == null ? 0L : ret.longValue();
    }
}
