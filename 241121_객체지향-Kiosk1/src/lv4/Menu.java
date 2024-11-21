package lv4;

import java.util.List;

public class Menu {
    private final List<MenuItem> menuItems;
    private final String name;

    public Menu(String name, MenuItem... items) {
        this.name = name;
        menuItems = List.of(items);
    }

    public String getName() {
        return name;
    }

    public List<MenuItem> getMenuItems() {
        return List.copyOf(menuItems);
    }

    public String getMenuItemInfo(int index) {
        return menuItems.get(index).toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
