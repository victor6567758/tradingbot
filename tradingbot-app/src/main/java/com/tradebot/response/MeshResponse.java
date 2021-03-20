package com.tradebot.response;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class MeshResponse {

    private List<MeshEntry> mesh;
    private String symbol;
    private long dateTime;
}
