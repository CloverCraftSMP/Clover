package com.clovercraftsmp.clover.duck;

import com.clovercraftsmp.clover.util.filter.Filter;
import org.jetbrains.annotations.Nullable;

public interface FilterDuck {
    void clover$setFilter(Filter filter);
    @Nullable Filter clover$getFilter();
}