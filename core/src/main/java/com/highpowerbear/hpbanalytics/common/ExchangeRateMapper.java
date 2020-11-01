package com.highpowerbear.hpbanalytics.common;

import com.highpowerbear.shared.ExchangeRateDTO;
import com.highpowerbear.hpbanalytics.database.ExchangeRate;
import org.mapstruct.Mapper;

/**
 * Created by robertk on 11/1/2020.
 */
@Mapper(componentModel = "spring")
public interface ExchangeRateMapper {

    ExchangeRateDTO entityToDto(ExchangeRate entity);
}
