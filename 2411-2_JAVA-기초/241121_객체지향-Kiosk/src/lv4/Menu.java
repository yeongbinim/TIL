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

    public String formatMenuItems() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < menuItems.size(); i++) {
            builder.append(String.format("\n%2d. %s", i + 1, menuItems.get(i)));
        }
        return builder.toString();
    }

    public String getMenuItemInfo(int index) {
        return menuItems.get(index).toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
