package eu.europeana.harvester.db.mongo;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import eu.europeana.harvester.domain.ReferenceOwner;
import eu.europeana.harvester.domain.SourceDocumentReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class SourceDocumentReferenceDaoImplTest {

    private static final Logger LOG = LogManager.getLogger(SourceDocumentReferenceDaoImplTest.class.getName());

    private SourceDocumentReferenceDaoImpl sourceDocumentReferenceDao;

    private MongodProcess mongod = null;
    private MongodExecutable mongodExecutable = null;
    private int port = 12345;

    public SourceDocumentReferenceDaoImplTest() throws IOException {

        MongodStarter starter = MongodStarter.getDefaultInstance();

        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(port, Network.localhostIsIPv6()))
                .build();

        mongodExecutable = starter.prepare(mongodConfig);
    }

    @Before
    public void setUp() throws Exception {
        mongod = mongodExecutable.start();

        Datastore datastore = null;
        try {
            MongoClient mongo = new MongoClient("localhost", port);
            Morphia morphia = new Morphia();
            String dbName = "harvester_persistency";

            datastore = morphia.createDatastore(mongo, dbName);
        } catch (UnknownHostException e) {
            LOG.error(e.getMessage());
        }

        sourceDocumentReferenceDao = new SourceDocumentReferenceDaoImpl(datastore);
    }

    @After
    public void tearDown() {
        mongodExecutable.stop();
    }


    @Test
    public void test_CreateOrModify_NullCollection() throws Exception {
        assertFalse(sourceDocumentReferenceDao.createOrModify((Collection) null, WriteConcern.NONE).iterator().hasNext());
    }

    @Test
    public void test_CreateOrModify_EmptyCollection() throws Exception {
        assertFalse(sourceDocumentReferenceDao.createOrModify(Collections.EMPTY_LIST, WriteConcern.NONE).iterator().hasNext());
    }

    @Test
    public void testCreate() throws Exception {
        final SourceDocumentReference sourceDocumentReference =
                new SourceDocumentReference(new ReferenceOwner("1", "1", "1"), "test", null, null, 0l, null, true);
        assertNotNull(sourceDocumentReference.getId());

        sourceDocumentReferenceDao.createOrModify(sourceDocumentReference, WriteConcern.NONE);
        assertEquals(sourceDocumentReference.getUrl(),
                     sourceDocumentReferenceDao.read(sourceDocumentReference.getId()).getUrl());

        sourceDocumentReferenceDao.delete(sourceDocumentReference.getId());
    }

    @Test
    public void test_CreateOrModify_ManyElements() throws Exception {
        final List<SourceDocumentReference> documentReferences = new ArrayList<>();

        for (int i = 0; i < 50; ++i) {
            final String iString = Integer.toString(i);
            documentReferences.add(
               new SourceDocumentReference(iString, new ReferenceOwner(iString, iString, iString, iString),
                                           "test", null, null, 0l, null, true
                                          )
            );
        }
        sourceDocumentReferenceDao.createOrModify(documentReferences, WriteConcern.NONE);

        for (final SourceDocumentReference document: documentReferences) {
            final SourceDocumentReference  writtenDocument = sourceDocumentReferenceDao.read(document.getId());

            sourceDocumentReferenceDao.delete(document.getId());
            ReflectionAssert.assertReflectionEquals(document, writtenDocument);
        }
    }

    @Test
    public void testRead() throws Exception {
        SourceDocumentReference sourceDocumentReferenceFromRead = sourceDocumentReferenceDao.read("");
        assertNull(sourceDocumentReferenceFromRead);

        final SourceDocumentReference sourceDocumentReference =
                new SourceDocumentReference(new ReferenceOwner("1", "1", "1"), "test", null, null, 0l, null, true);
        sourceDocumentReferenceDao.createOrModify(sourceDocumentReference, WriteConcern.NONE);

        sourceDocumentReferenceFromRead = sourceDocumentReferenceDao.read(sourceDocumentReference.getId());
        assertEquals(sourceDocumentReference.getUrl(), sourceDocumentReferenceFromRead.getUrl());

        sourceDocumentReferenceDao.delete(sourceDocumentReference.getId());
    }

    @Test
    public void testUpdate() throws Exception {
        final SourceDocumentReference sourceDocumentReference =
                new SourceDocumentReference(new ReferenceOwner("1", "1", "1"),"test", null, null, 0l, null, true);
        assertFalse(sourceDocumentReferenceDao.update(sourceDocumentReference, WriteConcern.NONE));
        sourceDocumentReferenceDao.createOrModify(sourceDocumentReference, WriteConcern.NONE);

        final SourceDocumentReference newSourceDocumentReference =
                new SourceDocumentReference(sourceDocumentReference.getId(),
                        sourceDocumentReference.getReferenceOwner(), "test2", null, null, 0l, null, true);
        assertTrue(sourceDocumentReferenceDao.update(newSourceDocumentReference, WriteConcern.NONE));

        assertEquals(sourceDocumentReferenceDao.read(sourceDocumentReference.getId()).getUrl(), "test2");
        sourceDocumentReferenceDao.delete(newSourceDocumentReference.getId());
    }

    @Test
    public void testDelete() throws Exception {
        final SourceDocumentReference sourceDocumentReference =
                new SourceDocumentReference(new ReferenceOwner("1", "1", "1"), "test", null, null, 0l, null, true);
        assertFalse(sourceDocumentReferenceDao.delete(sourceDocumentReference.getId()).getN() == 1);
        sourceDocumentReferenceDao.createOrModify(sourceDocumentReference, WriteConcern.NONE);

        SourceDocumentReference sourceDocumentReferenceFromRead =
                sourceDocumentReferenceDao.read(sourceDocumentReference.getId());
        assertNotNull(sourceDocumentReferenceFromRead);

        assertTrue(sourceDocumentReferenceDao.delete(sourceDocumentReference.getId()).getN() == 1);

        sourceDocumentReferenceFromRead = sourceDocumentReferenceDao.read(sourceDocumentReference.getId());
        assertNull(sourceDocumentReferenceFromRead);

        assertFalse(sourceDocumentReferenceDao.delete(sourceDocumentReference.getId()).getN() == 1);
    }

    @Test
    public void testCreateOrModify() throws Exception {
        final SourceDocumentReference sourceDocumentReference =
                new SourceDocumentReference(new ReferenceOwner("1", "1", "1"), "test", null, null, 0l, null, true);
        assertNull(sourceDocumentReferenceDao.read(sourceDocumentReference.getId()));

        sourceDocumentReferenceDao.createOrModify(sourceDocumentReference, WriteConcern.NONE);
        assertNotNull(sourceDocumentReferenceDao.read(sourceDocumentReference.getId()));
        assertEquals(sourceDocumentReferenceDao.read(sourceDocumentReference.getId()).getUrl(),
                sourceDocumentReference.getUrl());

        final SourceDocumentReference updatedSourceDocumentReference =
                new SourceDocumentReference(sourceDocumentReference.getId(),
                        new ReferenceOwner("1", "1", "1"), "test2", null, null, 0l, null, true);
        sourceDocumentReferenceDao.createOrModify(updatedSourceDocumentReference, WriteConcern.NONE);

        assertEquals(sourceDocumentReferenceDao.read(sourceDocumentReference.getId()).getUrl(),
                     updatedSourceDocumentReference.getUrl());
        assertNotEquals(sourceDocumentReferenceDao.read(sourceDocumentReference.getId()).getUrl(),
                sourceDocumentReference.getUrl());

        sourceDocumentReferenceDao.delete(sourceDocumentReference.getId());
    }

    @Test
    public void testFindByUrl() throws Exception {
        final SourceDocumentReference sourceDocumentReference =
                new SourceDocumentReference(new ReferenceOwner("1", "1", "1"), "test", null, null, 0l, null, true);

        sourceDocumentReferenceDao.createOrModify(sourceDocumentReference, WriteConcern.NONE);

        sourceDocumentReferenceDao.delete(sourceDocumentReference.getId());
    }
}
