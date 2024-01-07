package dev.hugog.minecraft.wonderquests.data.models;

import dev.hugog.minecraft.wonderquests.data.dtos.Dto;

public interface DataModel<T extends Dto<?>> {

  T toDto();

}
