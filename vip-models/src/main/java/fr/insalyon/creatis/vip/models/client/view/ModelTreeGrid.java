/* Copyright CNRS-CREATIS
 *
 * Rafael Silva
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
package fr.insalyon.creatis.vip.models.client.view;


import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.DragDataAction;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;
import com.smartgwt.client.widgets.tree.TreeNode;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.form.fields.events.DoubleClickEvent;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemSeparator;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
//import com.smartgwt.client.widgets.toolbar.ToolStrip;
//import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.tree.events.*;
import fr.cnrs.i3s.neusemstore.vip.semantic.simulation.model.client.bean.*;
import fr.cnrs.i3s.neusemstore.vip.semantic.simulation.model.client.bean.PhysicalParametersLayer.PhysicalParameterType;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants;
import fr.insalyon.creatis.vip.models.client.ModelConstants;
import fr.insalyon.creatis.vip.models.client.rpc.ModelService;
import fr.insalyon.creatis.vip.models.client.rpc.ModelServiceAsync;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * /**
 *
 * @author Tristan Glatard
 */
public class ModelTreeGrid extends TreeGrid {

    public boolean editable = true; // To allow to delete timepoints and instant
    private Menu nodeMenu = null;
    private SimulationObjectModel model = null;
    private Logger logger = null;
    private int tpSelected = 0;
    private int insSelected = 0;
    private Tree modelTree = null;
    private boolean bmodif = false;
    private int ntp = 0;
    private ModelTreeNode mnode = null;
    private String objOnto = "";
    private String objLab = "";
    private String objName = "";
    private String objLayer = "";
    private HashMap<String, SimulationObjectModel.ObjectType> layerTypeMap = new HashMap<String, SimulationObjectModel.ObjectType>();
    private HashMap<String, PhysicalParameterType> lutTypeMap = new HashMap<String, PhysicalParameterType>();
    private ModelMenu mymenu = null;
    private ModelCreateDialog dg = null;
    private String zipfile = "";
    private ToolStrip displayToolStrip = null;
    private String associatedraw = "";
    private ModelToolStrip mts = null;
    private String nwName = "";
    private String oldName = "";
    private  MenuItem modelNameItem = null;
    private TreeGridField tfg = null;

    public ModelTreeGrid(final SimulationObjectModel model, boolean bFull) {
        super();

 
        layerTypeMap.put("Anatomy", SimulationObjectModel.ObjectType.anatomical);
        layerTypeMap.put("External agent", SimulationObjectModel.ObjectType.external_agent);
        layerTypeMap.put("Foreign body", SimulationObjectModel.ObjectType.foreign_body);
        layerTypeMap.put("Pathology", SimulationObjectModel.ObjectType.pathological);
        layerTypeMap.put("Geometry", SimulationObjectModel.ObjectType.geometrical);

        lutTypeMap.put("T1", PhysicalParameterType.T1);
        lutTypeMap.put("T2", PhysicalParameterType.T2);
        lutTypeMap.put("T2s", PhysicalParameterType.T2s);
        lutTypeMap.put("chemicalComposition", PhysicalParameterType.chemicalComposition);
        lutTypeMap.put("protonDensity", PhysicalParameterType.protonDensity);
        lutTypeMap.put("radioactivity", PhysicalParameterType.radioactivity);
        lutTypeMap.put("echogenicity", PhysicalParameterType.echogenicity);
        lutTypeMap.put("concentration", PhysicalParameterType.concentration);
        lutTypeMap.put("susceptibility", PhysicalParameterType.susceptibility);

        this.model = model;
        oldName = this.model.getModelName();
        mts = new ModelToolStrip();
        //init the tree grid
        logger = Logger.getLogger("ModelTree");
        logger.log(Level.SEVERE, "model tree creation");
        setLoadDataOnDemand(true);
        setWidth(700);
        setHeight(600);
        setCanEdit(true);
        setShowOpenIcons(false);
        setShowDropIcons(false);
        setAutoFetchData(true);
        setCanFreezeFields(true);
        setCanReparentNodes(false);
        setCanAcceptDroppedRecords(true);
        setCanDragRecordsOut(true);
        setDragDataAction(DragDataAction.COPY);

        tfg = new TreeGridField(model.getModelName());
         tfg.setCanEdit(true);
        MenuItem modelNameItem = new MenuItem();
        modelNameItem.setTitle("Change model name ");
        modelNameItem.setIcon(CoreConstants.ICON_EDIT);
        tfg.setCanSort(false);
     
        Menu tfgMenu = new Menu();
        tfgMenu.setItemChecked(modelNameItem);
        //tfg.setContextMenu(tfgMenu);

        tfg.setCellFormatter(new CellFormatter() {

            public String format(Object value, ListGridRecord record, int rowNum, int colNum) {
                if (model.getModelName() == "0_0") {
                    return "";
                } else {
                    logger.log(Level.SEVERE, "value" + value.toString() + "rowNum :" + String.valueOf(rowNum) + "colNum : " + String.valueOf(colNum) + "number :" + record.getAttribute("number"));
                    return record.getAttribute(model.getModelName());
                }
            }
        });


        if (bFull) {
            logger.log(Level.SEVERE, "model tree set fields");
            setFields(tfg);
            // loadEmpty();
            load(model);
        } else {
            logger.log(Level.SEVERE, "load an empty model");
            setFields(tfg);
            loadEmpty();
        }



        this.addSelectionChangedHandler(new SelectionChangedHandler() {

            public void onSelectionChanged(SelectionEvent event) {
                logger.log(Level.SEVERE, "c'est pas le bon");
                ModelTreeNode node = (ModelTreeNode) event.getSelectedRecord();

                int index = node.getAttributeAsInt("number");
                String name = node.getAttributeAsString(model.getModelName());
                if (name.contains("Timepoint")) {
                    tpSelected = index;
                    insSelected = 0;
                } else if (name.contains("Instant")) {
                    insSelected = index;
                    tpSelected = modelTree.getParent(node).getAttributeAsInt("number");
                } else {
                    while (node.getAttributeAsString(model.getModelName()).contains("Instant")) {
                        node = (ModelTreeNode) modelTree.getParent(node);
                    }
                    insSelected = node.getAttributeAsInt("number");
                    tpSelected = modelTree.getParent(node).getAttributeAsInt("number");
                }
                logger.log(Level.SEVERE, "tp : " + tpSelected + " ins : " + insSelected);
            }
        });
//             this.addDropHandler(new DropHandler() {
//
//            @Override
//            public void onDrop(DropEvent event) {
//        //           logger.log(Level.SEVERE, "node to add " + objName);
//                  //  objType = typeDropped(objName);
//                    //   addItem(0,0,0,"test.mhd","Brain","anatomical",10);
//            
//               ModelCreateDialog dg = new ModelCreateDialog(objType, 0,0);
//               dg.show();
//               logger.log(Level.SEVERE, "label =  " + String.valueOf(dg.getLabel()));
//                logger.log(Level.SEVERE, "layer =  " + dg.getLayer());
//                             logger.log(Level.SEVERE, "ontologie =  " + String.valueOf(dg.getOntoName()));
//             //  addItem(0,0,0,"test.mhd",dg.getOntoName(),dg.getLayer(),dg.getLabel());
//               event.cancel();
//           //   addItem(tpSelected,insSelected,objType,event.getSource().toString().getNodes()[0].getAttribute("FileName"), objOnto, Integer.parseInt(objLab));  
//            }
//             
//             });
//            
//             
        dg = new ModelCreateDialog(this);
        this.addFolderDropHandler(new FolderDropHandler() {

            @Override
            public void onFolderDrop(FolderDropEvent event) {
                String dropname = event.getNodes()[0].getAttribute("FileName");
                if(dropname.endsWith(".mhd"))
                {
                    ModelServiceAsync ms = ModelService.Util.getInstance();
                    final AsyncCallback<String> callback = new AsyncCallback<String>() {

                    public void onFailure(Throwable caught) {
                        SC.say("The raw file associated with this mhd file is not available.");
                    }

                    public void onSuccess(String result) {
                       associatedraw = result;
                        
                    }
                };
                ms.extractRaw(dropname, zipfile, callback);
                }
                else
                {}
                dg.addInfo(typeDropped(dropname), tpSelected, insSelected, event.getNodes()[0].getAttribute("FileName"));
                dg.show();
                event.cancel();

            }
        });

        nodeMenu = new ModelMenu();
        this.addNodeContextClickHandler(new NodeContextClickHandler() {

            public void onNodeContextClick(NodeContextClickEvent event) {
                mnode = (ModelTreeNode) event.getNode();
                ((ModelMenu) nodeMenu).setNode((ModelTreeNode) event.getNode());
            }
        });
        this.setContextMenu(nodeMenu);
  

    }

