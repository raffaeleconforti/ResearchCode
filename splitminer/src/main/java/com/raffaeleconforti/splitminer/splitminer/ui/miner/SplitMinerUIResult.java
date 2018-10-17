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

package com.raffaeleconforti.splitminer.splitminer.ui.miner;


import com.raffaeleconforti.splitminer.splitminer.ui.dfgp.DFGPUIResult;

/**
 * Created by Adriano on 29/02/2016.
 */
public class SplitMinerUIResult extends DFGPUIResult {

    public enum StructuringTime {NONE, POST, PRE}

    public static final StructuringTime STRUCT_POLICY = StructuringTime.NONE;
    private boolean replaceIORs;
    private boolean removeSelfLoops;

    private StructuringTime structuringTime;

    public SplitMinerUIResult() {
        structuringTime = STRUCT_POLICY;
        replaceIORs = true;
        removeSelfLoops = false;
    }

    public StructuringTime getStructuringTime() {
        return structuringTime;
    }

    public void setStructuringTime(StructuringTime structuringTime) {
        this.structuringTime = structuringTime;
    }

    public boolean isReplaceIORs() {
        return replaceIORs;
    }

    public void setReplaceIORs(boolean replaceIORs) {
        this.replaceIORs = replaceIORs;
    }

    public boolean isRemoveSelfLoops() {
        return removeSelfLoops;
    }

    public void setRemoveSelfLoops(boolean removeSelfLoops) {
        this.removeSelfLoops = removeSelfLoops;
    }

}
