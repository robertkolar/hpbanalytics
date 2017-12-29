package com.highpowerbear.hpbanalytics.iblogger;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robertk on 12/28/2017.
 */
@Component
public class PositionRepository {

    private List<Position> positions = new ArrayList<>();

    public void clearPositions() {
        positions.clear();
    }

    public void addPosition(Position position) {
        positions.add(position);
    }

    public List<Position> getPositions() {
        return positions;
    }
}
