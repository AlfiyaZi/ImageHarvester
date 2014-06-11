package eu.europeana.harvester.db.mongo;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import eu.europeana.harvester.db.SourceDocumentReferenceMetaInfoDao;
import eu.europeana.harvester.domain.SourceDocumentReferenceMetaInfo;

import java.util.List;

public class SourceDocumentReferenceMetaInfoDaoImpl implements SourceDocumentReferenceMetaInfoDao {

    private final Datastore datastore;

    public SourceDocumentReferenceMetaInfoDaoImpl(Datastore datastore) {
        this.datastore = datastore;
    }

    @Override
    public void create(SourceDocumentReferenceMetaInfo sourceDocumentReferenceMetaInfo) {
        datastore.save(sourceDocumentReferenceMetaInfo);
    }

    @Override
    public SourceDocumentReferenceMetaInfo read(String id) {
        Query<SourceDocumentReferenceMetaInfo> query = datastore.find(SourceDocumentReferenceMetaInfo.class);
        query.criteria("id").equal(id);

        List<SourceDocumentReferenceMetaInfo> result = query.asList();
        if(!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    @Override
    public boolean update(SourceDocumentReferenceMetaInfo sourceDocumentReferenceMetaInfo) {
        Query<SourceDocumentReferenceMetaInfo> query = datastore.find(SourceDocumentReferenceMetaInfo.class);
        query.criteria("id").equal(sourceDocumentReferenceMetaInfo.getId());

        List<SourceDocumentReferenceMetaInfo> result = query.asList();
        if(!result.isEmpty()) {
            datastore.delete(query);
            datastore.save(sourceDocumentReferenceMetaInfo);

            return true;
        }

        return false;
    }

    @Override
    public boolean delete(SourceDocumentReferenceMetaInfo sourceDocumentReferenceMetaInfo) {
        Query<SourceDocumentReferenceMetaInfo> query = datastore.find(SourceDocumentReferenceMetaInfo.class);
        query.criteria("id").equal(sourceDocumentReferenceMetaInfo.getId());

        List<SourceDocumentReferenceMetaInfo> result = query.asList();
        if(!result.isEmpty()) {
            datastore.delete(sourceDocumentReferenceMetaInfo);

            return true;
        }

        return false;
    }

    @Override
    public SourceDocumentReferenceMetaInfo findBySourceDocumentReferenceId(String id) {
        Query<SourceDocumentReferenceMetaInfo> query = datastore.find(SourceDocumentReferenceMetaInfo.class);
        query.criteria("sourceDocumentReferenceId").equal(id);

        List<SourceDocumentReferenceMetaInfo> result = query.asList();
        if(!result.isEmpty()) {
            return result.get(0);
        }

        return null;
    }

}