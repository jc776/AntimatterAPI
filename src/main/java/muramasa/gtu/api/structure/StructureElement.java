package muramasa.gtu.api.structure;

import muramasa.gtu.api.capability.IComponentHandler;
import muramasa.gtu.api.interfaces.IGregTechObject;
import muramasa.gtu.api.util.Utils;
import muramasa.gtu.api.util.int3;
import muramasa.gtu.api.tileentities.TileEntityMachine;
import muramasa.gtu.api.interfaces.IComponent;
import net.minecraft.tileentity.TileEntity;

import java.util.HashMap;

public class StructureElement {

    private static HashMap<String, StructureElement> elementLookup = new HashMap<>();

    private String elementName;
    private String[] elementIds;

    public boolean excludeFromList;

    public StructureElement(IGregTechObject object) {
        this(object.getName(), object);
    }

    public StructureElement(String elementName, IGregTechObject... objects) {
        this.elementName = elementName;
        this.elementIds = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            this.elementIds[i] = objects[i].getName();
            elementLookup.put(objects[i].getName(), this);
        }
        elementLookup.put(elementName, this);
    }

    public String getName() {
        return elementName;
    }

    public StructureElement excludeFromList() {
        excludeFromList = true;
        return this;
    }

    public boolean evaluate(TileEntityMachine machine, int3 pos, StructureResult result) {
        TileEntity tile = Utils.getTile(machine.getWorld(), pos.asBP());
        if (tile instanceof IComponent) {
            IComponentHandler component = ((IComponent) tile).getComponentHandler();
            for (int i = 0; i < elementIds.length; i++) {
                if (elementIds[i].equals(component.getId())) {
                    if (testComponent(component)) {
                        result.addComponent(elementName, component);
                        return true;
                    }
                    result.withError("Failed Component Requirement: " + component.getId());
                    return false;
                }
            }
            result.withError("Expected: '" + elementName + "' Found: '" + component.getId() + "' @" + pos);
            return false;
        }
        result.withError("No valid component found @" + pos);
        return false;
    }

    public boolean testComponent(IComponentHandler component) {
        return true;
    }

    public static StructureElement get(String name) {
        return elementLookup.get(name);
    }
}
