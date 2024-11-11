import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CookRecipeApp {
    interface RecipeStructure<T> {
        String toString();
        void add(String recipe);
    }
    private static class RecipeMap<T extends Map<Integer, String>> implements RecipeStructure<T>{
        private final T map;
        public RecipeMap(T map) {
            this.map = map;
        }
        @Override
        public void add(String recipe) {
            map.put(map.size() + 1, recipe);
        }
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (int i = 1; i <= map.size(); i++) {
                result.append(String.format("%d. %s\n", i, map.get(i)));
            }
            return result.toString();
        }
    }
    private static class RecipeCollection<T extends Collection<String>> implements RecipeStructure<T>{

        private final T collection;

        public RecipeCollection(T collection) {
            this.collection = collection;
        }

        @Override
        public void add(String recipe) {
            collection.add(recipe);
        }
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            int i = 1;
            for (String value: collection) {
                result.append(String.format("%d. %s\n", i++, value));
            }
            return result.toString();
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String structure = br.readLine();
        String title = br.readLine();
        RecipeStructure<?> recipeStructure;
        switch(structure.toUpperCase()) {
            case "SET":
                recipeStructure = new RecipeCollection<>(new HashSet<>());
                break;
            case "LIST":
                recipeStructure = new RecipeCollection<>(new ArrayList<>());
                break;
            case "QUEUE":
                recipeStructure = new RecipeCollection<>(new ArrayDeque<>());
                break;
            case "MAP":
                recipeStructure = new RecipeMap<>(new HashMap<>());
                break;
            default :
                return;
        }
        while (true) {
            String input = br.readLine();
            if (input.equals("끝")){
                System.out.printf("[ %s 으로 저장된 %s ]\n%s", structure, title, recipeStructure);
                return;
            }
            recipeStructure.add(input);
        }
    }
}
