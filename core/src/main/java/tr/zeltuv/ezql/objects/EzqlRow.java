package tr.zeltuv.ezql.objects;

import java.util.HashMap;
import java.util.Map;

public class EzqlRow {


    private Map<String, Object> results = new HashMap<>();

    public EzqlRow(Object... objects) {
        if(objects.length%2 !=0)
            return;

        for (int i = 0; i < objects.length; i+=2) {
            if(objects.length > i+1){

                String key = (String) objects[i];
                Object value = objects[i+1];

                addValue(key,value);
            }
        }
    }

    public Map<String, Object> getValues() {
        return results;
    }

    public <T> T getValue(String id) {
        return (T) results.get(id);
    }

    public void addValue(String id, Object object) {
        results.put(id, object);
    }

    public void removeValue(String id) {
        results.remove(id);
    }

    @Override
    public String toString() {
        return "EzqlResultRow{" +
                "results=" + results +
                '}';
    }
}
