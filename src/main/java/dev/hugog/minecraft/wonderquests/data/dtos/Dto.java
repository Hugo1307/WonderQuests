package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.DataModel;

public interface Dto<T extends DataModel<?>> {

  T toModel();

}
