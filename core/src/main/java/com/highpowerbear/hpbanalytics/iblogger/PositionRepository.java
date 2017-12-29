package com.highpowerbear.hpbanalytics.iblogger;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by robertk on 12/28/2017.
 */
@Component
public class PositionRepository {

    private Map<String, List<PositionVO>> positionMap = new HashMap<>();

    public void initPositions(String accountId) {
        positionMap.put(accountId, new ArrayList<>());
    }

    public void addPosition(String accountId, PositionVO position) {
        positionMap.get(accountId).add(position);
    }

    public List<PositionVO> getPositions(String accountId) {
        return positionMap.get(accountId);
    }
}
