package com.highpowerbear.hpbanalytics.riskmgt;

import com.highpowerbear.hpbanalytics.iblogger.dto.IbPosition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robertk on 12/27/2017.
 */
@Component
public class PositionRepository {

    private List<IbPosition> positions = new ArrayList<>();

    public List<IbPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<IbPosition> positions) {
        this.positions = positions;
    }
}
