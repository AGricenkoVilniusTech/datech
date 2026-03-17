package com.datech.mvp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CategoryCreateRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @Pattern(regexp = "income|expense")
    private String type;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    private String color;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
