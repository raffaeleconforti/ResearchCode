/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.raffaeleconforti.noisefiltering.timestamp.noise.selection;

/**
 * Created by conforti on 26/02/15.
 */
public class TimeStampNoiseResult {

    Double percentageTraces;
    Double percentageUniqueTraces;
    Double percentageEvents;

    Double totalGaps;
    Double minGapLength;
    Double maxGapLength;
    Double averageGapLength;
    Double minGapNumber;
    Double maxGapNumber;
    Double averageGapNumber;

    public Double getPercentageTraces() {
        return percentageTraces;
    }

    public void setPercentageTraces(Double percentageTraces) {
        this.percentageTraces = percentageTraces;
    }

    public Double getPercentageUniqueTraces() {
        return percentageUniqueTraces;
    }

    public void setPercentageUniqueTraces(Double percentageUniqueTraces) {
        this.percentageUniqueTraces = percentageUniqueTraces;
    }

    public Double getPercentageEvents() {
        return percentageEvents;
    }

    public void setPercentageEvents(Double percentageEvents) {
        this.percentageEvents = percentageEvents;
    }

    public Double getTotalGaps() {
        return totalGaps;
    }

    public void setTotalGaps(Double totalGaps) {
        this.totalGaps = totalGaps;
    }

    public Double getMinGapLength() {
        return minGapLength;
    }

    public void setMinGapLength(Double minGapLength) {
        this.minGapLength = minGapLength;
    }

    public Double getMaxGapLength() {
        return maxGapLength;
    }

    public void setMaxGapLength(Double maxGapLength) {
        this.maxGapLength = maxGapLength;
    }

    public Double getAverageGapLength() {
        return averageGapLength;
    }

    public void setAverageGapLength(Double averageGapLength) {
        this.averageGapLength = averageGapLength;
    }

    public Double getMinGapNumber() {
        return minGapNumber;
    }

    public void setMinGapNumber(Double minGapNumber) {
        this.minGapNumber = minGapNumber;
    }

    public Double getMaxGapNumber() {
        return maxGapNumber;
    }

    public void setMaxGapNumber(Double maxGapNumber) {
        this.maxGapNumber = maxGapNumber;
    }

    public Double getAverageGapNumber() {
        return averageGapNumber;
    }

    public void setAverageGapNumber(Double averageGapNumber) {
        this.averageGapNumber = averageGapNumber;
    }
}
