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

package io.github.alphajiang.hyena.model.po;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.alphajiang.hyena.model.base.BasePo;
import io.github.alphajiang.hyena.utils.JacksonStringDeserialize;
import io.github.alphajiang.hyena.utils.JacksonStringSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PointLogPo extends BasePo {

    private long pid;
    private String uid;
    private long seqNum;
    private Long delta;
    @ApiModelProperty("变动部分的实际成本")
    private Long deltaCost;
    private Long point;
    private Long available;
    private Long used;
    private Long frozen;
    private Long refund;
    private Long expire;
    @ApiModelProperty("变动后,实际成本")
    private Long cost;
    @ApiModelProperty("变动后,冻结的成本")
    private Long frozenCost;
    /**
     * PointStatus
     */
    private Integer type;
    private String tag;
    private String orderNo;

    private Integer sourceType;
    private Integer orderType;
    private Integer payType;
    @JsonSerialize(using = JacksonStringSerialize.class)
    @JsonDeserialize(using = JacksonStringDeserialize.class)
    private String extra;
    private String note;


}
