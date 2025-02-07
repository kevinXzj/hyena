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

package io.github.alphajiang.hyena.biz.point;

import io.github.alphajiang.hyena.model.param.*;
import io.github.alphajiang.hyena.utils.JsonUtils;

public class PointUsageBuilder {

    public static PointUsage fromPointOpParam(PointOpParam param) {
        PointUsage usage = new PointUsage();
        usage.setType(param.getType()).setUid(param.getUid())
                .setName(param.getName())
                .setPoint(param.getPoint())
                .setTag(param.getTag())
                .setOrderNo(param.getOrderNo())
                .setSourceType(param.getSourceType())
                .setOrderType(param.getOrderType())
                .setPayType(param.getPayType())
                .setExtra(JsonUtils.toJsonString(param.getExtra()))
                .setNote(param.getNote());
        return usage;
    }

    public static PointUsage fromPointIncreaseParam(PointIncreaseParam param) {
        PointUsage usage = new PointUsage();
        usage.setType(param.getType()).setUid(param.getUid())
                .setName(param.getName())
                .setPoint(param.getPoint())
                .setCost(param.getCost())
                .setTag(param.getTag())
                .setOrderNo(param.getOrderNo())
                .setIssueTime(param.getIssueTime())
                .setSourceType(param.getSourceType())
                .setOrderType(param.getOrderType())
                .setPayType(param.getPayType())
                .setExtra(JsonUtils.toJsonString(param.getExtra()))
                .setNote(param.getNote()).setExpireTime(param.getExpireTime());
        return usage;
    }

    public static PointUsage fromPointFreezeParam(PointFreezeParam param) {
        PointUsage usage = PointUsageBuilder.fromPointOpParam(param);
        usage.setCost(param.getCost());
        return usage;
    }

    public static PointUsage fromPointUnfreezeParam(PointUnfreezeParam param) {
        PointUsage usage = PointUsageBuilder.fromPointOpParam(param);
        usage.setUnfreezeByOrderNo(param.getUnfreezeByOrderNo());
        return usage;
    }

    public static PointUsage fromPointCancelParam(PointCancelParam param) {
        PointUsage usage = PointUsageBuilder.fromPointOpParam(param);
        usage.setRecId(param.getRecId());
        return usage;
    }

    public static PointUsage fromPointDecreaseParam(PointDecreaseParam param) {
        PointUsage usage = PointUsageBuilder.fromPointUnfreezeParam(param);
        usage.setUnfreezePoint(param.getUnfreezePoint());
        return usage;
    }

    public static PointUsage fromPointRefundParam(PointRefundParam param) {
        PointUsage usage = PointUsageBuilder.fromPointUnfreezeParam(param);
        usage.setCost(param.getCost()).setUnfreezePoint(param.getUnfreezePoint());
        return usage;
    }
}
