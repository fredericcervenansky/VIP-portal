/* Copyright CNRS-CREATIS
 *
 * Rafael Ferreira da Silva
 * rafael.silva@creatis.insa-lyon.fr
 * http://www.rafaelsilva.com
 *
 * This software is a grid-enabled data-driven workflow manager and editor.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.insalyon.creatis.vip.gatelab.client.view.launch;

import com.smartgwt.client.widgets.form.fields.PickerIcon;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.FormItemClickHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemIconClickEvent;
import fr.insalyon.creatis.vip.application.client.view.common.AbstractSourceLayout;
import fr.insalyon.creatis.vip.core.client.view.util.FieldUtil;
import fr.insalyon.creatis.vip.datamanager.client.view.selection.PathSelectionWindow;
import java.util.LinkedHashMap;

/**
 *
 * @author Rafael Ferreira da Silva, Sorina Camarasu
 */
public class GateLabSourceLayout extends AbstractSourceLayout {

    private boolean isSelectItem;
    private SelectItem selectItem;
    private TextItem textItem;

    public GateLabSourceLayout(String name, String comment) {

        super(name, comment);

        if (name.equalsIgnoreCase("CPUestimation")) {

            LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
            map.put("1", "A few minutes");
            map.put("2", "A few hours");
            map.put("3", "A few days");
            map.put("4", "More than a few days");

            configureSelectItem(map);

        } else if (name.equalsIgnoreCase("ParallelizationType")) {

            LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
            map.put("stat", "Static");
            map.put("dyn", "Dynamic");

            configureSelectItem(map);

        } else if (name.equalsIgnoreCase("GateRelease")) {

            configureTextItem();
            textItem.setValue("/vip/GateLab (group)/releases/current_gate_release.tgz");

            PickerIcon browsePicker = new PickerIcon(PickerIcon.SEARCH, new FormItemClickHandler() {
                public void onFormItemClick(FormItemIconClickEvent event) {
                    new PathSelectionWindow(textItem).show();
                }
            });
            browsePicker.setPrompt("Browse on the Grid");
            textItem.setIcons(browsePicker);

        } else if (name.equalsIgnoreCase("GateInput") || name.equalsIgnoreCase("NumberOfParticles")) {

            configureTextItem();
            textItem.setDisabled(true);
        }
    }

    /**
     *
     * @param map
     */
    private void configureSelectItem(LinkedHashMap<String, String> map) {

        selectItem = new SelectItem();
        selectItem.setValueMap(map);
        selectItem.setShowTitle(false);
        selectItem.setWidth(200);
        selectItem.setRequired(true);
        this.addMember(FieldUtil.getForm(selectItem));
        isSelectItem = true;
    }

    private void configureTextItem() {

        textItem = FieldUtil.getTextItem(400, false, "", null);
        this.addMember(FieldUtil.getForm(textItem));
        isSelectItem = false;
    }

    @Override
    public String getValue() {

        return isSelectItem ? selectItem.getValueAsString() : textItem.getValueAsString();
    }

    @Override
    public void setValue(String value) {

        if (isSelectItem) {
            selectItem.setValue(value);
            if (name.equalsIgnoreCase("ParallelizationType") && value.equals("stat")) {
                this.selectItem.setDisabled(true);
            }
        } else {
            textItem.setValue(value);
        }
    }

    @Override
    public boolean validate() {

        return isSelectItem ? selectItem.validate() : textItem.validate();
    }
}