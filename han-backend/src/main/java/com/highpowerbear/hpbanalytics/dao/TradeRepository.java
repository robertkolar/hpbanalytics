package com.highpowerbear.hpbanalytics.dao;

import com.highpowerbear.hpbanalytics.entity.Trade;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by robertk on 11/18/2017.
 */
public interface TradeRepository extends CrudRepository<Trade, Long> {
}
