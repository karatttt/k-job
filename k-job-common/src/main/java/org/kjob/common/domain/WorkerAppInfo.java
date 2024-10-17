package org.kjob.common.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor

public class WorkerAppInfo implements Serializable {

    /**
     * 应用唯一 ID
     */
    private Long appId;
}