    protected MenuItem[] getHeaderContextMenuItems(final Integer fieldNum) {
       final MenuItem[] items = super.getHeaderContextMenuItems(fieldNum);
       MenuItem modelDescpItem = new MenuItem("Change model description");
       modelDescpItem.setIcon(CoreConstants.ICON_EDIT);
       modelDescpItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
           public void onClick(MenuItemClickEvent event) {
              setDescription();          
           }
       });
       modelNameItem = new MenuItem("Change model name");
       modelNameItem.setIcon(CoreConstants.ICON_EDIT);
       modelNameItem.setEnabled(true);
       modelNameItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
           public void onClick(MenuItemClickEvent event) {
                  setName();
           }
       });
       MenuItem[] newItems = new MenuItem[items.length + 3];
       for (int i = 0; i < items.length; i++) {
           MenuItem item = items[i];
           newItems[i] = item;
       }
       newItems[items.length] = new MenuItemSeparator();
       newItems[items.length+1] = modelDescpItem;
       newItems[items.length+2] = modelNameItem;
       return newItems;
    }
    
    private void setDescription()
    {
          ModelDescriptionWindow dg = new ModelDescriptionWindow(ModelTreeGrid.this);
          dg.show();
    }
    
        private void setName()
    {
          ModelNameWindow dg = new ModelNameWindow(ModelTreeGrid.this);
          dg.show();
    }
    
       public String getModelName()
       {    
           return nwName;
       }
       
       public void setModelName(String name)
       {    
           nwName = name;
           tfg.setPrompt("model name changed. You need to commit to record the modification.");
            tfg.setTitle(nwName);
            
            tfg.fireEvent(new DoubleClickEvent(this.getJsObj()));
            //tfg.fireEvent(new ItemClickEvent());
            bmodif = true;
           logger.log(Level.SEVERE,"NAME : " + nwName);
//          //this.removeData(modelTree);
//           modelTree.destroy();
//          this.setAttribute(oldName, nwName, true);
//          model.setModelName(nwName);
//            logger.log(Level.SEVERE,"NAME : " + nwName);
//          load(model);
//           
           redraw();
         }

    public void setObjName(String name) {
        objName = name;
    }

    public void setLab(String label) {
        objLab = label;
    }

    public void setOnto(String name) {
        objOnto = name;
    }

    public void setZipFile(String z)
    {
        zipfile = z;
        logger.log(Level.SEVERE,"zipfile :" + zipfile);
    }

    public void removeTimepoint()
    {
        ModelServiceAsync ms = ModelService.Util.getInstance();
        final AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

                public void onFailure(Throwable caught) {
                    SC.say("Cannot remove timepoint");
                }

                public void onSuccess(SimulationObjectModel result) {
                    model = result;
                   bmodif = true;
                   checkModality();
                   

                }
            };
            ms.removeTimePoint(model, Integer.parseInt(mnode.getAttribute("number")), callback);
    }
    
    public void removeInstant()
    {
       ModelServiceAsync ms = ModelService.Util.getInstance(); 
       final AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

                public void onFailure(Throwable caught) {
                    SC.say("Cannot remove instant");
                }

                public void onSuccess(SimulationObjectModel result) {
                    model = result;
                    bmodif = true;
                    checkModality();
                }
            };
            ms.removeInstant(model, Integer.parseInt(modelTree.getParent(mnode).getAttribute("number")),
                    Integer.parseInt(mnode.getAttribute("number")), callback);
    }
    
    public void removeObjectLayer()
    {
        ModelServiceAsync ms = ModelService.Util.getInstance();
            final AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

                public void onFailure(Throwable caught) {
                    SC.say("Cannot remove Layer");
                }

                public void onSuccess(SimulationObjectModel result) {
                    model = result;
                         bmodif = true;
                         checkModality();
                }
            };
            int ins = Integer.parseInt(modelTree.getParent(mnode).getAttribute("number"));
            int tp = Integer.parseInt(modelTree.getParent(modelTree.getParent(mnode)).getAttribute("number"));
            ms.removeObjectLayer(model, tp, ins, mnode.getAttribute(model.getModelName()), callback);
    }
    
    public void removeObjects()
    {
        ModelServiceAsync ms = ModelService.Util.getInstance();
        final AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

                public void onFailure(Throwable caught) {
                    SC.say("Cannot remove Objects");
                }

                public void onSuccess(SimulationObjectModel result) {
                    model = result;
                    bmodif = true;
                    checkModality();
                }
            };
            int ins = Integer.parseInt(modelTree.getParent(modelTree.getParent(mnode)).getAttribute("number"));
            int tp = Integer.parseInt(modelTree.getParent(modelTree.getParent(modelTree.getParent(mnode))).getAttribute("number"));
            String layer = modelTree.getParent(mnode).getAttribute(model.getModelName());
            if(layer.contains("Anatomy"))
                layer = "Anatomy";
            ms.removeObjects(model, tp, ins, getTypeFromMap(layer).toString(), callback);
    }
    
     public void removePhysicals()
    {
        ModelServiceAsync ms = ModelService.Util.getInstance();
        final AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

                public void onFailure(Throwable caught) {
                    SC.say("Cannot remove all Physicals");
                }

                public void onSuccess(SimulationObjectModel result) {
                    model = result;
                    bmodif = true;
                    checkModality();
                }
            };
            int ins = Integer.parseInt(modelTree.getParent(modelTree.getParent(mnode)).getAttribute("number"));
            int tp = Integer.parseInt(modelTree.getParent(modelTree.getParent(modelTree.getParent(mnode))).getAttribute("number"));
            String layer = modelTree.getParent(mnode).getAttribute(model.getModelName());
            if(layer.contains("Anatomy"))
                layer = "Anatomy";
            ms.removePhysicals(model, tp, ins, getTypeFromMap(layer).toString(), callback);
    }
    
    public void removeObject()
    {
        ModelServiceAsync ms = ModelService.Util.getInstance();
        final AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

                public void onFailure(Throwable caught) {
                    SC.say("Cannot remove Object");
                }

                public void onSuccess(SimulationObjectModel result) {
                    model = result;
                    bmodif = true;
                    checkModality();
                }
            };
            ModelTreeNode objects = (ModelTreeNode)modelTree.getParent(mnode);
            int ins = Integer.parseInt(modelTree.getParent(modelTree.getParent(objects)).getAttribute("number"));
            int tp = Integer.parseInt(modelTree.getParent(modelTree.getParent(modelTree.getParent(objects))).getAttribute("number"));
            String layer = modelTree.getParent(objects).getAttribute(model.getModelName());
            if (layer.contains("Anatomy"))
                layer = "Anatomy";
            ms.removeObject(model, tp, ins, getTypeFromMap(layer).toString(), mnode.getAttribute(model.getModelName()), callback);
    }
    
    public void removePhysical()
    {
        ModelServiceAsync ms = ModelService.Util.getInstance();
        final AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

                public void onFailure(Throwable caught) {
                    SC.say("Cannot remove Physical");
                }

                public void onSuccess(SimulationObjectModel result) {
                    model = result;
                    bmodif = true;
                    checkModality();
                }
            };
            ModelTreeNode objects = (ModelTreeNode)modelTree.getParent(mnode);
            int ins = Integer.parseInt(modelTree.getParent(modelTree.getParent(objects)).getAttribute("number"));
            int tp = Integer.parseInt(modelTree.getParent(modelTree.getParent(modelTree.getParent(objects))).getAttribute("number"));
            String layer = modelTree.getParent(objects).getAttribute(model.getModelName());
            if (layer.contains("Anatomy"))
                layer = "Anatomy";
            ms.removeObject(model, tp, ins, getTypeFromMap(layer).toString(), mnode.getAttribute(model.getModelName()), callback);
    }
    
    public void removeNode() {
        ModelServiceAsync ms = ModelService.Util.getInstance();

        String name = mnode.getAttribute(model.getModelName());
        if (name.contains("Timepoint")) {
           removeTimepoint();
        } else if (name.contains("Instant")) {
            removeInstant();
        } else if (name.contains("Anatomy") || name.contains("External agent") || name.contains("Foreign body")
                || name.contains("Pathology") || name.contains("Geometry")) {

            removeObjectLayer();
        } else if (name.contains("Objects")) {
            removeObjects();
        } else if (name.contains("Physical"))
        {
            removePhysicals();
            
        } else if (modelTree.getParent(mnode).getAttribute(model.getModelName()).contains("Objects")) {
            removeObject(); 
        }
        else if (modelTree.getParent(mnode).getAttribute(model.getModelName()).contains("Physicals")) {
            removePhysical(); 
        }
            else
        {
            //nothing
        }
        modelTree.remove(mnode);
        mnode = (ModelTreeNode)modelTree.getRoot();
       
    }

    private void loadEmpty() {


        ModelTreeNode instants = new ModelTreeNode("", "Instant 0", true, 0, null);

        instants.setIcon(ModelConstants.APP_IMG_INSTANT);
        ModelTreeNode timepoints = new ModelTreeNode("", "Timepoint ()", true, 1, instants);
        timepoints.setIcon(ModelConstants.APP_IMG_TIMEPOINT);
        TreeNode root = new ModelTreeNode("1", "Root", true, 1, timepoints);
        modelTree = new Tree();
        modelTree.setModelType(TreeModelType.CHILDREN);
        modelTree.setNameProperty("Debug");
        modelTree.setIdField("EntityId");
        modelTree.setChildrenProperty("Children");
        modelTree.setOpenProperty("isOpen");
        modelTree.setRoot(root);

        setData(modelTree);
    }

    public void refreshModel(SimulationObjectModel result) {
        bmodif = true;
        model = result;
        checkModality();
    }

    public void addTimePoint(Date d, int id) {
        ModelServiceAsync ms = ModelService.Util.getInstance();
        AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

            public void onFailure(Throwable caught) {
                SC.warn("Cant add a timepoint");
            }

            public void onSuccess(SimulationObjectModel result) {
                bmodif = true;
                model = result;
                checkModality();
                ModelTreeNode timepoint = new ModelTreeNode("", "Timepoint (" + new Date(System.currentTimeMillis()) + ")", true, ntp++, null);
                timepoint.setIcon(ModelConstants.APP_IMG_TIMEPOINT);
                modelTree.add(timepoint, modelTree.getRoot());

            }
        };
        ms.addTimePoint(model, d, callback);

    }

    public void addInstant() {
        ModelServiceAsync ms = ModelService.Util.getInstance();
        AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

            public void onFailure(Throwable caught) {
                SC.warn("Cant add an instant");
            }

            public void onSuccess(SimulationObjectModel result) {
                model = result;
                bmodif = true;
                checkModality();
                ModelTreeNode node = findNode(tpSelected);
                int size = modelTree.getFolders(node).length;
                ModelTreeNode instant = new ModelTreeNode("", "Instant (1000 )", true, size, null);
                instant.setIcon(ModelConstants.APP_IMG_INSTANT);
                modelTree.add(instant, node);
            }
        };
        ms.addInstant(model, tpSelected, callback);

    }
    


    public ModelTreeNode findNode(int... index) {
        TreeNode[] tpnodes = modelTree.getFolders(modelTree.getRoot());
        ModelTreeNode node;
        logger.log(Level.SEVERE, "index : " + index[0] + " tp size :" + tpnodes.length);
        for (TreeNode tp : tpnodes) {
            if (tp.getAttributeAsInt("number") == index[0]) {
                if (index.length == 1) {
                    return (ModelTreeNode) tp;
                } else {
                    TreeNode[] insnodes = modelTree.getFolders(tp);
                    for (TreeNode ins : insnodes) {
                        if (ins.getAttributeAsInt("number") == index[1]) {
                            return (ModelTreeNode) ins;

                        }
                    }
                }
                break;
            }
        }
        return null;
    }

    private void load(SimulationObjectModel model) {
        //model timepoints
        int id = 0;

        int nit = 0;

        ModelTreeNode[] timepoints = new ModelTreeNode[model.getTimepoints().size()];
                logger.log(Level.SEVERE, "TIMEPOINTS size : " + timepoints.length);
        //  SC.say(String.valueOf(model.getTimepoints().size()));
        for (Timepoint tp : model.getTimepoints()) {
            ModelTreeNode[] instants = new ModelTreeNode[tp.getInstants().size()];
            nit = 0;
            for (Instant it : tp.getInstants()) {
                int nol = 0;
                ModelTreeNode[] instantLayers = new ModelTreeNode[it.getObjectLayers().size() + 1];

                ModelTreeNode[] physicalLayers = new ModelTreeNode[it.getPhysicalParametersLayers().size()];



                int nopl = 0;

                for (PhysicalParametersLayer pl : it.getPhysicalParametersLayers()) {
                    String icon = ModelConstants.APP_IMG_MAGNETIC;
                    if (pl.getType() == PhysicalParameterType.T1 || pl.getType() == PhysicalParameterType.T2 || pl.getType() == PhysicalParameterType.T2s || pl.getType() == PhysicalParameterType.protonDensity || pl.getType() == PhysicalParameterType.susceptibility) {
                        icon = ModelConstants.APP_IMG_MAGNETIC;
                    }
                    if (pl.getType() == PhysicalParameterType.chemicalComposition) {
                        icon = ModelConstants.APP_IMG_CHEMICAL;
                    }
                    if (pl.getType() == PhysicalParameterType.radioactivity) {
                        icon = ModelConstants.APP_IMG_RADIO;
                    }
                    if (pl.getType() == PhysicalParameterType.echogenicity) {
                        icon = ModelConstants.APP_IMG_ECHO;
                    }
                    if (pl.getType() == PhysicalParameterType.concentration) {
                        icon = ModelConstants.APP_IMG_CONCENTRATION;
                    }

                    physicalLayers[nopl++] = new ModelTreeNode("" + (2 + id++), pl.toString(), false, nopl);
                    physicalLayers[nopl - 1].setIcon(icon);
                }
                if (nopl != 0) {
                    instantLayers[nol++] = new ModelTreeNode("" + (2 + id++), "Physical parameter maps", false, nol, physicalLayers);
                    instantLayers[nol - 1].setIcon(ModelConstants.APP_IMG_MAP);
                }



                for (ObjectLayer ol : it.getObjectLayers()) {
                    int nolp = 0;
                    ModelTreeNode[] objectLayerParts = new ModelTreeNode[2];

                    ModelTreeNode[] objectLayerPhysParams = new ModelTreeNode[2];

                    int olppl = 0;
                    ModelTreeNode[] objectLayerPhysParamsLUT = new ModelTreeNode[ol.getPhysicalParameters().size()];
                    for (PhysicalParameter pp : ol.getPhysicalParameters()) {
                        String description = pp.toString();
                        String icon = ModelConstants.APP_IMG_MAGNETIC;
                        if (pp.getType() == PhysicalParameterType.T1 || pp.getType() == PhysicalParameterType.T2 || pp.getType() == PhysicalParameterType.T2s || pp.getType() == PhysicalParameterType.protonDensity || pp.getType() == PhysicalParameterType.susceptibility) {
                            icon = ModelConstants.APP_IMG_MAGNETIC;
                        }
                        if (pp.getType() == PhysicalParameterType.chemicalComposition) {
                            icon = ModelConstants.APP_IMG_CHEMICAL;
                        }
                        if (pp.getType() == PhysicalParameterType.radioactivity) {
                            icon = ModelConstants.APP_IMG_RADIO;
                        }
                        if (pp.getType() == PhysicalParameterType.echogenicity) {
                            icon = ModelConstants.APP_IMG_ECHO;
                        }
                        if (pp.getType() == PhysicalParameterType.concentration) {
                        icon = ModelConstants.APP_IMG_CONCENTRATION;
                        }

                        objectLayerPhysParamsLUT[olppl++] = new ModelTreeNode("" + (2 + id++), description, false, olppl);
                        objectLayerPhysParamsLUT[olppl - 1].setIcon(icon);
                    }
                    if (olppl != 0) {
                        objectLayerPhysParams[0] = new ModelTreeNode("" + (2 + id++), "Look-up tables", true, 0, objectLayerPhysParamsLUT);
                        objectLayerPhysParams[0].setIcon(ModelConstants.APP_IMG_LUT);
                    }

                    ModelTreeNode[] objectLayerPhysParamsLayer = new ModelTreeNode[ol.getPhysicalParametersLayers().size()];
                    int olppla = 0;

                    for (PhysicalParametersLayer ppl : ol.getPhysicalParametersLayers()) {
                        String description = ppl.toString();
                        objectLayerPhysParamsLayer[olppla++] = new ModelTreeNode("" + (2 + id++), description, false, olppla);

                        objectLayerPhysParamsLayer[olppla - 1].setIcon(getPhysicalIcon(ppl.getType()));

                    }

                    if (olppla != 0) {
                        objectLayerPhysParams[1] = new ModelTreeNode("" + (2 + id++), "Maps", true, 1, objectLayerPhysParamsLayer);
                        objectLayerPhysParams[1].setIcon(ModelConstants.APP_IMG_MAP);
                    }
                    if (olppla != 0 || olppl != 0) {
                        objectLayerParts[nolp++] = new ModelTreeNode("" + (2 + id++), "Physical parameters", false, nolp, objectLayerPhysParams);
                        objectLayerParts[nolp - 1].setIcon(ModelConstants.APP_IMG_PHYSICAL_PARAMS);
                    }
                    ModelTreeNode[] objects = new ModelTreeNode[ol.getLayerParts().size()];
                    int no = 0;
                    for (ObjectLayerPart olp : ol.getLayerParts()) {

                        String description = olp.getReferredObject().getObjectName().replace("_", " ") + " (" + olp.getFormat() + ": ";
                        if (olp.getFormat() == ObjectLayerPart.Format.voxel) {
                            description += "label " + olp.getLabel() + " in ";
                        }

                        description += olp.getFileNames().toString().replace("[", "").replace("]", "") + ")";
                        objects[no++] = new ModelTreeNode("" + (2 + id++), description, false, no);
                        objects[no - 1].setIcon(ModelConstants.APP_IMG_OBJECT);
                    }
                    objectLayerParts[nolp++] = new ModelTreeNode("" + (2 + id++), "Objects", false, nolp - 1, objects);
                    objectLayerParts[nolp - 1].setIcon(ModelConstants.APP_IMG_OBJECT);

                    String description = getDescriptionObject(ol.getType());
                    if (!ol.getResolution().equals(ObjectLayer.Resolution.none)) {
                        description += " (" + ol.getResolution().toString() + " resolution) ";
                    }
                    instantLayers[nol++] = new ModelTreeNode("" + (2 + id++), description, false, nol, objectLayerParts);

                    instantLayers[nol - 1].setIcon(getIconObject(ol.getType()));
                }
                instants[nit++] = new ModelTreeNode("" + (2 + id++), "Instant (" + it.getDuration() + ")", true, nit - 1, instantLayers);
                instants[nit - 1].setIcon(ModelConstants.APP_IMG_INSTANT);
            }
            timepoints[ntp++] = new ModelTreeNode("" + (2 + id++), "Timepoint (" + tp.getStartingDate() + ")", true, ntp - 1, instants);
            timepoints[ntp - 1].setIcon(ModelConstants.APP_IMG_TIMEPOINT);
        }
        TreeNode root = new ModelTreeNode("1", "Root", true, 1, timepoints);

        //create tree with model timepoints
        modelTree = new Tree();
        modelTree.setModelType(TreeModelType.CHILDREN);
        modelTree.setNameProperty(model.getModelName());
        modelTree.setIdField("EntityId");
        modelTree.setChildrenProperty("Children");
        modelTree.setOpenProperty("isOpen");
        modelTree.setRoot(root);


        logger.log(Level.SEVERE, "root size : " + modelTree.getFolders(modelTree.getRoot()).length);

        setData(modelTree);
    }

    public int getTimePoint() {
        return tpSelected;
    }

    public int getInstant() {
        return insSelected;
    }

    public int typeDropped(String drop) {
        if (drop.contains(".mhd") || drop.contains(".zraw") || drop.contains(".raw")) {
            return 1; //voxels
        } else if (drop.contains("vtk") || drop.contains("vtp")) {
            return 0; //meshes
        } else if (drop.contains(".txt")) {
            return 2;
        } else {
            return -1;
        }
    }

    private String getIconObject(SimulationObjectModel.ObjectType type) {
        String icon = ModelConstants.APP_IMG_ANATOMY;

        if (type == SimulationObjectModel.ObjectType.anatomical) {
            return ModelConstants.APP_IMG_ANATOMY;
        } else if (type == SimulationObjectModel.ObjectType.external_agent) {
            return ModelConstants.APP_IMG_EXTERNAL;
        } else if (type == SimulationObjectModel.ObjectType.foreign_body) {
            return ModelConstants.APP_IMG_FOREIGN;
        } else if (type == SimulationObjectModel.ObjectType.pathological) {
            return ModelConstants.APP_IMG_PATHOLOGY;
        } else if (type == SimulationObjectModel.ObjectType.geometrical) {
            return ModelConstants.APP_IMG_GEOMETRY;
        } else {
            return ModelConstants.APP_IMG_ANATOMY;
        }
    }

    private String getDescriptionObject(SimulationObjectModel.ObjectType type) {
        if (type == SimulationObjectModel.ObjectType.anatomical) {
            return "Anatomy";
        } else if (type == SimulationObjectModel.ObjectType.external_agent) {
            return "External agent";
        } else if (type == SimulationObjectModel.ObjectType.foreign_body) {
            return "Foreign body";
        } else if (type == SimulationObjectModel.ObjectType.pathological) {
            return "Pathology";
        } else if (type == SimulationObjectModel.ObjectType.geometrical) {
            return "Geometry";
        } else {
            return "";
        }

    }

    private String getPhysicalIcon(PhysicalParameterType type) {
        if (type == PhysicalParameterType.T1 || type == PhysicalParameterType.T2
                || type == PhysicalParameterType.T2s || type == PhysicalParameterType.protonDensity
                || type == PhysicalParameterType.susceptibility) {
            return ModelConstants.APP_IMG_MAGNETIC;
        } else if (type == PhysicalParameterType.chemicalComposition) {
            return ModelConstants.APP_IMG_CHEMICAL;
        } else if (type == PhysicalParameterType.radioactivity) {
            return ModelConstants.APP_IMG_RADIO;
        } else if (type == PhysicalParameterType.echogenicity) {
            return ModelConstants.APP_IMG_ECHO;
        } else if (type == PhysicalParameterType.concentration) {
            return ModelConstants.APP_IMG_CONCENTRATION;
        } else {
            return "";
        }
    }

    // add an object with:
    // tp: timepoint
    // ins: instant
    // type: mexh, voxel, or physical parameters
    // name: name of object to add
    // OntoName: semantic name
    // objLayer: Layer to add
    // lab: associated label if needed
    public void addObjectItem(int tp, int ins, int type, String name, String OntoName, String objLayer, int lab) {
        int nbChild = 0;
        bmodif = true;
        checkModality();
        logger.log(Level.SEVERE, "tp :" + String.valueOf(tp) + "ins : " + String.valueOf(ins) + "type : " + String.valueOf(type)
                + "name : " + name + "OntoName : " + OntoName + "lab :" + String.valueOf(lab));
        ModelTreeNode insnode = findNode(tp, ins);
        // pour objet on doit regarder un niveau en dessous
        //Check if the object layer exists for this instant
        TreeNode[] nodes = modelTree.getFolders(insnode);
        ModelTreeNode objectLayerPartsNode = null;
        ModelTreeNode LayerNode = null;
        ModelTreeNode physicalLutNode = null;
        ModelTreeNode objectLeaf = null;
        // 

        boolean bLayerExist = false;
        boolean bObjectLayerExist = false;
        boolean bObjectExist = false;
        boolean bphysicalLutExist = false;

        String layer = "";
        for (String key : layerTypeMap.keySet()) {
            if (layerTypeMap.get(key).toString() == objLayer) {
                layer = key;
                break;
            }
        }

        logger.log(Level.SEVERE, "layer :" + layer);
        String layerPartName = "";

        if (type == 0 || type == 1) {
            layerPartName = "Objects";
        } else if (type == 2 || type == 3) {
            layerPartName = "Physical parameters";
        } else {
            //nothing
        }

        for (TreeNode nd : nodes) {
            // Find if the wanted layer exists
            if (nd.getAttribute(model.getModelName()).contains(layer)) {
                TreeNode[] objects = modelTree.getFolders(nd);
                bLayerExist = true;
                LayerNode = (ModelTreeNode) nd;
                for (TreeNode obj : objects) {
                    // Find if the Object Layer exist
                    if (obj.getAttribute(model.getModelName()).contains(layerPartName)) {
                        if (type == 0 || type == 1) {
                            bObjectLayerExist = true;
                            nbChild = modelTree.getDescendantLeaves(obj).length;
                            logger.log(Level.SEVERE, "couche object trouv�: " + String.valueOf(nbChild));
                            objectLayerPartsNode = (ModelTreeNode) obj;
                            TreeNode[] leaves = modelTree.getLeaves(objectLayerPartsNode);
                            for (TreeNode leave : leaves) {
                                if (leave.getAttribute(model.getModelName()).contains(name)
                                        && leave.getAttribute(model.getModelName()).contains(OntoName)) {
                                    bObjectExist = true;
                                    break;
                                }
                            }
                            break;
                        } else if (type == 2 || type == 3) {
                            bObjectLayerExist = true;
                            TreeNode[] physicalnodes = modelTree.getFolders(obj);
                            objectLayerPartsNode = (ModelTreeNode) obj;

                            for (TreeNode physical : physicalnodes) {
                                if (physical.getAttribute(model.getModelName()).contains("Look-up tables") && type == 2) {
                                    bphysicalLutExist = true;
                                    nbChild = modelTree.getDescendantLeaves(physical).length;
                                    logger.log(Level.SEVERE, "couche object trouv�: " + String.valueOf(nbChild));
                                    physicalLutNode = (ModelTreeNode) physical;
                                    break;

                                } else if (physical.getAttribute(model.getModelName()).contains("Maps") && type == 3) {
                                    bphysicalLutExist = true;
                                    nbChild = modelTree.getDescendantLeaves(physical).length;
                                    logger.log(Level.SEVERE, "LUT trouv�: " + String.valueOf(nbChild));
                                    physicalLutNode = (ModelTreeNode) physical;
                                    break;
                                }

                            }
                        }
                        break;
                    }
                }
                if (bObjectLayerExist) {
                    break;
                }
            }
            if (bLayerExist) {
                break;
            }
        }

        if (bObjectExist) {
            SC.say("Object already in the model");
            return;
        } else if (type == 0 || type == 1) {
            String format = " (mesh";
            String description = OntoName;
            if (type == 1) { //voxel
                format = " (voxel";
                description += format + ": label " + String.valueOf(lab) + " in ";
                if (name.contains(".raw")) {
                    description += name.substring(0, name.indexOf(".raw")) + ".mhd, " + name + ")";
                } else if (name.contains(".zraw")) {
                    description += name.substring(0, name.indexOf(".zraw")) + ".mhd, " + name + ")";
                } else {
                    description += name + ", " + associatedraw + ")";
                }
            } else {
                description += format + ": " + name + ")";
            }

            ModelTreeNode objectNode = new ModelTreeNode("", description, false, nbChild, null);
            objectNode.setIcon(ModelConstants.APP_IMG_OBJECT);
            if (bObjectLayerExist) {
                modelTree.add(objectNode, objectLayerPartsNode);
            } else {
                //create the Object Layer
                objectLayerPartsNode = new ModelTreeNode("", layerPartName, false, 1 - 1, objectNode);
                objectLayerPartsNode.setIcon(ModelConstants.APP_IMG_OBJECT);
                if (bLayerExist) {
                    modelTree.add(objectLayerPartsNode, LayerNode);
                } else {
                    // create the Layer
                    LayerNode = new ModelTreeNode("", layer, false, 1, objectLayerPartsNode);
                    LayerNode.setIcon(getIconObject(layerTypeMap.get(layer)));
                    modelTree.add(LayerNode, insnode);
                }
            }
        }
        ArrayList<String> objNames = new ArrayList<String>();

        if (type == 0)
        {
            objNames.add(name);
        }
        else
        {
            if (name.contains(".raw")) {
                    objNames.add(name.substring(0, name.lastIndexOf(".raw")) + ".mhd");
                    objNames.add(name);
                } else if (name.contains(".zraw")) {
                    objNames.add(name.substring(0, name.lastIndexOf(".zraw")) + ".mhd");
                    objNames.add(name);
                } else if (name.contains(".mhd")) {
                    objNames.add(name);
                    objNames.add(associatedraw);
            }
                else {}
        }
        



        ModelServiceAsync ms = ModelService.Util.getInstance();
        final AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

            public void onFailure(Throwable caught) {
                SC.say("Cannot added object in model");
            }

            public void onSuccess(SimulationObjectModel result) {
                SC.say("object added to model");
                model = result;
            }
        };

        ms.addObject(model, OntoName, objNames, tp, ins, type, lab, callback);
    }

    public SimulationObjectModel.ObjectType getTypeFromMap(String type) {
        return layerTypeMap.get(type);
    }

    public String getLayerFromMap(String type) {
        String layer = "";
        for (String key : layerTypeMap.keySet()) {
            if (layerTypeMap.get(key).toString() == type) {
                layer = key;
                break;
            }
        }
        return layer;
    }

    public ArrayList<String> getLutMap() {
        ArrayList<String> luts = new ArrayList<String>();
        for (Entry<String, PhysicalParameterType> entry : lutTypeMap.entrySet()) {
            luts.add(entry.getValue().toString());
        }
        return luts;
    }

    public void addPhysicalItem(int tp, int ins, int type, String name, String objLayer, String label) {
        int nbChild = 0;
        bmodif = true;
        checkModality();
        logger.log(Level.SEVERE, "tp :" + String.valueOf(tp) + "ins : " + String.valueOf(ins) + "type : " + String.valueOf(type)
                + "name : " + name + "lab :" + label);
        ModelTreeNode insnode = findNode(tp, ins);
        // pour objet on doit regarder un niveau en dessous
        //Check if the object layer exists for this instant
        logger.log(Level.SEVERE, "layer :" + objLayer);
        TreeNode[] nodes = modelTree.getFolders(insnode);
        ModelTreeNode objectLayerPartsNode = null;
        ModelTreeNode LayerNode = null;
        ModelTreeNode physicalLutNode = null;
        ModelTreeNode objectLeaf = null;
        // 

        boolean bLayerExist = false;
        boolean bObjectLayerExist = false;
        boolean bObjectExist = false;
        boolean bphysicalLutExist = false;

        
        if (!objLayer.equals("All"))
        {    
            String layer = getLayerFromMap(objLayer);

            logger.log(Level.SEVERE, "layer :" + layer);
            String layerPartName = "Physical parameters";
     
            for (TreeNode nd : nodes) {
                logger.log(Level.SEVERE, "nom des couches :" + nd.getAttribute(model.getModelName()));
                // Find if the wanted layer exists
                if (nd.getAttribute(model.getModelName()).contains(layer)) {
                    TreeNode[] objects = modelTree.getFolders(nd);
                    bLayerExist = true;
                    LayerNode = (ModelTreeNode) nd;
                    for (TreeNode obj : objects) {
                        logger.log(Level.SEVERE, "nom des couches objects :" + obj.getAttribute(model.getModelName()));
                        logger.log(Level.SEVERE, "layerpartName :" + layerPartName);
                        // Find if the Object Layer exist
                        if (obj.getAttribute(model.getModelName()).contains(layerPartName)) {
                            bObjectLayerExist = true;
                            TreeNode[] physicalnodes = modelTree.getFolders(obj);
                            objectLayerPartsNode = (ModelTreeNode) obj;

                            for (TreeNode physical : physicalnodes) {
                                if (physical.getAttribute(model.getModelName()).contains("Look-up tables") && type == 2) {
                                    bphysicalLutExist = true;
                                    nbChild = modelTree.getDescendantLeaves(physical).length;
                                    logger.log(Level.SEVERE, "couche object trouv�: " + String.valueOf(nbChild));
                                    physicalLutNode = (ModelTreeNode) physical;
                                    break;

                                } else if (physical.getAttribute(model.getModelName()).contains("Maps") && type == 3) {
                                    bphysicalLutExist = true;
                                    nbChild = modelTree.getDescendantLeaves(physical).length;
                                    logger.log(Level.SEVERE, "LUT trouv�: " + String.valueOf(nbChild));
                                    physicalLutNode = (ModelTreeNode) physical;
                                    break;
                                }

                            }
                        }
                        
                    }
                    if (bObjectLayerExist) {
                        break;
                    }
                }
                if (bLayerExist) {
                break;
            }
        }


        String format = name;
        String description = "";
        if (type == 2) { //LUT
            description = label + "[" + name + "]";

        } else {
            description += format + ": " + name + ")";
        }

        ModelTreeNode objectNode = new ModelTreeNode("", description, false, 1, null);
        objectNode.setIcon(getPhysicalIcon(lutTypeMap.get(label)));
        if (bphysicalLutExist) {
            modelTree.add(objectNode, physicalLutNode);
        } else {
            if (type == 2) {
                physicalLutNode = new ModelTreeNode("", "Look-up tables", false, 1, objectNode);
                physicalLutNode.setIcon(ModelConstants.APP_IMG_LUT);
            } else if (type == 3) {// map
                physicalLutNode = new ModelTreeNode("", "Maps", false, 1, objectNode);
                physicalLutNode.setIcon(ModelConstants.APP_IMG_MAP);
            }

            if (bObjectLayerExist) {
                modelTree.add(physicalLutNode, objectLayerPartsNode);
            } else {
                //create the Object Layer
                objectLayerPartsNode = new ModelTreeNode("", layerPartName, false, 1, physicalLutNode);
                objectLayerPartsNode.setIcon(ModelConstants.APP_IMG_OBJECT);
                if (bLayerExist) {
                    modelTree.add(objectLayerPartsNode, LayerNode);
                } else {
                    // create the Layer
                    LayerNode = new ModelTreeNode("", layer, false, 1, objectLayerPartsNode);
                    modelTree.add(LayerNode, insnode);
                }
            }
        }

        ModelServiceAsync ms = ModelService.Util.getInstance();
        final AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

            public void onFailure(Throwable caught) {
                SC.say("Cannot added LUT to model");
            }

            public void onSuccess(SimulationObjectModel result) {
                SC.say("LUT added to model");
                model = result;
            }
        };

            ms.addLUT(model, layerTypeMap.get(layer), name, tpSelected, insSelected, lutTypeMap.get(label), type, callback);
        }
        else
        {
           

            logger.log(Level.SEVERE, "layer :" + objLayer);
            String layerPartName = "Physical parameters";
     
            for (TreeNode nd : nodes) {
                logger.log(Level.SEVERE, "nom des couches :" + nd.getAttribute(model.getModelName()));
                // Find if the wanted layer exists
                if (nd.getAttribute(model.getModelName()).contains(layerPartName)) {
                    TreeNode[] physicalnodes = modelTree.getFolders(nd);
                    bLayerExist = true;
                    LayerNode = (ModelTreeNode) nd;
                    for (TreeNode physical : physicalnodes) {

                        if (physical.getAttribute(model.getModelName()).contains("Maps")) {
                            bphysicalLutExist = true;
                            nbChild = modelTree.getDescendantLeaves(physical).length;
                            logger.log(Level.SEVERE, "LUT trouv�: " + String.valueOf(nbChild));
                            physicalLutNode = (ModelTreeNode) physical;
                            break;
                        }

                    }
                }
                if (bLayerExist) {
                        break;
                    }

            }
        
        
        String format = name;
        String description = format + ": " + name + ")";
        
        ModelTreeNode objectNode = new ModelTreeNode("", description, false, 1, null);
        objectNode.setIcon(getPhysicalIcon(lutTypeMap.get(label)));
        if (bphysicalLutExist) {
            modelTree.add(objectNode, physicalLutNode);
        } else {
            // map
                physicalLutNode = new ModelTreeNode("", "Maps", false, 1, objectNode);
                physicalLutNode.setIcon(ModelConstants.APP_IMG_MAP);
                objectLayerPartsNode = new ModelTreeNode("", layerPartName, false, 1, physicalLutNode);
                objectLayerPartsNode.setIcon(ModelConstants.APP_IMG_OBJECT);
                modelTree.add(objectLayerPartsNode, insnode);
               
            }
        
        ModelServiceAsync ms = ModelService.Util.getInstance();
        final AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

            public void onFailure(Throwable caught) {
                SC.say("Cannot added MAP to model");
            }

            public void onSuccess(SimulationObjectModel result) {
                SC.say("MAP added to model");
                model = result;
            }
        };

            ms.addMap(model, name, tp, ins, PhysicalParameterType.T1, 0,"","", callback);
        }
                    
         
    }

    public SimulationObjectModel getModel() {
        return model;
    }

    public void rename(String name, SimulationObjectModel result) {
        model = result;
        bmodif = true;
        checkModality();
        mnode.setAttribute(model.getModelName(), name);
        this.markForRedraw();
    }

    public void duplicateInstant() {
        ModelServiceAsync ms = ModelService.Util.getInstance();
        final AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

            public void onFailure(Throwable caught) {
                SC.say("Cannot duplicate Instant");
            }

            public void onSuccess(SimulationObjectModel result) {
                SC.say("Instant duplicated");
                model = result;
                bmodif = true;
                checkModality();
                modelTree.add(new ModelTreeNode(mnode), modelTree.getParent(mnode));
            }
        };

        ms.duplicateInstant(model, Integer.parseInt(modelTree.getParent(mnode).getAttribute("number")), Integer.parseInt(mnode.getAttribute("number")), callback);
        checkModality();
        this.markForRedraw();
    }

    public void duplicateTimepoint() {
        ModelServiceAsync ms = ModelService.Util.getInstance();
        final AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

            public void onFailure(Throwable caught) {
                SC.say("Cannot duplicate Timepoint");
            }

            public void onSuccess(SimulationObjectModel result) {
                SC.say("Timepoint duplicated");
                model = result;
                bmodif = true;
                checkModality();
                ModelTreeNode node = new ModelTreeNode(mnode);

                modelTree.add(node, modelTree.getParent(mnode));
            }
        };

        ms.duplicateTimePoint(model, Integer.parseInt(mnode.getAttribute("number")), callback);
        this.markForRedraw();
    }

    public void renameTimepoint() {
              bmodif = true;
              checkModality();
        RenameTimepointWindow win = new RenameTimepointWindow(ModelTreeGrid.this,
                Integer.parseInt(mnode.getAttribute("number")), mnode.getAttribute(model.getModelName()));
        win.show();
    }

    public void renameInstant() {
      bmodif = true;
      checkModality();
        RenameInstantWindow win = new RenameInstantWindow(ModelTreeGrid.this, Integer.parseInt(modelTree.getParent(mnode).getAttribute("number")),
                Integer.parseInt(mnode.getAttribute("number")), mnode.getAttribute(model.getModelName()));
        win.show();

    }
    
    public ModelToolStrip getToolStrip()
    {
        return mts;
    }
    private void checkModality()
    {
  
            if (SimulationObjectModelUtil.isReadyForSimulation(model, SimulationObjectModelUtil.Modality.IRM)) {
                mts.isOk("MRI");
        } else {
              mts.isKo("MRI");
        }
        if (SimulationObjectModelUtil.isReadyForSimulation(model, SimulationObjectModelUtil.Modality.CT)) {
             mts.isOk("PET");
        } else {
              mts.isKo("PET");
        }
        if (SimulationObjectModelUtil.isReadyForSimulation(model, SimulationObjectModelUtil.Modality.PET)) {
             mts.isOk("CT");
        } else {
             mts.isKo("CT");
        }
        if (SimulationObjectModelUtil.isReadyForSimulation(model, SimulationObjectModelUtil.Modality.US)) {
             mts.isOk("UltraSound");
        } else {
              mts.isKo("UltraSound");
        }
           mts.redraw();
    }



    public ArrayList<String> getObjectsLabel(int tp, int ins, String layerType) {
        ArrayList<String> results = new ArrayList<String>();
        ArrayList<ObjectLayer> ols = model.getTimepoint(tp).getInstant(ins).getObjectLayers();

        for (ObjectLayer ol : ols) {
            for (ObjectLayerPart olp : ol.getLayerParts()) {
//               logger.log(Level.SEVERE, "label label:"+ getLayerFromMap(layerType));
//                logger.log(Level.SEVERE, "label label2:" + olp.getType().toString());
                if (olp.getType().toString() == layerType) {
                    logger.log(Level.SEVERE, "label label3: " + String.valueOf(olp.getLabel()));
                    results.add(String.valueOf(olp.getLabel()));

                }

            }
        }
        return results;
    }

    public ArrayList<String> getObjectsEntity(int tp, int ins, String layerType) {
        ArrayList<String> results = new ArrayList<String>();
        ArrayList<ObjectLayer> ols = model.getTimepoint(tp).getInstant(ins).getObjectLayers();
        for (ObjectLayer ol : ols) {
            for (ObjectLayerPart olp : ol.getLayerParts()) {
                if (olp.getType().equals(layerType)) {
                    results.add(olp.getReferredObject().getObjectName());
                }
            }
        }
        return results;
    }
    
    public boolean isModif(){
        return bmodif;
    }

    public class ModelTreeNode extends TreeNode {

        public ModelTreeNode(String entityId, String entityName, boolean display, int number) {
            this(entityId, entityName, display, number, new ModelTreeNode[]{});
        }

        public ModelTreeNode(ModelTreeNode src) {
            setAttribute(model.getModelName(), src.getAttribute(model.getModelName()));
            setAttribute("EntityId", src.getAttribute("entityId"));

            if (modelTree.hasChildren(src)) {
                TreeNode[] srChildren = modelTree.getChildren(src);
                TreeNode[] children = new TreeNode[srChildren.length];
                int index = 0;
                for (TreeNode node : srChildren) {
                    children[index++] = (new ModelTreeNode((ModelTreeNode) node));
                }
                setAttribute("Children", children);
            }
            setAttribute("isOpen", false);
            setAttribute("number", String.valueOf(modelTree.getChildren(src).length + 1));
            this.setIcon(src.getIcon());
            this.setTitle(src.getTitle());

        }

        public ModelTreeNode(String entityId, String entityName, boolean display, int number, ModelTreeNode... children) {
            setAttribute(model.getModelName(), entityName);
            setAttribute("EntityId", entityId);
            setAttribute("Children", children);
            setAttribute("isOpen", display);
            setAttribute("number", String.valueOf(number));
        }
    }

    public class ModelMenu extends Menu {

        private ModelTreeNode mnode = null;
        private ModelTreeGrid mgrid = null;
        private MenuItem removeItem = null;
        private MenuItem objectItem = null;
        private MenuItem objectsItem = null;
        private MenuItem instantItem = null;
        private MenuItem layerItem = null;
        private MenuItem durationIItem = null;
        private MenuItem durationTItem = null;
        private MenuItem physicalItem = null;
        private MenuItem duplicateInsItem = null;
        private MenuItem duplicateTpItem = null;

        public ModelMenu() {

            instantItem = new MenuItem();
            instantItem.setTitle("Add Instant");

            instantItem.setIcon(CoreConstants.ICON_ADD);
            instantItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {

                public void onClick(MenuItemClickEvent event) {
                    addInstant();
                }
            });

            duplicateTpItem = new MenuItem();
            duplicateTpItem.setTitle("Duplicate timepoint");
            duplicateTpItem.setIcon(CoreConstants.ICON_ADD);
            duplicateTpItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {

                public void onClick(MenuItemClickEvent event) {
                    duplicateTimepoint();
                }
            });

            duplicateInsItem = new MenuItem();
            duplicateInsItem.setTitle("Duplicate instant");
            duplicateInsItem.setIcon(ModelConstants.APP_IMG_OK);
            duplicateInsItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {

                public void onClick(MenuItemClickEvent event) {
                    duplicateInstant();
                }
            });

            layerItem = new MenuItem();
            layerItem.setTitle("Add layer");
            layerItem.setIcon(ModelConstants.APP_IMG_OK);


            durationTItem = new MenuItem();
            durationTItem.setTitle("Modify timepoint starting");
            durationTItem.setIcon(CoreConstants.ICON_EDIT);
            durationTItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {

                public void onClick(MenuItemClickEvent event) {
                    renameTimepoint();
                }
            });


            durationIItem = new MenuItem();
            durationIItem.setTitle("Modify instant duration ");
            durationIItem.setIcon(CoreConstants.ICON_EDIT);
            durationIItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {

                public void onClick(MenuItemClickEvent event) {
                    renameInstant();
                }
            });

            physicalItem = new MenuItem();
            physicalItem.setTitle("Add physical parameters ");
            physicalItem.setIcon(ModelConstants.APP_IMG_OK);

            objectsItem = new MenuItem();
            objectsItem.setTitle("Add objects layer part");
            objectsItem.setIcon(ModelConstants.APP_IMG_OK);

            objectItem = new MenuItem();
            objectItem.setTitle("Add object");
            objectItem.setIcon(ModelConstants.APP_IMG_OK);

            removeItem = new MenuItem();
            removeItem.setTitle("Remove");
            removeItem.setIcon(ModelConstants.APP_IMG_KO);
            removeItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {

                public void onClick(MenuItemClickEvent event) {
                    removeNode();
                }
            });

            this.setItems(instantItem, removeItem);
        }

        public void setNode(ModelTreeNode node) {

            this.removeItem(instantItem);
            this.removeItem(removeItem);
            this.removeItem(durationIItem);
            this.removeItem(objectsItem);
            this.removeItem(objectItem);
            this.removeItem(physicalItem);
            this.removeItem(duplicateInsItem);
            this.removeItem(duplicateTpItem);
            this.removeItem(durationTItem);

            if (node.getAttribute(model.getModelName()).contains("Timepoint")) {
                this.setItems(duplicateTpItem, instantItem, durationTItem, removeItem);
            } else if (node.getAttribute(model.getModelName()).contains("Instant")) {
                this.setItems(duplicateInsItem, layerItem, durationIItem, removeItem);
            } else if (node.getAttribute(model.getModelName()).contains("Objects")) {
                this.setItems(objectItem, removeItem);
            } else if (layerTypeMap.keySet().contains(node.getAttribute(model.getModelName()))) {
                this.setItems(objectsItem, physicalItem, removeItem);
            } else {
                this.setItems( removeItem);
            }


        }

        public void setModelTreeNode(ModelTreeNode node) {
            mnode = node;
        }

        public void setTreeGrid(ModelTreeGrid grid) {
            mgrid = grid;
        }

        public void removeNode2() {
            ModelServiceAsync ms = ModelService.Util.getInstance();

            String name = mnode.getAttribute(model.getModelName());
            logger.log(Level.SEVERE, "name: " + name);
            if (name.contains("timepoint")) {
                final AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

                    public void onFailure(Throwable caught) {
                        SC.say("Cannot remove timepoint");
                    }

                    public void onSuccess(SimulationObjectModel result) {
                        model = result;
                    }
                };
                ms.removeTimePoint(model, Integer.parseInt(mnode.getAttribute("number")), callback);

            } else if (name.contains("instant")) {
                final AsyncCallback<SimulationObjectModel> callback = new AsyncCallback<SimulationObjectModel>() {

                    public void onFailure(Throwable caught) {
                        SC.say("Cannot remove instant");
                    }

                    public void onSuccess(SimulationObjectModel result) {
                        model = result;
                    }
                };
                ms.removeInstant(model, Integer.parseInt(modelTree.getParent(mnode).getAttribute("number")),
                        Integer.parseInt(mnode.getAttribute("number")), callback);
            }
            modelTree.remove(mnode);
        }
    }
}
