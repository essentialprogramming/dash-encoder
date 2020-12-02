package com.dash.encoder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class SegmentationResult<T> {
    private T period;
    private Long totalSize;
}
