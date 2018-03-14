package com.raffaeleconforti.bpmnminer.prom.preprocessing.synchtracegeneration.ui;

import com.raffaeleconforti.foreignkeydiscovery.conceptualmodels.Entity;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class TraceGeneration_UI_subprocesses extends ProMPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_NAME = "Select Entities to Become SubProcesses";

    private List<Entity> data;
    private int chosenArtifactsIndex[];

	public TraceGeneration_UI_subprocesses(List<Entity> data, int chosenArtifactsIndex[]) {
		super(DIALOG_NAME);
		this.data = data;
		this.chosenArtifactsIndex = chosenArtifactsIndex;
	}

    public TraceGeneration_UI_subprocesses(List<Entity> data2, List<Entity> data, boolean all) {
        super(DIALOG_NAME);
        this.data = data;

		for (Entity currentData : data2) {
			JComponent box;
			box = addTextField(currentData.getName(), "subProcess");
			((ProMTextField) box).setEditable(false);
			// resize the combobox and the label for a better layout
			box.setPreferredSize(new Dimension(500, 30));
			for (Component c : box.getParent().getComponents()) {
				if (c instanceof JLabel) {
					c.setPreferredSize(new Dimension(500, 30));
				}
			}
		}

		chosenArtifactsIndex = new int[data.size()];

		int dataIndex = 0;
		// create a new property item for each event type
		for (Entity currentData : data) {

			chosenArtifactsIndex[dataIndex] = 0;

			// build a list of all keys (sets of attribute names)
			String[] keyList = new String[2];
			keyList[0] = "subProcess";
			keyList[1] = "not subProcess";

			// and create a combobox having all keys as an available option
			JComponent box;
			//if (keyList.length > 1) {
			box = addComboBox(currentData.getName(), keyList);
			((JComboBox) box).addActionListener(new UpdateDataListener(dataIndex));

			// resize the combobox and the label for a better layout
			box.setPreferredSize(new Dimension(500, 30));
			for (Component c : box.getParent().getComponents()) {
				if (c instanceof JLabel) {
					c.setPreferredSize(new Dimension(500, 30));
				}
			}
			// all available attributes of an event type are rendered in a tooltip
			//box.setToolTipText("All available attributes:\n" + attributesToToolTipString(currentData.attributes)); ???
			dataIndex++;
		}
    }

    /**
     * @param set
     * @return string to render list of attributes in a tooltip
     */
    protected static String attributesToToolTipString(String set[]) {
        StringBuilder keyString = new StringBuilder("(");
        for (int i = 0; i < set.length; i++) {
            keyString.append(set[i]).append(i == set.length - 1 ? ") " : ",\n");
        }
        return keyString.toString();
    }

    /**
     * Accumlate the selection and return a mapping from each primary key to the
     * set of event names which have this primary key.
     *
     * @return
     */
    //public Map< Set<String>, Set <String> > getSelection() {
    //	Map< Set<String>, Set <String> > group = new UnifiedMap< Set<String>, Set <String> > ();

    //	for (int dataIndex=0; dataIndex < data.size(); dataIndex++) {
    //		Set<String> primaryKey = data.get(dataIndex).primaryKeys[chosenArtifactsIndex[dataIndex]];
    //		if(!group.containsKey(primaryKey)) {
    //			group.put(primaryKey, new UnifiedSet<String>());
    //		}
    //		group.get(primaryKey).add(data.get(dataIndex).name);
    //	}

    //	return group;
    //}
    public List<Entity> getSelection() {
        List<Entity> selected = new ArrayList<Entity>();
        for (int dataIndex = 0; dataIndex < data.size(); dataIndex++) {
            if (chosenArtifactsIndex[dataIndex] == 0)
                selected.add(data.get(dataIndex));
        }
        return selected;
    }

    /**
     * Listener to copy the selection from the comboboxes to a readable data-structure.
     *
     * @author dfahland
     */
    private class UpdateDataListener implements ActionListener {

        private int index;

        public UpdateDataListener(int index) {
            this.index = index;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getID() == ActionEvent.ACTION_PERFORMED
                    && e.getSource() instanceof JComboBox) {
                chosenArtifactsIndex[index] = ((JComboBox) e.getSource()).getSelectedIndex();
            }
        }
    }


}
