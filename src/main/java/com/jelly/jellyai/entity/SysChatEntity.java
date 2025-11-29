package com.jelly.jellyai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysChatEntity extends ChatBase{
    private String name;
    private String type;

}
