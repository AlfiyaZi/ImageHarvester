package eu.europeana.harvester.domain;

import com.mongodb.DBObject;
import org.bson.BSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * A special metadata object specially created for our usecase. It's basically a hashmap.
 */
public class MongoMetaData implements DBObject {

    final Map<MetaDataFields, Object> map = new HashMap<MetaDataFields, Object>();

    @Override
    public void markAsPartialObject() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isPartialObject() {
        return false;
    }

    @Override
    public Object put(String s, Object o) {
        return map.put(MetaDataFields.valueOf(s), o);
    }

    @Override
    public void putAll(BSONObject bsonObject) {
        throw new NotImplementedException();
    }

    @Override
    public void putAll(Map map) {
        throw new NotImplementedException();
    }

    @Override
    public Object get(String s) {
        return map.get(MetaDataFields.valueOf(s));
    }

    @Override
    public Map toMap() {
        return map;
    }

    @Override
    public Object removeField(String s) {
        return map.remove(MetaDataFields.valueOf(s));
    }

    @Override
    public boolean containsKey(String s) {
        return map.containsKey(MetaDataFields.valueOf(s));
    }

    @Override
    public boolean containsField(String s) {
        return map.containsValue(s);
    }

    @Override
    public Set<String> keySet() {
        final Set<MetaDataFields> metaDataFieldsList = map.keySet();
        final Set<String> metaDataFieldsStringList = new HashSet<String>();
        for(MetaDataFields metaDataFields : metaDataFieldsList) {
            metaDataFieldsStringList.add(String.valueOf(metaDataFields));
        }

        return metaDataFieldsStringList;
    }
}
